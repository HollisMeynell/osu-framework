package org.spring.osu

import org.spring.core.ProxyConfig

data class OsuApiConfig(
    var redirectUri: String,
    var clientID: Long,
    var clientToken: String,
    var proxy: ProxyConfig?,
)