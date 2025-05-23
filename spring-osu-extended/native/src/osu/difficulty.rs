use super::java_fu::{get_object_ptr, set_object_ptr};
use crate::java::{cache_key::*, get_jni_class, get_jni_static_method_id};
use crate::{get_class, get_mods_from_java, to_ptr, to_status_replace, to_status_use, Result};
use jni::objects::{GlobalRef, JClass, JObject, JString};
use jni::signature::ReturnType;
use jni::sys::{jbyte, jdouble, jfloat, jint, jlong, jobject, jvalue};
use jni::JNIEnv;
use rosu_pp::any::DifficultyAttributes;
use rosu_pp::catch::CatchDifficultyAttributes;
use rosu_pp::mania::ManiaDifficultyAttributes;
use rosu_pp::osu::OsuDifficultyAttributes;
use rosu_pp::taiko::TaikoDifficultyAttributes;
use rosu_pp::{Difficulty, GameMods};

macro_rules! set_state {
    ($($fx:ident($f:ident);)+) => {$(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: f32, with_mods: bool) -> Result<()> {
            let ptr = get_object_ptr(env, this)?;
            to_status_replace::<Difficulty>(ptr, |difficulty: Difficulty| {
                difficulty.$f(value, with_mods)
            })?;
            Ok(())
        }
    )+};
    ($($fx:ident[$f:ident:$t:ty];)+) => {$(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: $t) -> Result<()> {
            let ptr = get_object_ptr(env, this)?;
            to_status_replace::<Difficulty>(ptr, |difficulty: Difficulty| {
                difficulty.$f(value)
            })?;
            Ok(())
        }
    )+};
}

set_state! {
    set_difficulty_ar(ar);
    set_difficulty_od(od);
    set_difficulty_cs(cs);
    set_difficulty_hp(hp);
}
set_state! {
    set_difficulty_is_lazer[lazer:bool];
    set_difficulty_is_hardrock[hardrock_offsets:bool];
    set_difficulty_passed_objects[passed_objects:u32];
    set_difficulty_clock_rate[clock_rate:f64];
}

pub fn set_difficulty_mods_bitflag(env: &mut JNIEnv, this: &JObject, legacy: jint) -> Result<()> {
    let legacy = get_mods_from_java!(legacy);
    set_difficulty_mods(env, this, legacy)
}
pub fn set_difficulty_mods_lazer(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    lazer: &JString,
) -> Result<()> {
    let lazer = get_mods_from_java!(env, mode, lazer)?;
    set_difficulty_mods(env, this, lazer)
}

pub fn set_difficulty_mods_mix(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    legacy: jint,
    lazer: &JString,
) -> Result<()> {
    let all = get_mods_from_java!(env, mode, legacy, lazer)?;
    set_difficulty_mods(env, this, all)
}

#[inline]
fn set_difficulty_mods(env: &mut JNIEnv, this: &JObject, mods: GameMods) -> Result<()> {
    let ptr = get_object_ptr(env, this)?;
    to_status_replace::<Difficulty>(ptr, |difficulty: Difficulty| difficulty.mods(mods))?;
    Ok(())
}

pub fn generate_difficulty(
    env: &mut JNIEnv,
    this: &JObject,
    with_mods: bool,
    is_lazer: bool,
    is_all_null: bool,
    ar: jfloat,
    od: jfloat,
    cs: jfloat,
    hp: jfloat,
    clock_rate: jdouble,
) -> Result<()> {
    let mut difficulty = Difficulty::new().lazer(is_lazer);
    if !is_all_null {
        macro_rules! f {
            ($name:ident) => {
                if $name >= -9f32 {
                    difficulty = difficulty.$name($name, with_mods);
                }
            };
        }
        f!(ar);
        f!(od);
        f!(cs);
        f!(hp);
        if clock_rate >= 0f64 {
            difficulty = difficulty.clock_rate(clock_rate);
        }
    }
    let ptr = to_ptr(difficulty);
    set_object_ptr(env, this, ptr)?;
    Ok(())
}

pub fn generate_difficulty_attributes_osu<'l>(
    env: &mut JNIEnv<'l>,
    data: &OsuDifficultyAttributes,
) -> Result<JObject<'l>> {
    let global = get_difficulty_attributes_class(env)?;
    let jclass = get_class!(global);
    let args = &[
        jvalue { d: data.aim },
        jvalue { d: data.speed },
        jvalue { d: data.flashlight },
        jvalue {
            d: data.slider_factor,
        },
        jvalue {
            d: data.speed_note_count,
        },
        jvalue {
            d: data.aim_difficult_strain_count,
        },
        jvalue {
            d: data.speed_difficult_strain_count,
        },
        jvalue { d: data.ar },
        jvalue { d: data.od() },
        jvalue { d: data.hp },
        jvalue {
            i: data.n_circles as i32,
        },
        jvalue {
            i: data.n_sliders as i32,
        },
        jvalue {
            i: data.n_large_ticks as i32,
        },
        jvalue {
            i: data.n_spinners as i32,
        },
        jvalue { d: data.stars },
        jvalue {
            i: data.max_combo as i32,
        },
    ];
    let method = get_jni_static_method_id(DIFFICULTY_OSU, || {
        let method = env.get_static_method_id(
            jclass,
            "createOsu",
            "(DDDDDDDDDDIIIIDI)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let obj =
        unsafe { env.call_static_method_unchecked(jclass, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

pub fn generate_difficulty_attributes_taiko<'l>(
    env: &mut JNIEnv<'l>,
    data: &TaikoDifficultyAttributes,
) -> Result<JObject<'l>> {
    let global = get_difficulty_attributes_class(env)?;
    let jclass = get_class!(global);
    let args = &[
        jvalue { d: data.stamina },
        jvalue { d: data.rhythm },
        jvalue { d: data.color },
        jvalue { d: data.reading },
        jvalue {
            d: data.great_hit_window,
        },
        jvalue {
            d: data.ok_hit_window,
        },
        jvalue {
            d: data.mono_stamina_factor,
        },
        jvalue { d: data.stars },
        jvalue {
            i: data.max_combo as i32,
        },
        jvalue {
            b: if data.is_convert { 1i8 } else { 0i8 },
        },
    ];

    let method = get_jni_static_method_id(DIFFICULTY_TAIKO, || {
        let method = env.get_static_method_id(
            jclass,
            "createTaiko",
            "(DDDDDDDDIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let obj =
        unsafe { env.call_static_method_unchecked(jclass, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

pub fn generate_difficulty_attributes_catch<'l>(
    env: &mut JNIEnv<'l>,
    data: &CatchDifficultyAttributes,
) -> Result<JObject<'l>> {
    let global = get_difficulty_attributes_class(env)?;
    let jclass = get_class!(global);
    let args = &[
        jvalue { d: data.stars },
        jvalue { d: data.ar },
        jvalue {
            i: data.n_fruits as i32,
        },
        jvalue {
            i: data.n_droplets as i32,
        },
        jvalue {
            i: data.n_tiny_droplets as i32,
        },
        jvalue {
            b: if data.is_convert { 1i8 } else { 0i8 },
        },
    ];

    let method = get_jni_static_method_id(DIFFICULTY_CATCH, || {
        let method = env.get_static_method_id(
            jclass,
            "createCatch",
            "(DDIIIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let obj =
        unsafe { env.call_static_method_unchecked(jclass, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

pub fn generate_difficulty_attributes_mania<'l>(
    env: &mut JNIEnv<'l>,
    data: &ManiaDifficultyAttributes,
) -> Result<JObject<'l>> {
    let global = get_difficulty_attributes_class(env)?;
    let jclass = get_class!(global);
    let args = &[
        jvalue { d: data.stars },
        jvalue {
            i: data.n_objects as i32,
        },
        jvalue {
            i: data.n_hold_notes as i32,
        },
        jvalue {
            i: data.max_combo as i32,
        },
        jvalue {
            b: if data.is_convert { 1i8 } else { 0i8 },
        },
    ];

    let method = get_jni_static_method_id(DIFFICULTY_MANAI, || {
        let method = env.get_static_method_id(
            jclass,
            "createMania",
            "(DIIIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let obj =
        unsafe { env.call_static_method_unchecked(jclass, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

fn get_difficulty_attributes_class(env: &mut JNIEnv) -> Result<GlobalRef> {
    get_jni_class(
        DIFFICULTY_ATTR_CLASS,
        env,
        "org/spring/osu/extended/rosu/JniDifficultyAttributes",
    )
}

pub fn generate_difficulty_attributes<'l>(
    env: &mut JNIEnv<'l>,
    difficulty_attributes: &DifficultyAttributes,
) -> Result<JObject<'l>> {
    let object = match difficulty_attributes {
        DifficultyAttributes::Osu(data) => generate_difficulty_attributes_osu(env, data)?,
        DifficultyAttributes::Taiko(data) => generate_difficulty_attributes_taiko(env, data)?,
        DifficultyAttributes::Catch(data) => generate_difficulty_attributes_catch(env, data)?,
        DifficultyAttributes::Mania(data) => generate_difficulty_attributes_mania(env, data)?,
    };
    Ok(object)
}

pub fn difficulty_calculate(
    env: &mut JNIEnv,
    this: &JObject,
    beatmap_ptr: jlong,
) -> Result<jobject> {
    let beatmap = to_status_use(beatmap_ptr)?;
    let ptr = get_object_ptr(env, this)?;
    let difficulty = to_status_use::<Difficulty>(ptr)?;
    let attr = difficulty.calculate(beatmap);
    let obj = generate_difficulty_attributes(env, &attr)?;
    let ptr = match attr {
        DifficultyAttributes::Osu(data) => to_ptr(data),
        DifficultyAttributes::Taiko(data) => to_ptr(data),
        DifficultyAttributes::Catch(data) => to_ptr(data),
        DifficultyAttributes::Mania(data) => to_ptr(data),
    };
    set_object_ptr(env, &obj, ptr)?;
    Ok(obj.into_raw())
}
