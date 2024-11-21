use crate::osu::java_fu::{get_object_ptr, set_object_ptr};
use crate::{to_ptr, to_status_use, Result};
use jni::objects::{JObject, JValueGen};
use jni::sys::jint;
use jni::JNIEnv;
use rosu_mods::{GameModIntermode, GameMode};
use rosu_mods::serde::GameModSeed::Mode;
use rosu_pp::any::ScoreState;
use rosu_pp::{Beatmap, GameMods, Performance};
use serde::de::DeserializeSeed;
use crate::osu::mods::get_mods_from_json;

pub fn generate_state(env: &mut JNIEnv, obj: &JObject) -> Result<()> {
    let state = ScoreState::new();
    let ptr = to_ptr(state);
    set_object_ptr(env, obj, ptr)?;
    parse_java_state(env, obj, Some(ptr))
}

fn parse_java_state(env: &mut JNIEnv, this: &JObject, state: Option<i64>) -> Result<()> {
    let state = match state {
        Some(data) => to_status_use::<ScoreState>(data)?,
        None => {
            let ptr = get_object_ptr(env, this)?;
            to_status_use::<ScoreState>(ptr)?
        }
    };
    state.max_combo = env.get_field(this, "maxCombo", "I")?.i()? as u32;
    state.max_combo = env.get_field(this, "sliderTickHits", "I")?.i()? as u32;
    state.max_combo = env.get_field(this, "sliderEndHits", "I")?.i()? as u32;
    state.n_geki = env.get_field(this, "geki", "I")?.i()? as u32;
    state.n_katu = env.get_field(this, "katu", "I")?.i()? as u32;
    state.n300 = env.get_field(this, "n300", "I")?.i()? as u32;
    state.n100 = env.get_field(this, "n100", "I")?.i()? as u32;
    state.n50 = env.get_field(this, "n50", "I")?.i()? as u32;
    state.misses = env.get_field(this, "misses", "I")?.i()? as u32;
    Ok(())
}
fn generate_state_from_performance(
    env: &mut JNIEnv,
    obj: &JObject,
    performance: &mut Performance,
) -> Result<()> {
    let state = performance.generate_state();
    env.set_field(
        obj,
        "maxCombo",
        "I",
        JValueGen::Int(state.max_combo as jint),
    )?;
    env.set_field(
        obj,
        "sliderTickHits",
        "I",
        JValueGen::Int(state.max_combo as jint),
    )?;
    env.set_field(
        obj,
        "sliderEndHits",
        "I",
        JValueGen::Int(state.max_combo as jint),
    )?;
    env.set_field(obj, "geki", "I", JValueGen::Int(state.n_geki as jint))?;
    env.set_field(obj, "katu", "I", JValueGen::Int(state.n_katu as jint))?;
    env.set_field(obj, "n300", "I", JValueGen::Int(state.n300 as jint))?;
    env.set_field(obj, "n100", "I", JValueGen::Int(state.n100 as jint))?;
    env.set_field(obj, "n50", "I", JValueGen::Int(state.n50 as jint))?;
    env.set_field(obj, "misses", "I", JValueGen::Int(state.misses as jint))?;

    let ptr = to_ptr(state);
    set_object_ptr(env, obj, ptr)?;
    Ok(())
}

pub fn get_performance_from_beatmap(
    env: &mut JNIEnv,
    this: &JObject,
    beatmap: &JObject,
) -> Result<()> {
    let beatmap_ptr = get_object_ptr(env, beatmap)?;
    let beatmap = to_status_use::<Beatmap>(beatmap_ptr)?;
    let mut performance = Performance::new(beatmap.clone());
    let state = env.get_field(
        this,
        "state",
        "Lorg/spring/osu/extended/rosu/JniScoreState;",
    )?;
    generate_state_from_performance(env, &state.l()?, &mut performance)?;
    let performance_ptr = to_ptr(performance);
    set_object_ptr(env, this, performance_ptr)?;
    Ok(())
}

#[test]
fn test_mods() ->Result<()>{
    let mods_str = "[{\"acronym\":\"HD\",\"settings\":{}},{\"acronym\":\"DT\",\"settings\":{}}]";
    let x = get_mods_from_json(mods_str, GameMode::Osu)?;
    println!("{:?}", x);
    Ok(())
}