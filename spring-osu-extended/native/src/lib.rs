use crate::NativeError::Runtime;
use thiserror::Error;

mod java;
mod r#macro;
mod osu;

type Result<T> = std::result::Result<T, NativeError>;

#[derive(Error, Debug)]
pub enum NativeError {
    #[error("run error: {0}")]
    Runtime(String),
    #[error("value is null")]
    Null,
    #[error(transparent)]
    Io(#[from] std::io::Error),
    #[error(transparent)]
    Jni(#[from] jni::errors::Error),
    #[error(transparent)]
    JniOther(#[from] jni::errors::JniError),
}

impl From<String> for NativeError {
    fn from(value: String) -> Self {
        Runtime(value)
    }
}

impl From<&str> for NativeError {
    fn from(value: &str) -> Self {
        value.to_string().into()
    }
}

impl NativeError {
    fn err_info(&self) -> String {
        self.to_string()
    }
}

#[inline]
pub fn to_ptr<T>(s: T) -> i64 {
    Box::into_raw(Box::new(s)) as i64
}

#[inline]
fn check_ptr<T>(point: *mut T) -> Result<()> {
    if point.is_null() {
        return Err("point is null or not".into());
    }
    Ok(())
}

#[inline]
pub fn to_status_use<T>(p: i64) -> Result<&'static mut T> {
    let point = p as *mut T;
    check_ptr(point)?;
    unsafe {
        point
            .as_mut()
            .ok_or_else(|| format!("read pointer error: ({})", p).into())
    }
}

#[inline]
#[warn(unused_must_use)]
fn to_status<T>(p: i64) -> Result<Box<T>> {
    let point = p as *mut T;
    check_ptr(point)?;
    unsafe {
        if let None = point.as_ref() {
            Err(format!("read pointer error: ({})", p).into())
        } else {
            Ok(Box::from_raw(point))
        }
    }
}
