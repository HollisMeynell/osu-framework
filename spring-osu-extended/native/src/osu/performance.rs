use super::java_fu::{get_object_ptr, release_object, set_object_ptr};
use crate::java::cache_key::*;
use crate::java::{get_jni_class, get_jni_static_method_id};
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
use rosu_pp::{Beatmap, Difficulty, GameMods, Performance};
use std::ops::Not;

struct PerformanceValue {
    mode: Option<GameMode>,
    mods: Option<GameMods>,
    difficulty: Option<Difficulty>,
    passed_objects: Option<u32>,
    clock_rate: Option<f64>,
    ar: Option<(f32, bool)>,
    od: Option<(f32, bool)>,
    cs: Option<(f32, bool)>,
    hp: Option<(f32, bool)>,
    hardrock_offsets: Option<bool>,
    state: Option<ScoreState>,
    accuracy: Option<f64>,
    misses: Option<u32>,
    combo: Option<u32>,
    hitresult_priority: Option<HitResultPriority>,
    lazer: Option<bool>,
    large_tick_hits: Option<u32>,
    small_tick_hits: Option<u32>,
    slider_end_hits: Option<u32>,
    n300: Option<u32>,
    n100: Option<u32>,
    n50: Option<u32>,
    n_katu: Option<u32>,
    n_geki: Option<u32>,
}

impl Default for PerformanceValue {
    fn default() -> Self {
        Self {
            mode: None,
            mods: None,
            difficulty: None,
            passed_objects: None,
            clock_rate: None,
            ar: None,
            od: None,
            cs: None,
            hp: None,
            hardrock_offsets: None,
            state: None,
            accuracy: None,
            misses: None,
            combo: None,
            hitresult_priority: None,
            lazer: None,
            large_tick_hits: None,
            small_tick_hits: None,
            slider_end_hits: None,
            n300: None,
            n100: None,
            n50: None,
            n_katu: None,
            n_geki: None,
        }
    }
}

pub(crate) struct PerformanceSetter<'map> {
    value: Option<PerformanceValue>,
    cache: Option<Performance<'map>>,
    changed: bool,
}

impl<'map> PerformanceSetter<'map> {
    fn get_performance(&mut self) -> Result<&mut Performance<'map>> {
        let performance = match self.get_cache() {
            Some(performance) => {
                performance
            }
            None => {
                return Err("performance is released".into());
            }
        };
        Ok(self.cache.insert(performance))
    }

    fn try_to_performance(&mut self) -> Result<Performance<'map>> {
        match self.get_cache() {
            None => {
                Err("performance is released".into())
            }
            Some(performance) => {
                Ok(performance)
            }
        }
    }

    fn get_cache(&mut self) -> Option<Performance<'map>>{
        let mut performance = if let Some(data) = self.cache.take() {
            data
        } else {
            return None;
        };

        if self.changed.not() {
            return Some(performance);
        }

        let values = self.value.get_or_insert_with(PerformanceValue::default);
        macro_rules! set_performance {
            ($($key:ident,)+) => {$(
                if let Some(v) = values.$key {
                    performance = performance.$key(v);
                }
            )+};
            ($(+$key:ident,)+) => {$(
                if let Some((v, with_mods)) = values.$key {
                    performance = performance.$key(v, with_mods);
                }
            )+};
            ($(>$key:ident,)+) => {$(
                if let Some(v) = &values.$key {
                    performance = performance.$key(v.clone());
                }
            )+};
        }
        set_performance! {
            passed_objects,
            clock_rate,hardrock_offsets,
            accuracy,
            misses,
            combo,
            lazer,
            large_tick_hits,
            slider_end_hits,
            n300,
            n100,
            n50,
            n_katu,
            n_geki,
        }
        set_performance! {
            >difficulty,
            >state,
            >hitresult_priority,
        }
        set_performance! {
            +ar,
            +od,
            +cs,
            +hp,
        }
        if let Some(mode) = values.mode {
            performance = performance.mode_or_ignore(mode);
        }
        if let Some(mods) = &values.mods {
            performance = performance.mods(mods.clone());
        }
        Some(performance)
    }
}

pub fn generate_state(env: &mut JNIEnv, obj: &JObject) -> Result<jobject> {
    let ptr = get_object_ptr(env, obj)?;
    let performance = to_status_use::<PerformanceSetter>(ptr)?;
    let score_state = performance.get_performance()?.generate_state();
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
            i: score_state.osu_small_tick_hits as jint,
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
            "(IIIIIIIIII)Lorg/spring/osu/extended/rosu/JniScoreState;",
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
        osu_small_tick_hits: array.get_i32() as u32,
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
            let setter = PerformanceSetter {
                value: None,
                cache: Some(performance),
                changed: false,
            };
            let ptr = to_ptr(setter);
            set_object_ptr(env, this, ptr)?;
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
            let performance = Performance::new(from.clone()).state(state.clone());
            let mut value = PerformanceValue::default();
            value.state = Some(state);
            let setter = PerformanceSetter {
                value: Some(value),
                cache: Some(performance),
                changed: false,
            };
            let ptr = to_ptr(setter);
            set_object_ptr(env, this, ptr)?;
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
    f: impl FnOnce(&mut PerformanceValue),
) -> Result<()> {
    let ptr = get_object_ptr(env, this)?;
    let setter = to_status_use::<PerformanceSetter>(ptr)?;
    let value = setter.value.get_or_insert_with(PerformanceValue::default);
    f(value);
    setter.changed = true;
    Ok(())
}

macro_rules! set_state {
    ($($fx:ident($f:ident);)+) => { $(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value:jint) -> Result<()> {
            set_performance_attr(env, this, |v| {
                v.$f = Some(value as u32)
            })
        }
    )+};
    ($($fx:ident[$f:ident:$t:ty];)+) => { $(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value:$t) -> Result<()> {
            set_performance_attr(env, this, |v| {
                v.$f = Some(value)
            })
        }
    )+};
    ($($fx:ident{$f:ident};)+) => { $(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: f32, with_mods: bool) -> Result<()> {
            set_performance_attr(env, this, |v| {
                v.$f = Some((value, with_mods))
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
    set_performance_small_tick(small_tick_hits);
    set_performance_slider_ends(slider_end_hits);
    set_performance_passed_objects(passed_objects);
}

set_state! {
    set_performance_is_lazer[lazer:bool];
    set_performance_is_hardrock[hardrock_offsets:bool];
    set_performance_clock_rate[clock_rate:f64];
    set_performance_accuracy[accuracy:f64];
    set_performance_hitresult_priority[hitresult_priority:HitResultPriority];
}

set_state! {
    set_performance_ar{ar};
    set_performance_od{od};
    set_performance_cs{cs};
    set_performance_hp{hp};
}

pub fn set_performance_game_mode(env: &mut JNIEnv, this: &JObject, mode: jbyte) -> Result<()> {
    let mode = GameMode::from(mode as u8);
    set_performance_attr(env, this, |v| v.mode = Some(mode))
}
pub fn set_performance_mods_bitflag(env: &mut JNIEnv, this: &JObject, legacy: jint) -> Result<()> {
    let legacy = get_mods_from_java!(legacy);
    set_performance_attr(env, this, |v| v.mods = Some(legacy))
}

pub fn set_performance_mods_lazer(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    lazer: &JString,
) -> Result<()> {
    let lazer = get_mods_from_java!(env, mode, lazer)?;
    set_performance_attr(env, this, |v| v.mods = Some(lazer))
}

pub fn set_performance_mods_mix(
    env: &mut JNIEnv,
    this: &JObject,
    mode: jbyte,
    legacy: jint,
    lazer: &JString,
) -> Result<()> {
    let all = get_mods_from_java!(env, mode, legacy, lazer)?;
    set_performance_attr(env, this, |v| v.mods = Some(all))
}

pub fn set_performance_state(env: &mut JNIEnv, this: &JObject, state: &JByteArray) -> Result<()> {
    let state = parse_java_state(env, state)?;
    set_performance_attr(env, this, |v| v.state = Some(state))?;
    Ok(())
}

pub fn set_performance_difficulty(
    env: &mut JNIEnv,
    this: &JObject,
    difficulty: jlong,
) -> Result<()> {
    let ptr = difficulty;
    let difficulty_setter = to_status_use::<DifficultySetter>(ptr)?;
    set_performance_attr(env, this, |setter| {
        setter.difficulty = Some(difficulty_setter.get_difficulty().clone())
    })?;
    Ok(())
}

pub(super) fn attribute_to_object(env: &mut JNIEnv, attr: PerformanceAttributes) -> Result<jclass> {
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

pub fn calculate_performance(env: &mut JNIEnv, this: &JObject) -> Result<jclass> {
    let ptr = get_object_ptr(env, this)?;
    release_object(env, this)?;
    let mut setter = to_status::<PerformanceSetter>(ptr)?;
    let attr = setter.try_to_performance()?.calculate();
    attribute_to_object(env, attr)
}
