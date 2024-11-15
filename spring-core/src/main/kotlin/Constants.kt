@file:Suppress("unused")

package org.spring.core

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.getOrSet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/************* kt ****************/
typealias TickerStop = () -> Unit

@OptIn(DelicateCoroutinesApi::class)
var coroutineScope: CoroutineScope = GlobalScope
val log = KotlinLogging.logger("org.spring.core")

val LOCAL = ThreadLocal<MutableMap<String, Any>>()
fun <T : Any> setContext(key: String, value: T) {
    val map = LOCAL.getOrSet { mutableMapOf() }
    map[key] = value
}

fun <T : Any> getContext(key: String): T? {
    val map = LOCAL.get()
    val value = map?.get(key)
    @Suppress("UNCHECKED_CAST")
    return value as T?
}

fun clearContext() {
    LOCAL.remove()
}

suspend fun <T> withContext(block: suspend () -> T): T {
    return coroutineScope {
        async(coroutineContext + LOCAL.asContextElement()) {
            block()
        }.await()
    }
}

/**
 * 重复执行任务
 */
inline fun CoroutineScope.ticker(
    delayTime: Duration = 3.seconds,
    crossinline action: suspend CoroutineScope.() -> Unit
): TickerStop {
    val job = this.launch {
        while (isActive) {
            delay(delayTime)
            tryRunUnit(info = "ticker action error") {
                action()
            }
        }
    }
    return { job.cancel() }
}

/**
 * 重复执行任务, block 直到返回 false 停止
 *
 * @param withException 当遇到异常时是否继续执行
 */
inline fun CoroutineScope.tickerCondition(
    delayTime: Duration = 3.seconds,
    withException: Boolean = true,
    crossinline action: suspend CoroutineScope.() -> Boolean
): TickerStop {
    val job = this.launch {
        var run = true
        while (isActive and run) {
            delay(delayTime)
            run = tryRun(info = "ticker action error") {
                action()
            } ?: withException
        }
    }
    return { job.cancel() }
}

val debounceJobMap = ConcurrentHashMap<String, Job>()

/**
 * 调用方法防抖, 在 delayTime 时间内多次调用只执行一次
 */
inline fun debounce(
    key: String,
    delayTime: Duration = 3.seconds,
    crossinline action: suspend CoroutineScope.() -> Unit
) {
    debounceJobMap[key]?.cancel()
    debounceJobMap[key] = coroutineScope.launch {
        delay(delayTime)
        debounceJobMap.remove(key)
        action()
    }
}

val mutex = Mutex()
val locks = HashMap<Any, CompletableDeferred<Any>>()

/**
 * 方法合并, 多次调用 key 相同的任务, 只有第一个任务会执行, 其他任务等待第一个任务执行完毕后返回结果
 */
suspend inline fun <reified T : Any> synchronizedTask(
    key: Any,
    crossinline action: suspend () -> T,
): T = coroutineScope {
    val deferred: CompletableDeferred<Any>
    val isNew = mutex.withLock {
        if (locks.contains(key)) {
            println("old key $key")
            deferred = locks[key]!!
            false
        } else {
            deferred = CompletableDeferred()
            locks[key] = deferred
            val lk = locks.keys.toList().joinToString(",")
            println("new key $key, ${locks.contains(key)}, now [$lk]")
            true
        }
    }
    if (isNew) launch {
        try {
            deferred.complete(action())
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
        } finally {
            mutex.withLock {
                locks.remove(key)
            }
        }
    }
    deferred.await() as T
}

/**
 * 并发执行多个任务, 返回所有任务的结果, 顺序为完成顺序, 出现异常则不添加到结果中
 */
suspend inline fun <reified T> synchronizedRun(
    actions: Collection<suspend () -> T>,
    crossinline onError: (Exception) -> Unit,
): List<T> =
    coroutineScope {
        actions
            .map {
                async {
                    try {
                        it()
                    } catch (e: Exception) {
                        onError(e)
                        null
                    }
                }
            }
            .mapNotNull { it.await() }
            .toList()
    }

/**
 * 并发竞争请求, 任意一个成功就返回结果, 其他的取消
 */
suspend inline fun <T> selectRun(
    actions: Collection<suspend () -> T>,
    crossinline onError: (Exception) -> Unit,
): T {
    val channel = Channel<T>(1)
    val failCount = AtomicInteger(actions.size)
    val fail: (Exception) -> Unit = {
        val n = failCount.decrementAndGet()
        if (n == 0) {
            channel.close()
        }
        onError(it)
    }
    val tasks = actions.map { action ->
        coroutineScope.launch {
            try {
                val result = action()
                channel.send(result)
            } catch (e: CancellationException) {
                return@launch
            } catch (e: Exception) {
                fail(e)
            }
        }
    }
    val result: T = channel.receive()
    tasks.forEach { it.cancel() }
    return result
}

inline fun <T> tryRun(
    logger: KLogger = log,
    info: String? = "",
    action: () -> T
): T? {
    return try {
        action()
    } catch (e: Exception) {
        if (info != null) logger.error(e) { info }
        null
    }
}

inline fun tryRunUnit(
    logger: KLogger = log,
    info: String? = "",
    action: () -> Unit
) {
    return try {
        action()
    } catch (e: Exception) {
        if (info != null) {
            logger.error(e) { info }
        }
        return
    }
}

/**
 * init
 */
fun applicationRun(action: suspend CoroutineScope.() -> Unit) = runBlocking {
    supervisorScope {
        coroutineScope = this@supervisorScope
        coroutineScope.action()
    }
}

