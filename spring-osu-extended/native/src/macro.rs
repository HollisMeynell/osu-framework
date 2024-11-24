#[macro_export]
macro_rules! jni_call {
    ([$env:ident] $func:expr) => {
        match std::panic::catch_unwind(move || {
            if let Err(e) = $func {
                throw_jni(&mut $env, e)
            }
        }) {
            Ok(_) => {}
            Err(e) => {
                println!("some panic: {e:?}");
            }
        }
    };
    ([$env:ident]$func:expr => {$default:expr}) => {
        match std::panic::catch_unwind(move || match $func {
            Ok(data) => data,
            Err(e) => {
                throw_jni(&mut $env, e);
                $default
            }
        }) {
            Ok(data) => data,
            Err(e) => {
                println!("some panic: {e:?}");
                $default
            }
        }
    };
}
