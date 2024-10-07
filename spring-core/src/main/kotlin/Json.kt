@file:Suppress("unused")

package org.spring.core


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule


object Json {
    internal val ktMode = KotlinModule.Builder()
        .enable(KotlinFeature.NullIsSameAsDefault)
        .enable(KotlinFeature.SingletonSupport)
        .enable(KotlinFeature.StrictNullChecks)
        .enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
        .enable(KotlinFeature.UseJavaDurationConversion)
        .build()
    val mapper: ObjectMapper = JsonMapper.builder()
        // 空 val 不报错
        .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
        // 允许 key 是任意字符
        .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
        // 支持尾随逗号
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        // 支持单引号
        .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
        // 支持字符串转义以及多行处理
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        // 数字支持 无穷大, NaN
        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
        // 允许注释
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        // 允许数字小数点在两端
        .enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
        .enable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS)
        // 允许数字前置加号
        .enable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS)
        // 序列化时忽略 null 字段
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build()
        // 设置允许忽略未知的字段
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // enum 使用 string
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
        // 设置可见性
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        // 默认使用驼峰转下划线命名
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .registerModules(ktMode)
        .registerModules(JavaTimeModule())

    val prettyPrinter: ObjectWriter = mapper.writerWithDefaultPrettyPrinter()
    val typeFactory: TypeFactory = mapper.typeFactory
    val nullNode: ObjectNode = mapper.createObjectNode()
}

fun Any?.toJson(): String = when (this) {
    null -> ""
    is String -> this
    else -> Json.mapper.writeValueAsString(this)
}

fun Any?.toPrettyJson(): String = when (this) {
    null -> ""
    is String -> this
    else -> Json.prettyPrinter.writeValueAsString(this)
}

inline fun <reified T> JsonNode.json(key: String? = null): T {
    val node = if (key == null) {
        this
    } else {
        this.get(key) ?: Json.nullNode
    }
    return Json.mapper.treeToValue(node, Json.typeFactory.constructType(T::class.java))
}

inline fun <reified T> JsonNode.jsonList(key: String? = null): List<T> {
    if (key == null) return Json.mapper.treeToValue(
        this,
        Json.typeFactory.constructCollectionType(List::class.java, T::class.java)
    )
    if (hasNonNull(key).not()) return emptyList()
    return Json.mapper.treeToValue(
        this.get(key),
        Json.typeFactory.constructCollectionType(List::class.java, T::class.java)
    )
}

fun JsonNode.getNode(key: String): JsonNode {
    return this.get(key) ?: Json.nullNode
}

inline fun <reified T> String.json(): T {
    return when (T::class) {
        String::class -> this as T
        JsonNode::class -> Json.mapper.readTree(this) as T
        else -> Json.mapper.readValue(
            this,
            Json.typeFactory.constructType(T::class.java)
        )
    }
}

inline fun <reified T> String.jsonList(): List<T> {
    return Json.mapper.readValue(
        this,
        Json.typeFactory.constructCollectionType(List::class.java, T::class.java)
    )
}

inline fun <reified T> String.json(type: TypeReference<T>): T {
    return when (T::class) {
        String::class -> this as T
        JsonNode::class -> Json.mapper.readTree(this) as T
        else -> Json.mapper.readValue(
            this,
            type
        )
    }
}

inline fun <reified T> String.jsonItem(key: String): T? {
    val node = Json.mapper.readTree(this)
    val item = node.get(key)
    if (item.isNull) return null
    return when (T::class) {
        Int::class -> item.asInt() as T
        Float::class -> item.asDouble().toFloat() as T
        Double::class -> item.asDouble() as T
        Long::class -> item.asLong() as T
        Boolean::class -> item.asBoolean() as T
        String::class -> item.asText() as T
        else -> item.json<T>()
    }
}

fun String.jsonNode(): JsonNode {
    return Json.mapper.readTree(this)
}