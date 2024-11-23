use crate::{NativeError, Result};
use jni::objects::{JFieldID, JStaticMethodID};
use jni::sys::{jboolean, jclass, jfieldID, jmethodID, JNI_TRUE};
use jni::JNIEnv;
use mini_moka::sync::Cache;
use std::sync::LazyLock;

static GLOBAL_CACHE: LazyLock<Cache<Key, usize>> = LazyLock::new(|| Cache::new(50));

#[derive(Hash, Eq, PartialEq)]
struct Key(&'static str);

fn get_id(key: &'static str) -> Option<usize> {
    GLOBAL_CACHE.get(&Key(key))
}

fn set_id(key: &'static str, ptr: usize) -> Result<()> {
    GLOBAL_CACHE.insert(Key(key), ptr);
    Ok(())
}

macro_rules! get_id_fn {
    ($fx:ident($t:ident, $r:ident)) => {
        pub fn $fx(key: &'static str, default: impl FnOnce() -> Result<$t>) -> Result<$t> {
            let j = match get_id(key) {
                None => {
                    let f = default()?.into_raw();
                    set_id(key, f.clone() as usize)?;
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
// get_id_fn! { get_jni_method_id(JMethodID, jmethodID) }
// get_id_fn! { get_jni_static_field_id(JStaticFieldID, jfieldID) }
get_id_fn! { get_jni_static_method_id(JStaticMethodID, jmethodID) }

/// ```
/// get_jni_class("cache_key", || {
///     let class = env.find_class("org/spring/osu/extended/rosu/Class")?;
///     Ok(class.into_raw())
/// })?;
/// ```
pub fn get_jni_class(
    key: &'static str,
    default: impl FnOnce() -> Result<jclass>,
) -> Result<jclass> {
    let j = match get_id(key) {
        None => {
            let raw = default()?;
            set_id(key, raw as usize)?;
            raw
        }
        Some(p) => p as jclass,
    };
    Ok(j)
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
