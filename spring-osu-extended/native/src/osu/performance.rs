use super::java_fu::{get_object_ptr, set_object_ptr};
use crate::java::cache_key::*;
use crate::java::{get_jni_class, get_jni_field_id, get_jni_static_method_id};
use crate::osu::difficulty::*;
use crate::{get_mods_from_java, to_ptr, to_status, to_status_use, Result};
use jni::objects::{JClass, JObject, JString};
use jni::signature::{Primitive, ReturnType};
use jni::sys::{jbyte, jclass, jint, jlong, jobject, jvalue};
use jni::JNIEnv;
use rosu_pp::any::{HitResultPriority, PerformanceAttributes, ScoreState};
use rosu_pp::catch::CatchDifficultyAttributes;
use rosu_pp::mania::ManiaDifficultyAttributes;
use rosu_pp::model::mode::GameMode;
use rosu_pp::osu::OsuDifficultyAttributes;
use rosu_pp::taiko::TaikoDifficultyAttributes;
use rosu_pp::{Beatmap, Difficulty, Performance};

pub const PERFORMANCE_FIELD_MAX_COMBO: &str = "maxCombo";
pub const PERFORMANCE_FIELD_LARGE_TICK_HITS: &str = "largeTickHits";
pub const PERFORMANCE_FIELD_SLIDER_END_HITS: &str = "sliderEndHits";
pub const PERFORMANCE_FIELD_N_GEKI: &str = "geki";
pub const PERFORMANCE_FIELD_N_KATU: &str = "katu";
pub const PERFORMANCE_FIELD_N300: &str = "n300";
pub const PERFORMANCE_FIELD_N100: &str = "n100";
pub const PERFORMANCE_FIELD_N50: &str = "n50";
pub const PERFORMANCE_FIELD_MISSES: &str = "misses";

pub fn generate_state(env: &mut JNIEnv, obj: &JObject) -> Result<jobject> {
    let ptr = get_object_ptr(env, obj)?;
    let performance = to_status_use::<Performance>(ptr)?;
    let data = performance.generate_state();
    let args = &[
        jvalue {
            i: data.max_combo as jint,
        },
        jvalue {
            i: data.osu_large_tick_hits as jint,
        },
        jvalue {
            i: data.slider_end_hits as jint,
        },
        jvalue {
            i: data.n_geki as jint,
        },
        jvalue {
            i: data.n_katu as jint,
        },
        jvalue {
            i: data.n300 as jint,
        },
        jvalue {
            i: data.n100 as jint,
        },
        jvalue {
            i: data.n50 as jint,
        },
        jvalue {
            i: data.misses as jint,
        },
    ];
    let jclass = get_jni_class(PERFORMANCE_STATE_CLASS, || {
        let class = env.find_class("org/spring/osu/extended/rosu/JniPerformance")?;
        let class = env.new_global_ref(class)?;
        Ok(class.as_raw() as jclass)
    })?;
    let method = get_jni_static_method_id(PERFORMANCE_STATE_CREATE, || {
        let class = unsafe { JClass::from_raw(jclass) };
        let method = env.get_static_method_id(
            class,
            "createState",
            "(IIIIIIIII)Lorg/spring/osu/extended/rosu/JniScoreState;",
        )?;
        Ok(method)
    })?;
    let class = unsafe { JClass::from_raw(jclass) };
    let object =
        unsafe { env.call_static_method_unchecked(class, method, ReturnType::Object, args)? }
            .l()?;
    Ok(object.into_raw())
}

fn parse_java_state(env: &mut JNIEnv, this: &JObject) -> Result<ScoreState> {
    let mut state = ScoreState::default();

    macro_rules! get_state_field {
        ($([$key:expr]$jf:expr=>$rf:ident,)+) => {$(
            let field_id = get_jni_field_id($key, || {
                let class = env.get_object_class(this)?;
                let field_id = env.get_field_id(class, $jf, "I")?;
                Ok(field_id)
            })?;
            state.$rf = env.get_field_unchecked(
                this,
                field_id,
                ReturnType::Primitive(Primitive::Int)
            )?.i()? as u32;
        )+};
    }
    get_state_field! {
        [PERFORMANCE_STATE_MAX_COMBO]
        PERFORMANCE_FIELD_MAX_COMBO         => max_combo,
        [PERFORMANCE_STATE_LARGE_TICK_HITS]
        PERFORMANCE_FIELD_LARGE_TICK_HITS   => osu_large_tick_hits,
        [PERFORMANCE_STATE_SLIDER_END_HITS]
        PERFORMANCE_FIELD_SLIDER_END_HITS   => slider_end_hits,
        [PERFORMANCE_STATE_N_GEKI]
        PERFORMANCE_FIELD_N_GEKI            => n_geki,
        [PERFORMANCE_STATE_N_KATU]
        PERFORMANCE_FIELD_N_KATU            => n_katu,
        [PERFORMANCE_STATE_N300]
        PERFORMANCE_FIELD_N300              => n300,
        [PERFORMANCE_STATE_N100]
        PERFORMANCE_FIELD_N100              => n100,
        [PERFORMANCE_STATE_N50]
        PERFORMANCE_FIELD_N50               => n50,
        [PERFORMANCE_STATE_MISSES]
        PERFORMANCE_FIELD_MISSES            => misses,
    }
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
            state: &JObject,
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

pub fn set_performance_state(env: &mut JNIEnv, this: &JObject, state: &JObject) -> Result<()> {
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

pub fn calculate_performance(env: &mut JNIEnv, this: &JObject, mode: jint) -> Result<jclass> {
    let ptr = get_object_ptr(env, this)?;
    set_object_ptr(env, this, 0)?;
    let performance = to_status::<Performance>(ptr)?;
    let mode = GameMode::from(mode as u8);
    let attr = performance.mode_or_ignore(mode).calculate();

    let jclass = get_jni_class(PERFORMANCE_ATTR_CLASS, || {
        let class = env.find_class("org/spring/osu/extended/rosu/JniPerformanceAttributes")?;
        let class = env.new_global_ref(class)?;
        Ok(class.as_raw() as jclass)
    })?;

    let obj: Result<JObject> = match attr {
        PerformanceAttributes::Osu(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_OSU, || {
                let class = unsafe { JClass::from_raw(jclass) };
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
            let class = unsafe { JClass::from_raw(jclass) };
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
        PerformanceAttributes::Taiko(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_TAIKO, || {
                let class = unsafe { JClass::from_raw(jclass) };
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
            let class = unsafe { JClass::from_raw(jclass) };
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
        PerformanceAttributes::Catch(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_CATCH, || {
                let class = unsafe { JClass::from_raw(jclass) };
                let field = env.get_static_method_id(
                    class,
                    "createCatch",
                    "(DLorg/spring/osu/extended/rosu/CatchDifficultyAttributes;)Lorg/spring/osu/extended/rosu/JniPerformanceAttributes;",
                )?;
                Ok(field)
            })?;
            let attr = generate_difficulty_attributes_catch(env, &data.difficulty)?.as_raw();
            let args = &[jvalue { d: data.pp }, jvalue { l: attr }];
            let class = unsafe { JClass::from_raw(jclass) };
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
        PerformanceAttributes::Mania(data) => {
            let method = get_jni_static_method_id(PERFORMANCE_ATTR_MANAI, || {
                let class = unsafe { JClass::from_raw(jclass) };
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
            let class = unsafe { JClass::from_raw(jclass) };
            let obj = unsafe {
                env.call_static_method_unchecked(class, method, ReturnType::Object, args)?
            };
            Ok(obj.l()?)
        }
    };
    Ok(obj?.as_raw())
}
