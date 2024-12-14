package org.spring.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.utils.io.*
import org.spring.core.json
import org.spring.web.Param.RequestType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

typealias FieldKey = Pair<KClass<*>, RequestType?>
typealias FieldVal = List<Pair<KProperty1<*, *>, Param?>>

val typeCache = ConcurrentHashMap<FieldKey, FieldVal>()
val bodyMethods = setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)

inline fun <reified T : Any> ApplicationCall.getDataNullable(name: String): T? {
    return if (isSimpleType<T>()) {
        try {
            parameters[name]?.toType<T>()
        } catch (e: Exception) {
            throw HttpTipsException(400, "unable to parse $name")
        }
    } else {
        null
    }
}
inline fun <reified T : Any> ApplicationCall.getData(name: String): T {
    return if (isSimpleType<T>()) {
        try {
            parameters[name]?.toType<T>() ?: throw HttpTipsException(400, "must get $name")
        } catch (e: Exception) {
            throw HttpTipsException(400, "unable to parse $name")
        }
    } else {
        throw HttpTipsException(500, "unable to parse $name")
    }
}

suspend inline fun <reified T : Any> ApplicationCall.getData(type: RequestType? = null): T {
    if (request.httpMethod !in bodyMethods) throw IllegalArgumentException("Request method is not supported")
    when (T::class) {
        String::class -> return receiveText() as T
        ByteArray::class -> return receive<ByteArray>() as T
        ByteReadChannel::class -> return receiveChannel() as T
    }
    val data = receive<T>()
    val fields = getFields<T>(type)

    if (type == null) return data
    for ((field, param) in fields) {
        val value = field.get(data)
        if (param == null || !param.testType(type)) {
            continue
        }

        if (param.notNull && value == null) {
            throw IllegalArgumentException("${param.name} is null")
        }
        if (param.notEmpty && value is String && value.isEmpty()) {
            throw IllegalArgumentException("${param.name} is empty")
        }
        if (value is Number && value !in param.min..param.max) {
            throw IllegalArgumentException("${param.name} length is not in range")
        }
    }
    return data
}

inline fun <reified T> isSimpleType(): Boolean = when (T::class) {
    String::class, Boolean::class, Byte::class, Short::class, Int::class, Long::class, Float::class, Double::class -> true
    else -> false
}

inline fun <reified T> String.toType(): T? = when (T::class) {
    String::class -> this as T
    Boolean::class -> toBoolean() as T
    Byte::class -> toByte() as T
    Short::class -> toShort() as T
    Int::class -> toInt() as T
    Long::class -> toLong() as T
    Float::class -> toFloat() as T
    Double::class -> toDouble() as T
    else -> this.json()
}

inline fun <reified T : Any> getFields(type: RequestType?): List<Pair<KProperty1<T, *>, Param?>> {
    @Suppress("UNCHECKED_CAST")
    return typeCache.computeIfAbsent(T::class to type) { (clazz, type) ->
        val allMembers = clazz.memberProperties.map {
            it to it.findAnnotation<Param>()
        }
        if (type == null) {
            return@computeIfAbsent allMembers
        }

        return@computeIfAbsent allMembers.filter { (_, param) -> param != null }
    } as List<Pair<KProperty1<T, *>, Param?>>
}