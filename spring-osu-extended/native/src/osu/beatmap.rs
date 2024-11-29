use crate::java::cache_key::*;
use crate::java::get_jni_field_id;
use crate::osu::java_fu::{get_bytes, get_object_ptr, set_object_ptr};
use crate::{to_ptr, to_status_use, Result};
use jni::objects::{JByteArray, JObject, JString, JValueGen};
use jni::sys::{jboolean, jbyte, JNI_TRUE};
use jni::JNIEnv;
use rosu_pp::model::mode::GameMode;
use rosu_pp::{Beatmap, GameMods};

pub fn get_beatmap_from_data(env: &mut JNIEnv, this: &JObject, map: JByteArray) -> Result<()> {
    let data = get_bytes(env, &map)?;
    let beatmap = Beatmap::from_bytes(&data)?;
    set_beatmap_field(env, this, &beatmap)?;
    let ptr = to_ptr(beatmap);
    set_object_ptr(env, this, ptr)?;
    Ok(())
}

pub fn get_beatmap_from_local(env: &mut JNIEnv, this: &JObject, path: &JString) -> Result<()> {
    let path: String = env.get_string(path)?.into();
    let beatmap = Beatmap::from_path(path)?;
    set_beatmap_field(env, this, &beatmap)?;
    let ptr = to_ptr(beatmap);
    set_object_ptr(env, this, ptr)?;
    Ok(())
}

pub fn set_beatmap_field(env: &mut JNIEnv, this: &JObject, map: &Beatmap) -> Result<()> {
    macro_rules! set_beatmap {
        ($key:ident[$name:literal, $ty:literal, $vl:ident]) => {
            let id = get_jni_field_id($key, || {
                let class = env.get_object_class(this)?;
                env.get_field_id(class, $name, $ty)
                    .or_else(|e| Err(e.into()))
            })?;
            env.set_field_unchecked(this, id, $vl)?;
        };
        ($key:ident($name:literal, $field:ident)) => {
            let id = get_jni_field_id($key, || {
                let class = env.get_object_class(this)?;
                env.get_field_id(class, $name, "F")
                    .or_else(|e| Err(e.into()))
            })?;
            env.set_field_unchecked(this, id, JValueGen::Float(map.$field))?;
        };
    }

    set_beatmap! { BEATMAP_FIELD_AR("ar", ar) }
    set_beatmap! { BEATMAP_FIELD_OD("od", od) }
    set_beatmap! { BEATMAP_FIELD_CS("cs", cs) }
    set_beatmap! { BEATMAP_FIELD_HP("hp", hp) }

    let mode_int = match map.mode {
        GameMode::Osu => 0,
        GameMode::Taiko => 1,
        GameMode::Catch => 2,
        GameMode::Mania => 3,
    };
    let mode = JValueGen::Int(mode_int);
    let bpm = JValueGen::Double(map.bpm());
    let sm = JValueGen::Double(map.slider_multiplier);
    let st = JValueGen::Double(map.slider_tick_rate);
    let objects = JValueGen::Int(map.hit_objects.len() as i32);
    set_beatmap! { BEATMAP_FIELD_MD["modeValue", "I", mode]}
    set_beatmap! { BEATMAP_FIELD_BPM["bpm", "D",bpm]}
    set_beatmap! { BEATMAP_FIELD_SM["sliderMultiplier", "D",sm]}
    set_beatmap! { BEATMAP_FIELD_ST["sliderTickTate", "D",st]}
    set_beatmap! { BEATMAP_FIELD_OBJS["objects", "I", objects]}
    Ok(())
}

pub fn try_to_convert(env: &mut JNIEnv, this: &JObject, mode_byte: jbyte) -> Result<jboolean> {
    let ptr = get_object_ptr(env, this)?;
    let beatmap = to_status_use::<Beatmap>(ptr)?;
    let mode = GameMode::from(mode_byte as u8);
    if beatmap.mode == mode {
        return Ok(JNI_TRUE);
    }
    beatmap
        .convert_mut(mode, &GameMods::default())
        .or_else(|e| Err(e.to_string()))?;
    set_beatmap_field(env, this, &beatmap)?;
    Ok(JNI_TRUE)
}
