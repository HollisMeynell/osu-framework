package org.spring.application

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.util.StdConverter
import org.spring.core.applicationRun
import org.spring.core.json
import org.spring.core.toJson

class Main


class A1 : JsonSerializer<String>() {
    override fun serialize(p0: String?, p1: JsonGenerator, p2: SerializerProvider) {
        p1.writeString(p0)
    }
}

class A2 : JsonDeserializer<String>() {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext): String {
        return "[${p0?.text}]"
    }
}

class A22 : StdConverter<A3, A3>() {
    override fun convert(value: A3?): A3 {
        if (value == null) return A3("", null)
        if (value::class != Any::class) return value
        return A3(value.x + "end", value.z)
    }
}

@JsonSerialize(converter = A22::class)
open class A3(
    @JsonSerialize(using = A1::class)
    @JsonDeserialize(using = A2::class)
    val x: String = "xx",
    val z: Int?,
)

class A4(
    z: Int?,
    val d: Int
) : A3(z = z)

class A5(
    val a3s: List<A4>
)


fun main() = applicationRun {
    println("Hello, Spring!")
    val x = """
        {a3s: [{x: "15", d: 1611, }, {x: "15", d: 1611, }, {x: "15", d: 1611, }, {x: "15", d: 1611, }, {x: "15", d: 1611, }, ]}
    """.trimIndent()
    val y = x.json<A5>()
    println(y.a3s.first().javaClass)
    println(y.toJson())

    val x1 = "{x: 'aa'}".json<A3>()
    println(x1.toJson())
}