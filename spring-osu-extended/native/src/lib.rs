use std::fs::remove_file;
use std::io::Write;
use bytes::BufMut;
use thiserror::Error;

mod macros;
mod java;

type Result<T> = std::result::Result<T, NativeError>;
#[derive(Error, Debug)]
pub enum NativeError {
    #[error("run error: {0}")]
    Any(String),
    #[error(transparent)]
    Io(#[from] std::io::Error),
    #[error(transparent)]
    Jni(#[from] jni::errors::Error),
    #[error(transparent)]
    JniOther(#[from] jni::errors::JniError),
}

impl NativeError {
    fn err_info(&self) -> String {
        format!("{:?}", self)
    }
}
