mod beatmap;
mod difficulty;
mod gradual;
mod mods;
mod performance;

use crate::java::{throw_jni, JavaBoolean};
use crate::jni_call;
use crate::osu::performance::*;
use beatmap::*;
use difficulty::*;
use gradual::*;
use java_fu::*;
use jni::objects::{JByteArray, JClass, JObject, JString};
use jni::sys::{jboolean, jbyte, jdouble, jfloat, jint, jlong, jobject, JNI_FALSE};
use jni::JNIEnv;
use jni_macro::jni_fn;
use rosu_pp::any::HitResultPriority;

mod java_fu {
    use crate::java::get_jni_field_id;
    use crate::{throw, to_status, Result};
    use jni::objects::{JByteArray, JFieldID, JObject, JValueGen};
    use jni::signature::{Primitive, ReturnType};
    use jni::JNIEnv;
    use rosu_pp::any::ScoreState;
    use rosu_pp::catch::CatchDifficultyAttributes;
    use rosu_pp::mania::ManiaDifficultyAttributes;
    use rosu_pp::osu::OsuDifficultyAttributes;
    use rosu_pp::taiko::TaikoDifficultyAttributes;
    use rosu_pp::{Beatmap, Difficulty, GradualPerformance, Performance};

    #[warn(unused_must_use)]
    pub fn release_by_type(ptr: i64, type_val: i8) -> Result<()> {
        if ptr == 0 {
            return Ok(());
        }
        macro_rules! release_status {
            ($t:ident,$ptr:ident{$($val:literal $status:ident,)*}) => {
                match $t {
                    $($val => {let _ = to_status::<$status>($ptr)?;})*
                    _ => { return Err("unknown type!".into()) }
                }
            };
        }

        release_status! {
            type_val, ptr {
                0 Beatmap,
                1 Difficulty,
                2 Performance,
                3 ScoreState,
                4 OsuDifficultyAttributes,
                5 TaikoDifficultyAttributes,
                6 CatchDifficultyAttributes,
                7 ManiaDifficultyAttributes,
                8 GradualPerformance,
            }
        }
        Ok(())
    }

    pub fn get_bytes(env: &mut JNIEnv, data: &JByteArray) -> Result<Vec<u8>> {
        let bytes = env.convert_byte_array(data)?;
        Ok(bytes)
    }

    fn get_ptr_field(env: &mut JNIEnv) -> Result<JFieldID> {
        get_jni_field_id(crate::java::cache_key::PTR, || {
            let f = env.get_field_id("org/spring/osu/extended/rosu/NativeClass", "_ptr", "J")?;
            Ok(f)
        })
    }
    pub fn set_object_ptr(env: &mut JNIEnv, obj: &JObject, ptr: i64) -> Result<()> {
        let field = get_ptr_field(env)?;
        env.set_field_unchecked(obj, field, JValueGen::Long(ptr))?;
        Ok(())
    }
    pub fn get_object_ptr(env: &mut JNIEnv, obj: &JObject) -> Result<i64> {
        let f_id = get_ptr_field(env)?;
        let result = env
            .get_field_unchecked(obj, f_id, ReturnType::Primitive(Primitive::Long))
            .or_else(|e| throw(format!("can not get point [{e:?}]")))?;
        Ok(result.j()?)
    }
    pub fn release_object(env: &mut JNIEnv, obj: &JObject) -> Result<()> {
        let field = get_ptr_field(env)?;
        env.set_field_unchecked(obj, field, JValueGen::Long(0))?;
        Ok(())
    }
}

#[jni_fn("org.spring.osu.extended.rosu.NativeClass")]
fn release(mut env: JNIEnv, _: JClass, ptr: jlong, jtype: jbyte) {
    jni_call! {
        [env]release_by_type(ptr, jtype)
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniBeatmap")]
fn parseByBytes(mut env: JNIEnv, this: JObject, map: JByteArray) {
    jni_call! {
        [env]get_beatmap_from_data(&mut env, &this, map)
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniBeatmap")]
fn parseByPath(mut env: JNIEnv, this: JObject, local: JString) {
    jni_call! {
        [env]get_beatmap_from_local(&mut env, &this, &local)
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniBeatmap")]
fn convertInPlace(mut env: JNIEnv, this: JObject, mode_byte: jbyte) -> jboolean {
    jni_call! {
        [env]try_to_convert(&mut env, &this, mode_byte) => {
            JNI_FALSE
        }
    }
}

macro_rules! set_difficulty_state {
    ($fx:ident:$t:ident) => {
        #[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
        fn $fx(mut env: JNIEnv, this: JObject, value: jfloat, with_mods: jboolean) {
            jni_call! {
                [env]$t(&mut env, &this, value, with_mods.is_true())
            }
        }
    };
    ($fx:ident>$t:ident) => {
        #[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
        fn $fx(mut env: JNIEnv, this: JObject, b: jboolean) {
            jni_call! {
                [env]$t(&mut env, &this, b.is_true())
            }
        }
    };
}

set_difficulty_state!(nativeSetAr:set_difficulty_ar);
set_difficulty_state!(nativeSetOd:set_difficulty_od);
set_difficulty_state!(nativeSetCs:set_difficulty_cs);
set_difficulty_state!(nativeSetHp:set_difficulty_hp);
set_difficulty_state!(nativeSetLazer > set_difficulty_is_lazer);
set_difficulty_state!(setHardrock > set_difficulty_is_hardrock);

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn setPassObject(mut env: JNIEnv, this: JObject, number: jint) {
    jni_call!([env]set_difficulty_passed_objects(&mut env, &this, number as u32))
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeSetClockRate(mut env: JNIEnv, this: JObject, clock_rate: jdouble) {
    jni_call!([env]set_difficulty_clock_rate(&mut env, &this, clock_rate))
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeSetMods(mut env: JNIEnv, this: JObject, legacy: jint) {
    jni_call!([env]set_difficulty_mods_bitflag(&mut env, &this, legacy))
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeSetModsByStr(mut env: JNIEnv, this: JObject, mode: jbyte, lazer: JString) {
    jni_call!([env]set_difficulty_mods_lazer(&mut env, &this, mode, &lazer))
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeSetModsMix(mut env: JNIEnv, this: JObject, mode: jbyte, legacy: jint, lazer: JString) {
    jni_call!([env]set_difficulty_mods_mix(&mut env, &this, mode, legacy, &lazer))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn convertInPlace(mut env: JNIEnv, this: JObject, mode_byte: jbyte) {
    jni_call!([env]set_performance_game_mode(&mut env, &this, mode_byte))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetMods(mut env: JNIEnv, this: JObject, legacy: jint) {
    jni_call!([env]set_performance_mods_bitflag(&mut env, &this, legacy))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetModsByStr(mut env: JNIEnv, this: JObject, mode: jbyte, lazer: JString) {
    jni_call!([env]set_performance_mods_lazer(&mut env, &this, mode, &lazer))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetModsMix(mut env: JNIEnv, this: JObject, mode: jbyte, legacy: jint, lazer: JString) {
    jni_call!([env]set_performance_mods_mix(&mut env, &this, mode, legacy, &lazer))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetState(mut env: JNIEnv, this: JObject, state: JByteArray) {
    jni_call!([env]set_performance_state(&mut env, &this, &state))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetDifficulty(mut env: JNIEnv, this: JObject, difficulty: jlong) {
    jni_call!([env]set_performance_difficulty(&mut env, &this, difficulty))
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn generateState(mut env: JNIEnv, this: JObject) -> jobject {
    jni_call!([env]generate_state(&mut env, &this) => {JObject::null().into_raw()})
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn initDifficulty(
    mut env: JNIEnv,
    this: JObject,
    with_mods: jboolean,
    lazer: jboolean,
    is_all_null: jboolean,
    ar: jfloat,
    od: jfloat,
    cs: jfloat,
    hp: jfloat,
    cr: jdouble,
) {
    jni_call!([env]generate_difficulty(
        &mut env,
        &this,
        with_mods.is_true(),
        lazer.is_true(),
        is_all_null.is_true(),
        ar, od, cs, hp, cr
    ))
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeCalculate(mut env: JNIEnv, this: JObject, beatmap: jlong) -> jobject {
    jni_call!([env]difficulty_calculate(&mut env, &this, beatmap) => { JObject::null().into_raw() })
}

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeTransformToGradualPerformance(mut env: JNIEnv, this: JObject, beatmap: jlong) -> jobject {
    jni_call!([env]gradual_performance(&mut env, &this, beatmap) => { JObject::null().into_raw() })
}

#[jni_fn("org.spring.osu.extended.rosu.JniGradualPerformance")]
fn remainingLength(mut env: JNIEnv, this: JObject) -> jint {
    jni_call!([env]gradual_len(&mut env, &this) => { 0 })
}

#[jni_fn("org.spring.osu.extended.rosu.JniGradualPerformance")]
fn nativeNext(mut env: JNIEnv, this: JObject, state: JByteArray) -> jobject {
    jni_call!([env]gradual_next(&mut env, &this, &state) => { JObject::null().into_raw() })
}

#[jni_fn("org.spring.osu.extended.rosu.JniGradualPerformance")]
fn nativeLast(mut env: JNIEnv, this: JObject, state: JByteArray) -> jobject {
    jni_call!([env]gradual_last(&mut env, &this, &state) => { JObject::null().into_raw() })
}

#[jni_fn("org.spring.osu.extended.rosu.JniGradualPerformance")]
fn nativeNextSome(mut env: JNIEnv, this: JObject, state: JByteArray, n: jint) -> jobject {
    jni_call!([env]gradual_nth(&mut env, &this, &state, n) => { JObject::null().into_raw() })
}

macro_rules! init_performance {
    ($($fx:ident>$call:ident;)+) => {$(
        #[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
        fn $fx(mut env: JNIEnv, this: JObject, ptr: jlong) {
            jni_call! {
                [env]$call(&mut env, &this, ptr)
            }
        }
    )+};
    ($(+$fx:ident>$call:ident;)+) => {$(
        #[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
        fn $fx<'local>(mut env: JNIEnv, this: JObject, ptr: jlong, state:JByteArray) {
            jni_call! {
                [env]$call(&mut env, &this, ptr, &state)
            }
        }
    )+};
    ($(|$fx:ident>$call:ident;)+) => {$(
        #[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
        fn $fx(mut env: JNIEnv, this: JObject, value: jint) {
            jni_call! {
                [env]$call(&mut env, &this, value)
            }
        }
    )+};
    ($(~$fx:ident>$call:ident;)+) => {$(
        #[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
        fn $fx(mut env: JNIEnv, this: JObject, value: jfloat, with_mods: jboolean) {
            jni_call! {
                [env]$call(&mut env, &this, value, with_mods.is_true())
            }
        }
    )+};
}

init_performance! {
    initByBeatmap > init_performance_by_beatmap;
    initByOsuDifficultyAttributes > init_performance_by_osu_attributes;
    initByTaikoDifficultyAttributes > init_performance_by_taiko_attributes;
    initByCatchDifficultyAttributes > init_performance_by_catch_attributes;
    initByManiaDifficultyAttributes > init_performance_by_mania_attributes;
}

init_performance! {
    +initByBeatmapWithState > init_performance_by_beatmap_with_state;
    +initByOsuDifficultyAttributesWithState > init_performance_by_osu_attributes_with_state;
    +initByTaikoDifficultyAttributesWithState > init_performance_by_taiko_attributes_with_state;
    +initByCatchDifficultyAttributesWithState > init_performance_by_catch_attributes_with_state;
    +initByManiaDifficultyAttributesWithState > init_performance_by_mania_attributes_with_state;
}

init_performance! {
    |setCombo   > set_performance_combo;
    |setGeki > set_performance_geki;
    |setKatu > set_performance_katu;
    |setN300 > set_performance_n300;
    |setN100 > set_performance_n100;
    |setN50 > set_performance_n50;
    |setMisses > set_performance_misses;
    |setLargeTick > set_performance_large_tick;
    |setSmallTick > set_performance_small_tick;
    |setSliderEnds > set_performance_slider_ends;
    |setPassedObjects > set_performance_passed_objects;
}

init_performance! {
    ~nativeSetAr > set_performance_ar;
    ~nativeSetOd > set_performance_od;
    ~nativeSetCs > set_performance_cs;
    ~nativeSetHp > set_performance_hp;
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetLazer(mut env: JNIEnv, this: JObject, value: jboolean) {
    jni_call! {
        [env]set_performance_is_lazer(&mut env, &this, value.is_true())
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn setHardrock(mut env: JNIEnv, this: JObject, value: jboolean) {
    jni_call! {
        [env]set_performance_is_hardrock(&mut env, &this, value.is_true())
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn setClockRate(mut env: JNIEnv, this: JObject, value: jdouble) {
    jni_call! {
        [env]set_performance_clock_rate(&mut env, &this, value)
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeSetAccuracy(mut env: JNIEnv, this: JObject, value: jdouble) {
    jni_call! {
        [env]set_performance_accuracy(&mut env, &this, value)
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn setHitResultPriority(mut env: JNIEnv, this: JObject, value: jboolean) {
    jni_call! {
        [env]set_performance_hitresult_priority(
            &mut env,
            &this,
            if value.is_true() { HitResultPriority::BestCase }
            else { HitResultPriority::WorstCase }
        )
    }
}

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn nativeCalculate(mut env: JNIEnv, this: JObject) -> jobject {
    jni_call!([env]calculate_performance(&mut env, &this) => {JObject::null().into_raw()})
}
