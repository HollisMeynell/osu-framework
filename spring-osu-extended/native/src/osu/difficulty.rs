use super::java_fu::{get_object_ptr, set_object_ptr};
use crate::java::{cache_key::*, get_jni_class, get_jni_static_method_id};
use crate::{get_class, get_mods_from_java, to_ptr, to_status_use, Result};
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

struct DifficultyValue {
    ar: Option<(f32, bool)>,
    od: Option<(f32, bool)>,
    cs: Option<(f32, bool)>,
    hp: Option<(f32, bool)>,
    lazer: Option<bool>,
    hardrock_offsets: Option<bool>,
    passed_objects: Option<u32>,
    clock_rate: Option<f64>,
}

impl Default for DifficultyValue {
    fn default() -> Self {
        Self {
            ar: None,
            od: None,
            cs: None,
            hp: None,
            lazer: None,
            hardrock_offsets: None,
            passed_objects: None,
            clock_rate: None,
        }
    }
}

pub(crate) struct DifficultySetter {
    cache: Option<Difficulty>,
    values: Option<DifficultyValue>,
    mods: Option<GameMods>,
    changed: bool,
}

impl DifficultySetter {
    pub fn get_difficulty(&mut self) -> &mut Difficulty {
        let difficulty = self.get_cache();
        self.cache.insert(difficulty)
    }

    pub fn to_difficulty(mut self) -> Difficulty {
        self.get_cache()
    }

    fn get_cache(&mut self) -> Difficulty {
        let setter = self;
        let mut difficulty = if let Some(difficulty) = setter.cache.take() {
            difficulty
        } else {
            Difficulty::new()
        };
        if !setter.changed {
            return difficulty;
        }
        if setter.values.is_some() || setter.mods.is_some() {
            if let Some(values) = &setter.values {
                macro_rules! set_value {
                    (>$key:ident) => {
                        if let Some((value, with_mode)) = values.$key {
                            difficulty = difficulty.$key(value, with_mode);
                        }
                    };
                    ($key:ident) => {
                        if let Some(value) = values.$key {
                            difficulty = difficulty.$key(value);
                        }
                    };
                }
                set_value!(>ar);
                set_value!(>od);
                set_value!(>cs);
                set_value!(>hp);
                set_value!(lazer);
                set_value!(hardrock_offsets);
                set_value!(passed_objects);
                set_value!(clock_rate);
            }
            if let Some(mods) = &setter.mods {
                difficulty = difficulty.mods(mods.clone());
            }
        };
        setter.changed = false;
        difficulty
    }
}

macro_rules! set_state {
    ($($fx:ident($f:ident);)+) => {$(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: f32, with_mods: bool) -> Result<()> {
            let ptr = get_object_ptr(env, this)?;
            let setter = to_status_use::<DifficultySetter>(ptr)?;
            let values = setter.values.get_or_insert_with(DifficultyValue::default);
            values.$f = Some((value, with_mods));
            setter.changed = true;
            Ok(())
        }
    )+};
    ($($fx:ident[$f:ident:$t:ty];)+) => {$(
        pub fn $fx(env: &mut JNIEnv, this: &JObject, value: $t) -> Result<()> {
            let ptr = get_object_ptr(env, this)?;
            let setter = to_status_use::<DifficultySetter>(ptr)?;
            let values = setter.values.get_or_insert_with(DifficultyValue::default);
            values.$f = Some(value);
            setter.changed = true;
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
    let setter = to_status_use::<DifficultySetter>(ptr)?;
    setter.mods = Some(mods);
    setter.changed = true;
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
    let mut setter = DifficultyValue::default();
    setter.lazer = Some(is_lazer);
    if !is_all_null {
        macro_rules! set_field {
            ($name:ident) => {
                if $name >= -9f32 {
                    setter.$name = Some(($name, with_mods));
                }
            };
        }
        set_field!(ar);
        set_field!(od);
        set_field!(cs);
        set_field!(hp);
        if clock_rate >= 0f64 {
            setter.clock_rate = Some(clock_rate);
        }
    }
    let setter = DifficultySetter {
        values: Some(setter),
        cache: None,
        mods: None,
        changed: true,
    };
    let ptr = to_ptr(setter);
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
        jvalue { d: data.od },
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
        jvalue { d: data.peak },
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
        jvalue { d: data.hit_window },
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
            "(DDiIIZ)Lorg/spring/osu/extended/rosu/JniDifficultyAttributes;",
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
    let setter = to_status_use::<DifficultySetter>(ptr)?;
    let difficulty = setter.get_difficulty();
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
