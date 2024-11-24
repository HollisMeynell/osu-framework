#[macro_export]
macro_rules! jni_call {
    ([$env:ident] $func:expr) => {
        let panic_result = panic::catch_unwind(|| {
            if let Err(e) = $func {
                throw_jni(&mut $env, e)
            }
        });
        match panic_result {
            Ok(_) => {}
            Err(e) => { println!("some panic: {e:?}"); }
        }
    };
    ([$env:ident]$func:expr => {$default:expr}) => {
        let panic_result = panic::catch_unwind(|| {
            match $func {
                Ok(data) => data,
                Err(e) => {
                    throw_jni(&mut $env, e);
                    $default
                }
            }
        });
        match panic_result {
            Ok(data) => { data }
            Err(e) => {
                println!("some panic: {e:?}");
                $default
            }
        }

    };
}
