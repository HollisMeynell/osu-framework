package org.spring.web

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.spring.core.Json
import org.spring.core.ProxyConfig

object WebClient {
    lateinit var client: HttpClient
    lateinit var proxyClient: HttpClient

    fun initClient(proxy: ProxyConfig? = null) {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                register(
                    ContentType.Application.Json,
                    JacksonConverter(Json.mapper)
                )
            }
        }

        proxyClient = proxy?.let {
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    register(
                        ContentType.Application.Json,
                        JacksonConverter(Json.mapper)
                    )
                }
                engine {
                    this.proxy = it.toProxy()
                }
            }
        } ?: client
    }
}