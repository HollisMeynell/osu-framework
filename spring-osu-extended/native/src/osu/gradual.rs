use crate::java::cache_key::{GRADUAL_PERFORMANCE_CLASS, GRADUAL_PERFORMANCE_INIT};
use crate::java::{get_jni_class, get_jni_method_id};
use crate::osu::java_fu::{get_object_ptr, release_object, set_object_ptr};
use crate::osu::performance::{generate_java_state, parse_java_state};
use crate::{get_class, to_ptr, to_status, to_status_use};
use jni::objects::{JByteArray, JClass, JObject};
use jni::sys::{jint, jlong, jobject, jvalue};
use jni::JNIEnv;
use rosu_pp::any::{PerformanceAttributes, ScoreState};
use rosu_pp::{Difficulty, GradualPerformance};

pub fn gradual_performance(
    env: &mut JNIEnv,
    this: &JObject,
    beatmap_ptr: jlong,
) -> crate::Result<jobject> {
    let beatmap = to_status_use(beatmap_ptr)?;
    let difficulty_ptr = get_object_ptr(env, this)?;
    let difficulty = to_status::<Difficulty>(difficulty_ptr)?;
    release_object(env, this)?;

    let gradual = difficulty.gradual_performance(beatmap);
    let global = get_jni_class(
        GRADUAL_PERFORMANCE_CLASS,
        env,
        "org/spring/osu/extended/rosu/JniGradualPerformance",
    )?;
    let class = get_class!(global);
    let method = get_jni_method_id(GRADUAL_PERFORMANCE_INIT, || {
        let method = env.get_method_id(
            class,
            "<init>",
            "(Lorg/spring/osu/extended/rosu/JniScoreState;)V",
        )?;
        Ok(method)
    })?;

    let state = generate_java_state(env, ScoreState::new())?;
    let obj = unsafe { env.new_object_unchecked(class, method, &[jvalue { l: state }]) }?;
    set_object_ptr(env, &obj, to_ptr(gradual))?;
    Ok(obj.into_raw())
}

pub fn gradual_len(env: &mut JNIEnv, this: &JObject) -> crate::Result<jint> {
    let ptr = get_object_ptr(env, this)?;
    let gradual = to_status_use::<GradualPerformance>(ptr)?;
    Ok(gradual.len() as i32)
}

fn gradual_action(
    env: &mut JNIEnv,
    this: &JObject,
    state: &JByteArray,
    fun: impl FnOnce(&mut GradualPerformance, ScoreState) -> Option<PerformanceAttributes>,
) -> crate::Result<jobject> {
    let state = parse_java_state(env, state)?;
    let ptr = get_object_ptr(env, this)?;
    let gradual = to_status_use::<GradualPerformance>(ptr)?;
    let attr = fun(gradual, state);
    let obj = match attr {
        Some(data) => crate::osu::performance::attribute_to_object(env, data)?,
        None => JObject::null().into_raw(),
    };
    Ok(obj)
}

pub fn gradual_next(
    env: &mut JNIEnv,
    this: &JObject,
    state: &JByteArray,
) -> crate::Result<jobject> {
    gradual_action(env, this, state, |gradual, state| gradual.next(state))
}

pub fn gradual_last(
    env: &mut JNIEnv,
    this: &JObject,
    state: &JByteArray,
) -> crate::Result<jobject> {
    gradual_action(env, this, state, |gradual, state| gradual.last(state))
}

pub fn gradual_nth(
    env: &mut JNIEnv,
    this: &JObject,
    state: &JByteArray,
    n: jint,
) -> crate::Result<jobject> {
    gradual_action(env, this, state, |gradual, state| {
        gradual.nth(state, n as usize)
    })
}
