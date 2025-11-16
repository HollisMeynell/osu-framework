@file:Suppress("unused")

package org.spring.core


import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KProperty

object Json {
    internal val ktMode = KotlinModule.Builder()
        .enable(KotlinFeature.NullIsSameAsDefault)
        .enable(KotlinFeature.SingletonSupport)
        .enable(KotlinFeature.StrictNullChecks)
        .enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
        .enable(KotlinFeature.UseJavaDurationConversion)
        .build()

    val timeMode = JavaTimeModule()

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
        .defaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.USE_DEFAULTS))
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
        .registerModules(timeMode)
        .setTimeZone(TimeZone.getDefault())

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

inline fun <reified T> JsonNode.getItem(key: String): T? {
    val item = this.get(key) ?: return null
    return item.asType()
}

inline fun <reified T> JsonNode.asType(): T {
    val item = this
    return when (T::class) {
        JsonNode::class -> item as T
        BigDecimal::class -> item.decimalValue() as T
        BigInteger::class -> item.bigIntegerValue() as T
        Short::class -> item.shortValue() as T
        Int::class -> item.asInt() as T
        Float::class -> item.asDouble().toFloat() as T
        Double::class -> item.asDouble() as T
        Long::class -> item.asLong() as T
        Boolean::class -> item.asBoolean() as T
        String::class -> item.asText() as T
        else -> item.json<T>()
    }
}

inline fun <reified T> ObjectNode.putValue(name: String, value: T?) {
    when (T::class) {
        BigDecimal::class -> put(name, value as BigDecimal)
        BigInteger::class -> put(name, value as BigInteger)
        Short::class -> put(name, value as Short)
        Int::class -> put(name, value as Int)
        Float::class -> put(name, value as Float)
        Double::class -> put(name, value as Double)
        Long::class -> put(name, value as Long)
        Boolean::class -> put(name, value as Boolean)
        String::class -> put(name, value as String)
        JsonNode::class -> putIfAbsent(name, value as JsonNode)
        else -> putIfAbsent(name, Json.mapper.valueToTree(value) as JsonNode)
    }
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
    return node.getItem(key)
}

fun String.jsonNode(): JsonNode {
    return Json.mapper.readTree(this)
}

/**
 * json 代理类, 子类可以使用 by delegate 来访问代理字段, 但是可能没必要
 */
abstract class JsonObject(
    @JsonIgnore
    protected val delegate: ObjectNode = Json.mapper.createObjectNode()
) : TreeNode by delegate {
    @JsonIgnore
    protected val delegateNullable = DelegateNullable()

    protected class DelegateNullable

    protected inline operator fun <R : JsonObject, reified T> DelegateNullable.getValue(
        thisRef: R,
        property: KProperty<*>
    ): T? {
        val item = thisRef.delegate.get(property.name) ?: return null
        return item.asType()
    }

    protected inline operator fun <R : JsonObject, reified T> DelegateNullable.setValue(
        thisRef: R,
        property: KProperty<*>,
        value: T?
    ) {
        thisRef.delegate.putValue(property.name, value)
    }

    protected inline operator fun <R : JsonObject, reified T> ObjectNode.getValue(thisRef: R, property: KProperty<*>): T {
        return thisRef.delegate.get(property.name).asType()
    }

    protected inline operator fun <R : JsonObject, reified T> ObjectNode.setValue(thisRef: R, property: KProperty<*>, value: T) {
        thisRef.delegate.putValue(property.name, value)
    }

    inline fun <reified T> JsonNode.asType(): T {
        val item = this
        return when (T::class) {
            JsonNode::class -> item as T
            BigDecimal::class -> item.decimalValue() as T
            BigInteger::class -> item.bigIntegerValue() as T
            Short::class -> item.shortValue() as T
            Int::class -> item.asInt() as T
            Float::class -> item.asDouble().toFloat() as T
            Double::class -> item.asDouble() as T
            Long::class -> item.asLong() as T
            Boolean::class -> item.asBoolean() as T
            String::class -> item.asText() as T
            else -> item.json<T>()
        }
    }

    inline fun <reified T> ObjectNode.putValue(name: String, value: T?) {
        when (T::class) {
            BigDecimal::class -> put(name, value as BigDecimal)
            BigInteger::class -> put(name, value as BigInteger)
            Short::class -> put(name, value as Short)
            Int::class -> put(name, value as Int)
            Float::class -> put(name, value as Float)
            Double::class -> put(name, value as Double)
            Long::class -> put(name, value as Long)
            Boolean::class -> put(name, value as Boolean)
            String::class -> put(name, value as String)
            JsonNode::class -> putIfAbsent(name, value as JsonNode)
            else -> putIfAbsent(name, Json.mapper.valueToTree(value) as JsonNode)
        }
    }

    @JsonIgnore
    fun getObjectNode() = delegate
    inline fun<reified T> getValue(key:String):T?{
        val item = getObjectNode().get(key) ?: return null
        return item.asType()
    }
    inline fun<reified T> getValue(index:Int):T?{
        val item = getObjectNode().get(index) ?: return null

        return item.asType()
    }

    /**
     * 适使用前, 将注册到 mapper 上 SimpleModule().setSerializerModifier(JsonObject.SkipDelegatePropertiesModifier())
     */
    class SkipDelegatePropertiesModifier : BeanSerializerModifier() {
        private val noNeedProperties =
            setOf("is_array", "is_object", "is_container_node", "is_value_node", "is_missing_node")

        override fun changeProperties(
            config: SerializationConfig,
            beanDesc: BeanDescription,
            beanProperties: MutableList<BeanPropertyWriter>
        ): MutableList<BeanPropertyWriter> {
            if (beanDesc.beanClass.isAssignableFrom(JsonObject::class.java)) {
                return beanProperties
            }
            return beanProperties
                .filterNot { noNeedProperties.contains(it.name) || it.name.endsWith("\$delegate") }
                .toMutableList()
        }
    }
}