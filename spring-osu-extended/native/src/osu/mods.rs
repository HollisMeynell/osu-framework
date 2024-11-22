#[macro_export]
macro_rules! get_mods_from_java {
    ($mods:tt) => {
        crate::osu::mods::pp::get_mods_from_java_bits($mods)
    };
    ($env:tt, $mode:tt, $mods:tt) => {
        crate::osu::mods::pp::get_mods_from_java_json($env, $mode, $mods)
    };
    ($env:tt, $mode:tt, $legacy:tt, $lazer:tt) => {
        crate::osu::mods::pp::get_mods_from_java_mix($env, $mode, $legacy, $lazer)
    };
}

pub mod pp {
    use super::get_mods_from_json;
    use crate::Result;
    use jni::objects::JString;
    use jni::sys::{jbyte, jint};
    use jni::JNIEnv;
    use rosu_mods::{GameMode, GameModsLegacy};
    use rosu_pp::GameMods;

    pub fn get_mods_from_java_bits(legacy: jint) -> GameMods {
        GameMods::from(legacy as u32)
    }

    pub fn get_mods_from_java_json(
        env: &mut JNIEnv,
        mode: jbyte,
        mods: &JString,
    ) -> Result<GameMods> {
        let mode = GameMode::from(mode as u8);
        let name: String = env.get_string(mods)?.into();
        let lazer = get_mods_from_json(&name, mode)?;
        let lazer = rosu_pp::GameMods::from(lazer);
        Ok(lazer)
    }

    pub fn get_mods_from_java_mix(
        env: &mut JNIEnv,
        mode: jbyte,
        legacy: jint,
        lazer: &JString,
    ) -> Result<GameMods> {
        let mode = GameMode::from(mode as u8);
        let name: String = env.get_string(lazer)?.into();
        let mut lazer = get_mods_from_json(&name, mode)?;
        let legacy = GameModsLegacy::from_bits(legacy as u32).to_intermode();
        rosu_mods::GameMods::from_intermode(&legacy, mode)
            .into_iter()
            .for_each(|game_mod| lazer.insert(game_mod));

        let all = GameMods::from(lazer);
        Ok(all)
    }
}

mod mods {
    use crate::Result;
    use rosu_mods::serde::GameModsSeed;
    use rosu_mods::{GameMode, GameMods, GameModsLegacy};
    use serde::de::DeserializeSeed;
    use serde_json::Deserializer;

    pub fn get_mods_from_bits(value: i32) -> GameModsLegacy {
        GameModsLegacy::from_bits(value as u32)
    }

    pub fn get_mods_from_json(json: &str, mode: GameMode) -> Result<GameMods> {
        let mut json = Deserializer::from_str(json);
        let mods = GameModsSeed::Mode(mode).deserialize(&mut json)?;
        Ok(mods)
    }

    pub fn get_mods_from_json_unknown_mode(json: &str) -> Result<GameMods> {
        let mut json = Deserializer::from_str(json);
        let mods = GameModsSeed::SameModeForEachMod.deserialize(&mut json)?;
        Ok(mods)
    }
}

pub use mods::*;
