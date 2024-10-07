package org.spring.osu.extended

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.Proxy

val log = KotlinLogging.logger { }

data class IrcConfig(
    var username: String,
    var password: String,
    val address: String = "irc.ppy.sh",
    val port: Int = 6667,
    val proxy: Proxy = Proxy.NO_PROXY,
)

