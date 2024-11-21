mod beatmap;
mod difficulty;
mod performance;
mod mods;

use crate::java::{throw_jni, JavaBoolean};
use crate::osu::performance::{generate_state, get_performance_from_beatmap};
use crate::{jni_call, to_status_use};
use beatmap::*;
use difficulty::*;
use java_fu::*;
use jni::objects::{JByteArray, JClass, JObject, JString, JValueGen};
use jni::sys::{jboolean, jbyte, jdouble, jfloat, jint, jlong, jobject, JNI_FALSE};
use jni::JNIEnv;
use jni_macro::jni_fn;
use rosu_pp::any::ScoreState;

mod java_fu {
    use crate::{to_status, Result};
    use jni::objects::{JByteArray, JFieldID, JObject, JValueGen};
    use jni::JNIEnv;
    use jni::signature::{Primitive, ReturnType};
    use rosu_pp::any::ScoreState;
    use rosu_pp::catch::CatchDifficultyAttributes;
    use rosu_pp::mania::ManiaDifficultyAttributes;
    use rosu_pp::osu::OsuDifficultyAttributes;
    use rosu_pp::taiko::TaikoDifficultyAttributes;
    use rosu_pp::{Beatmap, Difficulty, Performance};
    use crate::java::get_jni_field_id;

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
            }
        }
        Ok(())
    }

    pub fn get_bytes(env: &mut JNIEnv, data: &JByteArray) -> Result<Vec<u8>> {
        let bytes = env.convert_byte_array(data)?;
        Ok(bytes)
    }

    fn get_ptr_field(env: &mut JNIEnv) -> Result<JFieldID> {
        get_jni_field_id("_ptr", || {
            let f =
                env.get_field_id("org/spring/osu/extended/rosu/NativeClass", "_ptr", "J")?;
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
        let r = env.get_field_unchecked(obj, f_id, ReturnType::Primitive(Primitive::Long));
        let result = match r {
            Ok(data) => data,
            Err(e) => {
                println!("{e:?}");
                return Err("can not get pointer".into());
            }
        };
        Ok(result.j()?)
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
        fn $fx(mut env: JNIEnv, this: JObject, value: jfloat, is_lazer: jboolean) {
            jni_call! {
                [env]$t(&mut env, &this, value, is_lazer.is_true())
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
set_difficulty_state!(setLazer > set_difficulty_is_lazer);
set_difficulty_state!(setHardrock > set_difficulty_is_hardrock);

#[jni_fn("org.spring.osu.extended.rosu.JniDifficulty")]
fn nativeSetClockRate(mut env: JNIEnv, this: JObject, clock_rate: jdouble) {
    jni_call!([env]set_difficulty_clock_rate(&mut env, &this, clock_rate))
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
fn calculate(mut env: JNIEnv, this: JObject, beatmap: JObject) -> jobject {
    jni_call!([env]difficulty_calculate(&mut env, &this, &beatmap) => {JObject::null().into_raw()})
}

#[jni_fn("org.spring.osu.extended.rosu.JniScoreState")]
fn initState(mut env: JNIEnv, this: JObject) {
    jni_call! {
        [env]generate_state(&mut env, &this)
    }
}

macro_rules! set_score_state {
    ($fx:ident($field:literal, $value:ident)) => {
        #[jni_fn("org.spring.osu.extended.rosu.JniScoreState")]
        fn $fx(mut env: JNIEnv, this: JObject, value: jint) {
            let env_mut = &mut env;
            let ptr = match get_object_ptr(env_mut, &this) {
                Ok(data) => data,
                Err(e) => {
                    throw_jni(env_mut, e);
                    return;
                }
            };

            let state = match to_status_use::<ScoreState>(ptr) {
                Ok(data) => data,
                Err(e) => {
                    throw_jni(env_mut, e);
                    return;
                }
            };

            if let Err(e) = env_mut.set_field(&this, $field, "I", JValueGen::Int(value)) {
                throw_jni(env_mut, e.into());
            }

            state.$value = value as u32;
        }
    };
}

set_score_state!(setMaxCombo("maxCombo", max_combo));
set_score_state!(setSliderTickHits("sliderTickHits", max_combo));
set_score_state!(setSliderEndHits("sliderEndHits", max_combo));
set_score_state!(setGeki("geki", n_geki));
set_score_state!(setKatu("katu", n_katu));
set_score_state!(setN300("n300", n300));
set_score_state!(setN100("n100", n100));
set_score_state!(setN50("n50", n50));
set_score_state!(setMisses("misses", misses));

#[jni_fn("org.spring.osu.extended.rosu.JniPerformance")]
fn initByBeatmap<'local>(mut env: JNIEnv, this: JObject, beatmap: JObject) {
    jni_call! {
        [env]get_performance_from_beatmap(&mut env, &this, &beatmap)
    }
}
