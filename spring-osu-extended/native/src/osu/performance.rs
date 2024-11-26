use super::java_fu::{get_object_ptr, release_object, set_object_ptr};
use crate::java::cache_key::*;
use crate::java::{get_jni_class, get_jni_method_id, get_jni_static_method_id};
use crate::osu::difficulty::*;
use crate::{get_class, get_mods_from_java, to_ptr, to_status, to_status_use, Result};
use bytes::{Buf, Bytes};
use jni::objects::{JByteArray, JClass, JObject, JString};
use jni::signature::ReturnType;
use jni::sys::{jbyte, jclass, jint, jlong, jobject, jvalue};
use jni::JNIEnv;
use rosu_pp::any::{HitResultPriority, PerformanceAttributes, ScoreState};
use rosu_pp::catch::CatchDifficultyAttributes;
use rosu_pp::mania::ManiaDifficultyAttributes;
use rosu_pp::model::mode::GameMode;
use rosu_pp::osu::OsuDifficultyAttributes;
use rosu_pp::taiko::TaikoDifficultyAttributes;
use rosu_pp::{Beatmap, Difficulty, GradualPerformance, Performance};

pub fn generate_state(env: &mut JNIEnv, obj: &JObject) -> Result<jobject> {
    let ptr = get_object_ptr(env, obj)?;
    let performance = to_status_use::<Performance>(ptr)?;
    let score_state = performance.generate_state();
    generate_java_state(env, score_state)
}

pub fn generate_java_state(env: &mut JNIEnv, score_state: ScoreState) -> Result<jobject> {
    let args = &[
        jvalue {
            i: score_state.max_combo as jint,
        },
        jvalue {
            i: score_state.osu_large_tick_hits as jint,
        },
        jvalue {
            i: score_state.slider_end_hits as jint,
        },
        jvalue {
            i: score_state.n_geki as jint,
        },
        jvalue {
            i: score_state.n_katu as jint,
        },
        jvalue {
            i: score_state.n300 as jint,
        },
        jvalue {
            i: score_state.n100 as jint,
        },
        jvalue {
            i: score_state.n50 as jint,
        },
        jvalue {
            i: score_state.misses as jint,
        },
    ];
    let global = get_jni_class(
        PERFORMANCE_STATE_CLASS,
        env,
        "org/spring/osu/extended/rosu/JniScoreState",
    )?;
    let jclass = get_class!(global);
    let method = get_jni_static_method_id(PERFORMANCE_STATE_CREATE, || {
        let method = env.get_static_method_id(
            jclass,
            "create",
            "(IIIIIIIII)Lorg/spring/osu/extended/rosu/JniScoreState;",
        )?;
        Ok(method)
    })?;
    let object =
        unsafe { env.call_static_method_unchecked(jclass, method, ReturnType::Object, args)? }
            .l()?;
    Ok(object.into_raw())
}

pub fn parse_java_state(env: &mut JNIEnv, state: &JByteArray) -> Result<ScoreState> {
    let mut array = Bytes::from(env.convert_byte_array(state)?);

    let state = ScoreState {
        max_combo: array.get_i32() as u32,
        osu_large_tick_hits: array.get_i32() as u32,
        slider_end_hits: array.get_i32() as u32,
        n_geki: array.get_i32() as u32,
        n_katu: array.get_i32() as u32,
        n300: array.get_i32() as u32,
        n100: array.get_i32() as u32,
        n50: array.get_i32() as u32,
        misses: array.get_i32() as u32,
    };

    Ok(state)
}

macro_rules! init_performance {
    ($(-$fx:ident($ty:ty);)+) => {$(
        pub fn $fx(
            env: &mut JNIEnv,
            this: &JObject,
            ptr: jlong,
        ) -> Result<()> {
            let from = to_status_use::<$ty>(ptr)?;
            let performance = Performance::new(from.clone());
            let performance_ptr = to_ptr(performance);
            set_object_ptr(env, this, performance_ptr)?;
            Ok(())
        }
    )+};
    ($(+$fx:ident($ty:ty);)+) => {$(
        pub fn $fx(
            env: &mut JNIEnv,
            this: &JObject,
            ptr: jlong,
            state: &JByteArray,
        ) -> Result<()> {
            let from = to_status_use::<$ty>(ptr)?;
            let state = parse_java_state(env, state)?;
            let performance = Performance::new(from.clone()).state(state);
            let performance_ptr = to_ptr(performance);
            set_object_ptr(env, this, performance_ptr)?;
            Ok(())
        }
    )+};
}

init_performance! {
    -init_performance_by_beatmap(Beatmap);
    -init_performance_by_osu_attributes(OsuDifficultyAttributes);
    -init_performance_by_taiko_attributes(TaikoDifficultyAttributes);
    -init_performance_by_catch_attributes(CatchDifficultyAttributes);
    -init_performance_by_mania_attributes(ManiaDifficultyAttributes);
}
init_performance! {
    +init_performance_by_beatmap_with_state(Beatmap);
    +init_performance_by_osu_attributes_with_state(OsuDifficultyAttributes);
    +init_performance_by_taiko_attributes_with_state(TaikoDifficultyAttributes);
    +init_performance_by_catch_attributes_with_state(CatchDifficultyAttributes);
    +init_performance_by_mania_attributes_with_state(ManiaDifficultyAttributes);
}

#[inline]
fn set_performance_attr(
    env: &mut JNIEnv,
    this: &JObject,
    f: impl FnOnce(Box<Performance>) -> Performance,
) -> Result<()> {
    let ptr = get_object_ptr(env, this)?;
    let performance = to_status(ptr)?;
    let performance = f(performance);
    let ptr = to_ptr(performance);
    set_object_ptr(env, this, ptr)?;
    Ok(())
}

macro_rules! set_state {
    ($($fx:ident($f:ident);)+) => { $(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value:jint) -> Result<()> {
            set_performance_attr(env, this, |performance| {
                performance.$f(value as u32)
            })
        }
    )+};
    ($($fx:ident[$f:ident:$t:ty];)+) => { $(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value:$t) -> Result<()> {
            set_performance_attr(env, this, |performance| {
                performance.$f(value)
            })
        }
    )+};
}

set_state! {
    set_performance_combo(combo);
    set_performance_geki(n_geki);
    set_performance_katu(n_katu);
    set_performance_n300(n300);
    set_performance_n100(n100);
    set_performance_n50(n50);
    set_performance_misses(misses);
    set_performance_large_tick(large_tick_hits);
    set_performance_slider_ends(n_slider_ends);
    set_performance_passed_objects(passed_objects);
}

set_state! {
    set_performance_is_lazer[lazer:bool];
    set_performance_is_hardrock[lazer:bool];
    set_performance_clock_rate[clock_rate:f64];
    set_performance_accuracy[accuracy:f64];
    set_performance_hitresult_priority[hitresult_priority:HitResultPriority];
}

pub fn set_performance_mods_bitflag(env: &mut JNIEnv, this: &JObject, legacy: jint) -> Result<()> {
    let legacy = get_mods_from_java!(legacy);
    set_performance_attr(env, this, |performance| performance.mods(legacy))
}

pub fn set_performance_mods_lazer(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    lazer: &JString,
) -> Result<()> {
    let lazer = get_mods_from_java!(env, mode, lazer)?;
    set_performance_attr(env, this, |performance| performance.mods(lazer))
}

pub fn set_performance_mods_mix(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    legacy: jint,
    lazer: &JString,
) -> Result<()> {
    let all = get_mods_from_java!(env, mode, legacy, lazer)?;
    set_performance_attr(env, this, |performance| performance.mods(all))
}

pub fn set_performance_state(env: &mut JNIEnv, this: &JObject, state: &JByteArray) -> Result<()> {
    let state = parse_java_state(env, state)?;
    set_performance_attr(env, this, |performance| performance.state(state))?;
    Ok(())
}

pub fn set_performance_difficulty(
    env: &mut JNIEnv,
    this: &JObject,
    difficulty: jlong,
) -> Result<()> {
    let ptr = difficulty;
    let difficulty = to_status_use::<Difficulty>(ptr)?;
    set_performance_attr(env, this, |performance| {
        performance.difficulty(difficulty.clone())
    })?;
    Ok(())
}

fn attribute_to_object(env: &mut JNIEnv, attr: PerformanceAttributes) -> Result<jclass> {
    let global = get_jni_class(
        PERFORMANCE_ATTR_CLASS,
        env,
        "org/spring/osu/extended/rosu/JniPerformanceAttributes",
    )?;
    let class = get_class!(global);

    let obj: Result<JObject> = match attr {
        PerformanceAttributes::Osu(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_OSU, || {
                let field = env.get_static_method_id(
                    class,
                    "createOsu",
                    "(DDDDDDLorg/spring/osu/extended/rosu/OsuDifficultyAttributes;)Lorg/spring/osu/extended/rosu/JniPerformanceAttributes;",
                )?;
                Ok(field)
            })?;
            let attr = generate_difficulty_attributes_osu(env, &data.difficulty)?.as_raw();
            let args = &[
                jvalue { d: data.pp },
                jvalue { d: data.pp_acc },
                jvalue { d: data.pp_aim },
                jvalue {
                    d: data.pp_flashlight,
                },
                jvalue { d: data.pp_speed },
                jvalue {
                    d: data.effective_miss_count,
                },
                jvalue { l: attr },
            ];
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
        PerformanceAttributes::Taiko(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_TAIKO, || {
                let field = env.get_static_method_id(
                    class,
                    "createTaiko",
                    "(DDDDDLorg/spring/osu/extended/rosu/TaikoDifficultyAttributes;)Lorg/spring/osu/extended/rosu/JniPerformanceAttributes;",
                )?;
                Ok(field)
            })?;
            let attr = generate_difficulty_attributes_taiko(env, &data.difficulty)?.as_raw();
            let args = &[
                jvalue { d: data.pp },
                jvalue { d: data.pp_acc },
                jvalue {
                    d: data.pp_difficulty,
                },
                jvalue {
                    d: data.effective_miss_count,
                },
                jvalue {
                    d: data.estimated_unstable_rate.unwrap_or(-1f64),
                },
                jvalue { l: attr },
            ];
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
        PerformanceAttributes::Catch(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_CATCH, || {
                let field = env.get_static_method_id(
                    class,
                    "createCatch",
                    "(DLorg/spring/osu/extended/rosu/CatchDifficultyAttributes;)Lorg/spring/osu/extended/rosu/JniPerformanceAttributes;",
                )?;
                Ok(field)
            })?;
            let attr = generate_difficulty_attributes_catch(env, &data.difficulty)?.as_raw();
            let args = &[jvalue { d: data.pp }, jvalue { l: attr }];
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
        PerformanceAttributes::Mania(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_MANAI, || {
                let field = env.get_static_method_id(
                    class,
                    "createMania",
                    "(DDLorg/spring/osu/extended/rosu/ManiaDifficultyAttributes;)Lorg/spring/osu/extended/rosu/JniPerformanceAttributes;",
                )?;
                Ok(field)
            })?;
            let attr = generate_difficulty_attributes_mania(env, &data.difficulty)?.as_raw();
            let args = &[
                jvalue { d: data.pp },
                jvalue {
                    d: data.pp_difficulty,
                },
                jvalue { l: attr },
            ];
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
    };

    Ok(obj?.as_raw())
}

pub fn calculate_performance(env: &mut JNIEnv, this: &JObject, mode: jint) -> Result<jclass> {
    let ptr = get_object_ptr(env, this)?;
    release_object(env, this)?;
    let performance = to_status::<Performance>(ptr)?;
    let mode = GameMode::from(mode as u8);
    let attr = performance.mode_or_ignore(mode).calculate();
    attribute_to_object(env, attr)
}

pub fn gradual_performance(
    env: &mut JNIEnv,
    this: &JObject,
    beatmap_ptr: jlong,
) -> Result<jobject> {
    let beatmap = to_status_use(beatmap_ptr)?;
    let difficulty_ptr = get_object_ptr(env, this)?;
    let difficulty = to_status::<Difficulty>(difficulty_ptr)?;
    release_object(env, this)?;

    let gradual = difficulty.gradual_performance(beatmap);
    let global = get_jni_class(
        GRADUAL_PERFORMANCE_CLASS,
        env,
        "org/spring/osu/extended/rosu/JniGradualPerformance",
    )?;
    let class = get_class!(global);
    let method = get_jni_method_id(GRADUAL_PERFORMANCE_INIT, || {
        let method = env.get_method_id(
            class,
            "<init>",
            "(Lorg/spring/osu/extended/rosu/JniScoreState;)V",
        )?;
        Ok(method)
    })?;

    let state = generate_java_state(env, ScoreState::new())?;
    let obj = unsafe { env.new_object_unchecked(class, method, &[jvalue { l: state }]) }?;
    set_object_ptr(env, &obj, to_ptr(gradual))?;
    Ok(obj.into_raw())
}

pub fn gradual_len(env: &mut JNIEnv, this: &JObject) -> Result<jint> {
    let ptr = get_object_ptr(env, this)?;
    let gradual = to_status_use::<GradualPerformance>(ptr)?;
    Ok(gradual.len() as i32)
}

fn gradual_action(
    env: &mut JNIEnv,
    this: &JObject,
    state: &JByteArray,
    fun: impl FnOnce(&mut GradualPerformance, ScoreState) -> Option<PerformanceAttributes>,
) -> Result<jobject> {
    let state = parse_java_state(env, state)?;
    let ptr = get_object_ptr(env, this)?;
    let gradual = to_status_use::<GradualPerformance>(ptr)?;
    let attr = fun(gradual, state);
    let obj = match attr {
        Some(data) => attribute_to_object(env, data)?,
        None => JObject::null().into_raw(),
    };
    Ok(obj)
}

pub fn gradual_next(env: &mut JNIEnv, this: &JObject, state: &JByteArray) -> Result<jobject> {
    gradual_action(env, this, state, |gradual, state| gradual.next(state))
}

pub fn gradual_last(env: &mut JNIEnv, this: &JObject, state: &JByteArray) -> Result<jobject> {
    gradual_action(env, this, state, |gradual, state| gradual.last(state))
}

pub fn gradual_nth(
    env: &mut JNIEnv,
    this: &JObject,
    state: &JByteArray,
    n: jint,
) -> Result<jobject> {
    gradual_action(env, this, state, |gradual, state| {
        gradual.nth(state, n as usize)
    })
}
