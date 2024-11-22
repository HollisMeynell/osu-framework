use super::java_fu::{get_object_ptr, set_object_ptr};
use crate::java::get_jni_static_method_id;
use crate::{get_mods_from_java, to_ptr, to_status, to_status_use, Result};
use jni::objects::{JClass, JObject, JString};
use jni::signature::ReturnType;
use jni::sys::{jbyte, jdouble, jfloat, jint, jlong, jobject, jvalue};
use jni::JNIEnv;
use rosu_pp::any::DifficultyAttributes;
use rosu_pp::catch::CatchDifficultyAttributes;
use rosu_pp::mania::ManiaDifficultyAttributes;
use rosu_pp::osu::OsuDifficultyAttributes;
use rosu_pp::taiko::TaikoDifficultyAttributes;
use rosu_pp::Difficulty;

const DIFFICULTY_CLASS_OSU: &str = "dm_c_o";
const DIFFICULTY_CLASS_TAIKO: &str = "dm_c_t";
const DIFFICULTY_CLASS_CATCH: &str = "dm_c_c";
const DIFFICULTY_CLASS_MANAI: &str = "dm_c_m";
const DIFFICULTY_OSU: &str = "dm_init_o";
const DIFFICULTY_TAIKO: &str = "dm_init_t";
const DIFFICULTY_CATCH: &str = "dm_init_c";
const DIFFICULTY_MANAI: &str = "dm_init_m";

macro_rules! set_state {
    ($($fx:ident($f:ident);)+) => {$(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: f32, is_lazer: bool) -> Result<()> {
            set_difficulty_attr(env, this, |difficulty| difficulty.$f(value, is_lazer))
        }
    )+};
    ($($fx:ident[$f:ident:$t:ty];)+) => {$(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: $t) -> Result<()> {
            set_difficulty_attr(env, this, |difficulty| difficulty.$f(value))
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
    set_difficulty_attr(env, this, |difficulty| difficulty.mods(legacy))
}
pub fn set_difficulty_mods_lazer(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    lazer: &JString,
) -> Result<()> {
    let lazer = get_mods_from_java!(env, mode, lazer)?;
    set_difficulty_attr(env, this, |difficulty| difficulty.mods(lazer))
}

pub fn set_difficulty_mods_mix(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    legacy: jint,
    lazer: &JString,
) -> Result<()> {
    let all = get_mods_from_java!(env, mode, legacy, lazer)?;
    set_difficulty_attr(env, this, |difficulty| difficulty.mods(all))
}

#[inline]
fn set_difficulty_attr(
    env: &mut JNIEnv,
    this: &JObject,
    f: impl FnOnce(Box<Difficulty>) -> Difficulty,
) -> Result<()> {
    let ptr = get_object_ptr(env, this)?;
    let difficulty = to_status(ptr)?;
    let difficulty = f(difficulty);
    let n_ptr = to_ptr(difficulty);
    set_object_ptr(env, this, n_ptr)?;
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
    let mut difficulty = Difficulty::new();
    if is_lazer {
        difficulty = difficulty.lazer(is_lazer);
    }
    if !is_all_null {
        macro_rules! set_field {
            ($name:ident) => {
                if $name >= -9f32 {
                    difficulty = difficulty.$name($name, with_mods)
                }
            };
        }
        set_field!(ar);
        set_field!(od);
        set_field!(cs);
        set_field!(hp);
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
    let class = env.find_class("org/spring/osu/extended/rosu/JniDifficultyAttributes")?;
    let jclass = class.as_raw();
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
        jvalue { d: data.stars },
        jvalue {
            i: data.max_combo as i32,
        },
    ];
    let method = get_jni_static_method_id(DIFFICULTY_OSU, || {
        let class = unsafe { JClass::from_raw(jclass) };
        let method = env.get_static_method_id(
            class,
            "createOsu",
            "(DDDDDDDDI)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let class = unsafe { JClass::from_raw(jclass) };
    let obj = unsafe { env.call_static_method_unchecked(class, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

pub fn generate_difficulty_attributes_taiko<'l>(
    env: &mut JNIEnv<'l>,
    data: &TaikoDifficultyAttributes,
) -> Result<JObject<'l>> {
    let class = env.find_class("org/spring/osu/extended/rosu/JniDifficultyAttributes")?;
    let jclass = class.as_raw();
    let args = &[
        jvalue { d: data.stamina },
        jvalue { d: data.rhythm },
        jvalue { d: data.color },
        jvalue { d: data.peak },
        jvalue {
            d: data.great_hit_window,
        },
        jvalue {
            d: data.ok_hit_window,
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
        let class = unsafe { JClass::from_raw(jclass) };
        let method = env.get_static_method_id(
            class,
            "createTaiko",
            "(DDDDDDDIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let class = unsafe { JClass::from_raw(jclass) };
    let obj = unsafe { env.call_static_method_unchecked(class, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

pub fn generate_difficulty_attributes_catch<'l>(
    env: &mut JNIEnv<'l>,
    data: &CatchDifficultyAttributes,
) -> Result<JObject<'l>> {
    let class = env.find_class("org/spring/osu/extended/rosu/JniDifficultyAttributes")?;
    let jclass = class.as_raw();
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
        let class = unsafe { JClass::from_raw(jclass) };
        let method = env.get_static_method_id(
            class,
            "createCatch",
            "(DDIIIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let class = unsafe { JClass::from_raw(jclass) };
    let obj = unsafe { env.call_static_method_unchecked(class, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
}

pub fn generate_difficulty_attributes_mania<'l>(
    env: &mut JNIEnv<'l>,
    data: &ManiaDifficultyAttributes,
) -> Result<JObject<'l>> {
    let class = env.find_class("org/spring/osu/extended/rosu/JniDifficultyAttributes")?;
    let jclass = class.as_raw();
    let class = unsafe { JClass::from_raw(jclass) };
    let args = &[
        jvalue { d: data.stars },
        jvalue { d: data.hit_window },
        jvalue {
            i: data.n_objects as i32,
        },
        jvalue {
            i: data.max_combo as i32,
        },
        jvalue {
            b: if data.is_convert { 1i8 } else { 0i8 },
        },
    ];

    let method = get_jni_static_method_id(DIFFICULTY_MANAI, || {
        let class = unsafe { JClass::from_raw(jclass) };
        let method = env.get_static_method_id(
            class,
            "createMania",
            "(DDIIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
        )?;
        Ok(method)
    })?;
    let obj = unsafe { env.call_static_method_unchecked(class, method, ReturnType::Object, args)? };
    Ok(obj.l()?)
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
    let difficulty_ptr = get_object_ptr(env, this)?;
    let difficulty = to_status_use::<Difficulty>(difficulty_ptr)?;
    let attr = difficulty.calculate(beatmap);
    let obj = generate_difficulty_attributes(env, &attr)?;
    let ptr = match attr {
        DifficultyAttributes::Osu(data) => { to_ptr(data) }
        DifficultyAttributes::Taiko(data) => { to_ptr(data) }
        DifficultyAttributes::Catch(data) => { to_ptr(data) }
        DifficultyAttributes::Mania(data) => { to_ptr(data) }
    };
    set_object_ptr(env, &obj, ptr)?;
    Ok(obj.into_raw())
}
