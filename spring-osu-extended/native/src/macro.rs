#[macro_export]
macro_rules! jni_call {
    ([$env:ident] $func:expr) => {
        if let Err(e) = $func {
            throw_jni(&mut $env, e)
        }
    };
    ([$env:ident]$func:expr => {$default:expr}) => {
        match $func {
            Ok(data) => data,
            Err(e) => {
                throw_jni(&mut $env, e);
                $default
            }
        }
    };
}
