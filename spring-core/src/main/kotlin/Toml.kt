@file:Suppress("unused")

package org.spring.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.spring.core.Json.ktMode

object Toml {
    private val mapper: TomlMapper = TomlMapper.builder()
        .addModules(JavaTimeModule())
        .addModules(ktMode)
        .build()

    fun toJsonNode(toml: String): JsonNode {
        return mapper.readTree(toml)
    }
}