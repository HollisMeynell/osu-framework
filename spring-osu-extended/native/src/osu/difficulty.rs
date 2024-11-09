use std::cell::RefCell;
use std::rc::Rc;
use crate::osu::java_fu::{get_object_ptr, set_object_ptr};
use crate::{to_ptr, to_status, to_status_use, Result};
use jni::objects::{JClass, JObject, JValue, JValueGen};
use jni::sys::{jboolean, jbyte, jclass, jdouble, jfloat, jobject, jvalue};
use jni::{objects, JNIEnv};
use rosu_pp::any::DifficultyAttributes;
use rosu_pp::Difficulty;
use crate::java::{get_jni_class, get_jni_field_id, get_jni_method_id};

static DIFFICULTY_OSU: &str = "dm_init_o";
static DIFFICULTY_TAIKO: &str = "dm_init_t";
static DIFFICULTY_CATCH: &str = "dm_init_c";
static DIFFICULTY_MANAI: &str = "dm_init_m";

macro_rules! set_state {
    ($fx:ident($f:ident)) => {
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: f32, is_lazer: bool) -> Result<()> {
            set_difficulty_attr(env, this, |difficulty| difficulty.$f(value, is_lazer))
        }
    };
    ($fx:ident[$f:ident]) => {
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: bool) -> Result<()> {
            set_difficulty_attr(env, this, |difficulty| difficulty.$f(value))
        }
    };
}
set_state!(set_difficulty_ar(ar));
set_state!(set_difficulty_cs(cs));
set_state!(set_difficulty_hp(hp));
set_state!(set_difficulty_od(od));
set_state!(set_difficulty_is_lazer[lazer]);
set_state!(set_difficulty_is_hardrock[hardrock_offsets]);

pub fn set_difficulty_clock_rate(env: &mut JNIEnv, this: &JObject, clock_rate: f64) -> Result<()> {
    set_difficulty_attr(env, this, |difficulty| difficulty.clock_rate(clock_rate))
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

pub fn difficulty_calculate(
    env: &mut JNIEnv,
    this: &JObject,
    beatmap: &JObject,
) -> Result<jobject> {
    let beatmap_ptr = get_object_ptr(env, beatmap)?;
    let beatmap = to_status_use(beatmap_ptr)?;
    let difficulty_ptr = get_object_ptr(env, this)?;
    let difficulty = to_status_use::<Difficulty>(difficulty_ptr)?;
    let attr = difficulty.calculate(beatmap);

    let (obj, ptr) = match attr {
        DifficultyAttributes::Osu(data) => {
            // let args = &[
            //     JValueGen::Double(data.aim),
            //     JValueGen::Double(data.speed),
            //     JValueGen::Double(data.flashlight),
            //     JValueGen::Double(data.slider_factor),
            //     JValueGen::Double(data.speed_note_count),
            //     JValueGen::Double(data.aim_difficult_strain_count),
            //     JValueGen::Double(data.speed_difficult_strain_count),
            //     JValueGen::Double(data.stars),
            //     JValueGen::Int(data.max_combo as i32),
            // ];
            // let object = env
            //     // .new_object("org/spring/osu/extended/rosu/OsuDifficultyAttributes", "(DDDDDDDDI)V", args)?;
            //     .call_static_method(
            //         "org/spring/osu/extended/rosu/JniDifficultyAttributes",
            //         "createOsu",
            //         "(DDDDDDDDI)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
            //         args,
            //     )?
            //     .l()?;
            // env.exception_describe()?;
            let args = &[
                jvalue { d: data.aim },
                jvalue { d: data.speed },
                jvalue { d: data.flashlight },
                jvalue { d: data.slider_factor },
                jvalue { d: data.speed_note_count },
                jvalue { d: data.aim_difficult_strain_count },
                jvalue { d: data.speed_difficult_strain_count },
                jvalue { d: data.stars },
                jvalue { i: data.max_combo as i32 },
            ];

            let class = env.find_class("org/spring/osu/extended/rosu/OsuDifficultyAttributes")?;
            let jclass = class.as_raw();
            let class = unsafe { JClass::from_raw(jclass) };
            let f = get_jni_method_id(DIFFICULTY_TAIKO, || {
                let c = class.as_raw();
                let class = unsafe { JClass::from_raw(jclass) };
                let f = env.get_method_id(class, "<init>", "(DDDDDDDDI)V")?;
                Ok(f)
            })?;
            let object;
            unsafe {
                object = env.new_object_unchecked(class, f, args)?;
            }
            (object, to_ptr(data))
        }
        DifficultyAttributes::Taiko(data) => {
            // let args = &[
            //     JValueGen::Double(data.stamina),
            //     JValueGen::Double(data.rhythm),
            //     JValueGen::Double(data.color),
            //     JValueGen::Double(data.peak),
            //     JValueGen::Double(data.great_hit_window),
            //     JValueGen::Double(data.ok_hit_window),
            //     JValueGen::Double(data.stars),
            //     JValueGen::Int(data.max_combo as i32),
            //     JValueGen::Bool(jboolean::from(data.is_convert)),
            // ];
            // let object = env
            //     .call_static_method(
            //         attr_static_class,
            //         "createTaiko",
            //         "(DDDDDDDIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
            //         args,
            //     )?
            //     .l()?;
            let args = &[
                jvalue { d: data.stamina },
                jvalue { d: data.rhythm },
                jvalue { d: data.color },
                jvalue { d: data.peak },
                jvalue { d: data.great_hit_window },
                jvalue { d: data.ok_hit_window },
                jvalue { d: data.stars },
                jvalue { i: data.max_combo as i32 },
                jvalue { b: if data.is_convert { 1i8 } else { 0i8 } },
            ];

            let class = env.find_class("org/spring/osu/extended/rosu/TaikoDifficultyAttributes")?;
            let jclass = class.as_raw();
            let class = unsafe { JClass::from_raw(jclass) };
            let f = get_jni_method_id(DIFFICULTY_TAIKO, || {
                let c = class.as_raw();
                let class = unsafe { JClass::from_raw(jclass) };
                let f = env.get_method_id(class, "<init>", "(DDDDDDDIZ)V")?;
                Ok(f)
            })?;
            let object;
            unsafe {
                object = env.new_object_unchecked(class, f, args)?;
            }
            (object, to_ptr(data))
        }
        DifficultyAttributes::Catch(data) => {
            // let args = &[
            //     JValueGen::Double(data.stars),
            //     JValueGen::Double(data.ar),
            //     JValueGen::Int(data.n_fruits as i32),
            //     JValueGen::Int(data.n_droplets as i32),
            //     JValueGen::Int(data.n_tiny_droplets as i32),
            //     JValueGen::Bool(jboolean::from(data.is_convert)),
            // ];
            // let object = env
            //     .call_static_method(
            //         attr_static_class,
            //         "createCatch",
            //         "(DDIIIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
            //         args,
            //     )?
            //     .l()?;
            let args = &[
                jvalue { d: data.stars },
                jvalue { d: data.ar },
                jvalue { i: data.n_fruits as i32 },
                jvalue { i: data.n_droplets as i32 },
                jvalue { i: data.n_tiny_droplets as i32 },
                jvalue { b: if data.is_convert { 1i8 } else { 0i8 } },
            ];

            let class = env.find_class("org/spring/osu/extended/rosu/CatchDifficultyAttributes")?;
            let jclass = class.as_raw();
            let class = unsafe { JClass::from_raw(jclass) };
            let f = get_jni_method_id(DIFFICULTY_CATCH, || {
                let c = class.as_raw();
                let class = unsafe { JClass::from_raw(jclass) };
                let f = env.get_method_id(class, "<init>", "(DDIIIZ)V")?;
                Ok(f)
            })?;
            let object;
            unsafe {
                object = env.new_object_unchecked(class, f, args)?;
            }
            (object, to_ptr(data))
        }
        DifficultyAttributes::Mania(data) => {
            /*
            let args = &[
                JValue::Double(data.stars),
                JValue::Double(data.hit_window),
                JValue::Int(data.n_objects as i32),
                JValue::Int(data.max_combo as i32),
                JValue::Bool(jboolean::from(data.is_convert)),
            ];
            let object = env.new_object(
                "org/spring/osu/extended/rosu/ManiaDifficultyAttributes",
                "(DDIIZ)V",
                args,
            )?;
           */

            let args = &[
                jvalue { d: data.stars },
                jvalue { d: data.hit_window },
                jvalue { i: data.n_objects as i32 },
                jvalue { i: data.max_combo as i32 },
                jvalue { b: if data.is_convert { 1i8 } else { 0i8 } },
            ];
            let class = env.find_class("org/spring/osu/extended/rosu/ManiaDifficultyAttributes")?;
            let jclass = class.as_raw();
            let class = unsafe { JClass::from_raw(jclass) };
            let f = get_jni_method_id(DIFFICULTY_MANAI, || {
                let c = class.as_raw();
                let class = unsafe { JClass::from_raw(jclass) };
                let f = env.get_method_id(class, "<init>", "(DDIIZ)V")?;
                Ok(f)
            })?;
            let object;
            unsafe {
                object = env.new_object_unchecked(class, f, args)?;
            }
            (object, to_ptr(data))
        }
    };
    set_object_ptr(env, &obj, ptr)?;
    Ok(obj.into_raw())
}