use crate::{to_ptr, to_status_use, NativeError, Result};
use jni::objects::{JClass, JFieldID, JMethodID, JStaticFieldID, JStaticMethodID};
use jni::sys::{jboolean, jclass, jfieldID, jmethodID, JNI_TRUE};
use jni::JNIEnv;
use std::collections::HashMap;
use std::sync::atomic::AtomicI64;
use std::sync::atomic::Ordering::SeqCst;
use std::sync::Once;

type JniIdMap = HashMap<&'static str, usize>;
static GLOBAL_JNI_ID: AtomicI64 = AtomicI64::new(0);
static INIT: Once = Once::new();

fn get_global_map() -> Result<&'static mut JniIdMap> {
    INIT.call_once(|| {
        let map: JniIdMap = JniIdMap::new();
        let m_ptr = to_ptr(map);
        unsafe {
            GLOBAL_JNI_ID.store(m_ptr, SeqCst);
        }
    });
    let m_ptr = unsafe { GLOBAL_JNI_ID.load(SeqCst) };
    to_status_use::<JniIdMap>(m_ptr)
}
fn get_id(key: &str) -> Option<usize> {
    let map = get_global_map().unwrap();
    map.get(key).map(|i| *i)
}

fn set_id(key: &'static str, ptr: usize) -> Result<()> {
    let map = get_global_map()?;
    map.insert(key, ptr);
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
get_id_fn! { get_jni_method_id(JMethodID, jmethodID) }
get_id_fn! { get_jni_static_field_id(JStaticFieldID, jfieldID) }
get_id_fn! { get_jni_static_method_id(JStaticMethodID, jmethodID) }

pub fn get_jni_class(key: &'static str, default: impl FnOnce() -> Result<jclass>) -> Result<jclass> {
    let j = match get_id(key) {
        None => {
            println!("is null");
            let f = default()?;
            set_id(key, f as usize)?;
            f
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
