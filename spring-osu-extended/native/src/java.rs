use crate::{NativeError, Result};
use jni::objects::{GlobalRef, JFieldID, JMethodID, JStaticMethodID};
use jni::sys::{jboolean, jfieldID, jmethodID, JNI_TRUE};
use jni::JNIEnv;
use mini_moka::sync::Cache;
use std::sync::LazyLock;

pub(crate) mod cache_key {
    pub const PTR: u32 = 0;

    pub const BEATMAP_FIELD_AR: u32 = 10;
    pub const BEATMAP_FIELD_OD: u32 = 11;
    pub const BEATMAP_FIELD_CS: u32 = 12;
    pub const BEATMAP_FIELD_HP: u32 = 13;
    pub const BEATMAP_FIELD_MD: u32 = 14;
    pub const BEATMAP_FIELD_BPM: u32 = 15;
    pub const BEATMAP_FIELD_SM: u32 = 16;
    pub const BEATMAP_FIELD_ST: u32 = 17;
    pub const BEATMAP_FIELD_OBJS: u32 = 18;

    pub const DIFFICULTY_ATTR_CLASS: u32 = 50;
    pub const DIFFICULTY_OSU: u32 = 51;
    pub const DIFFICULTY_TAIKO: u32 = 52;
    pub const DIFFICULTY_CATCH: u32 = 53;
    pub const DIFFICULTY_MANAI: u32 = 54;

    pub const GRADUAL_PERFORMANCE_CLASS: u32 = 70;
    pub const GRADUAL_PERFORMANCE_INIT: u32 = 71;

    pub const PERFORMANCE_STATE_CLASS: u32 = 100;
    pub const PERFORMANCE_STATE_CREATE: u32 = 110;
    pub const PERFORMANCE_ATTR_OSU: u32 = 111;
    pub const PERFORMANCE_ATTR_TAIKO: u32 = 112;
    pub const PERFORMANCE_ATTR_CATCH: u32 = 113;
    pub const PERFORMANCE_ATTR_MANAI: u32 = 114;
    pub const PERFORMANCE_ATTR_CLASS: u32 = 115;
}

static GLOBAL_CLASS_CACHE: LazyLock<Cache<u32, GlobalRef>> = LazyLock::new(|| Cache::new(15));

static GLOBAL_JNI_ID_CACHE: LazyLock<Cache<u32, usize>> = LazyLock::new(|| Cache::new(30));

macro_rules! get_id_fn {
    ($fx:ident($t:ident, $r:ident)) => {
        pub fn $fx(key: u32, default: impl FnOnce() -> Result<$t>) -> Result<$t> {
            let j = match GLOBAL_JNI_ID_CACHE.get(&key) {
                None => {
                    let f = default()?.into_raw();
                    GLOBAL_JNI_ID_CACHE.insert(key, f as usize);
                    f
                }
                Some(p) => p as $r,
            };
            let j = unsafe { $t::from_raw(j) };
            Ok(j)
        }
    };
}

get_id_fn! { get_jni_field_id(JFieldID, jfieldID) }
get_id_fn! { get_jni_method_id(JMethodID, jmethodID) }
// get_id_fn! { get_jni_static_field_id(JStaticFieldID, jfieldID) }
get_id_fn! { get_jni_static_method_id(JStaticMethodID, jmethodID) }

/// ```
/// val global = get_jni_class("cache_key", || {
///     let class = env.find_class("org/spring/osu/extended/rosu/Class")?;
///     Ok(class))
/// })?;
/// <&JClass>::from(global.as_obj())
/// ```
pub fn get_jni_class(key: u32, env: &mut JNIEnv, sig: &str) -> Result<GlobalRef> {
    let j = match GLOBAL_CLASS_CACHE.get(&key) {
        None => {
            let class = env.find_class(sig)?;
            let raw = env.new_global_ref(class)?;
            GLOBAL_CLASS_CACHE.insert(key, raw.clone());
            raw
        }
        Some(p) => p,
    };
    Ok(j)
}

#[macro_export]
macro_rules! get_class {
    ($global_ref:expr) => {
        <&JClass>::from($global_ref.as_obj())
    };
    ($key:expr,$env:expr, $sig:expr) => {
        let global_ref = get_jni_class($key, $env, $sig)?;
        <&JClass>::from(global_ref.as_obj())
    };
}

pub fn throw_jni(env: &mut JNIEnv, err: NativeError) {
    if env.exception_check().expect("check error.") {
        env.exception_describe().expect("show");
    }
    env.exception_clear().expect("clear error.");
    env.throw_new("Ljava/lang/Exception;", err.err_info())
        .expect("error!");
}

pub trait JavaBoolean {
    fn is_true(&self) -> bool;
}

impl JavaBoolean for jboolean {
    #[inline]
    fn is_true(&self) -> bool {
        JNI_TRUE == *self
    }
}
