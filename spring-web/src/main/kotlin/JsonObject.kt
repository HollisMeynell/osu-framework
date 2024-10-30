package org.spring.web

data class ProxyDto(
    @Param(notEmpty = true)
    val url: String,
    @Param(notEmpty = true)
    val method: String,

    val headers: Map<String, String>?,
    val parameter: Map<String, String>?,
    val body: Any?,
)