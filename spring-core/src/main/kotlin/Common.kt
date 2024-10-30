package org.spring.core

import java.net.Proxy

data class ProxyConfig (
    val host: String,
    val port: Int,
    val type: String = "HTTP"
) {
    fun toProxy(): Proxy {
        return when (type.uppercase()) {
            "HTTP" -> Proxy(Proxy.Type.HTTP, java.net.InetSocketAddress(host, port))
            "SOCKS" -> Proxy(Proxy.Type.SOCKS, java.net.InetSocketAddress(host, port))
            else -> throw Exception("Unknown proxy type: $type")
        }
    }
}