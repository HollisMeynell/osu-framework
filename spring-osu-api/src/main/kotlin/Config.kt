package org.spring.osu

data class OsuApiConfig(
    val redirectUri: String,
    val clientID: Long,
    val clientToken: String,
    val proxyUrl: String?,
)