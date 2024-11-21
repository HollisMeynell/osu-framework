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

