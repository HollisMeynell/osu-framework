package org.spring.osu

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.spring.core.HttpClientRateLimiter
import org.spring.core.Json
import org.spring.osu.model.UserAuth
import kotlin.reflect.full.isSubclassOf

internal object ApiRequest {
    private lateinit var config: OsuApiConfig
    private lateinit var bot: UserAuth
    lateinit var client: HttpClientRateLimiter

    val log = KotlinLogging.logger { }
    val clientID
        get() = config.clientID
    val clientToken
        get() = config.clientToken

    val isInited: Boolean
        get() = ::client.isInitialized

    /******************************* auth ****************************/
    fun getOauthUrl(state: String, vararg scopes: AuthScope): String =
        URLBuilder("https://osu.ppy.sh/oauth/authorize")
            .apply {
                parameters.append("client_id", clientID.toString())
                parameters.append("redirect_uri", config.redirectUri)
                parameters.append("response_type", "code")
                parameters.append("scope", scopes.joinToString(" "))
                parameters.append("state", state)
            }
            .buildString()

    private suspend fun getBotAuth(): String {
        if (bot.isExpired().not()) return bot.accessToken!!
        val body = Parameters.build {
            append("client_id", clientID.toString())
            append("client_secret", clientToken)
            append("grant_type", "client_credentials")
            append("scope", "public")
        }

        val response = client.request {
            method = HttpMethod.Post
            url("https://osu.ppy.sh/oauth/token")
            headers {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.FormUrlEncoded)
            }
            setBody(FormDataContent(body))
        }

        if (response.status.isSuccess().not()) {
            val text = response.bodyAsText()
            log.error { "注册 bot 令牌时请求失败[${response.status.description}]: $text" }
            throw OsuApiException(response, text)
        }

        val auth = response.body<AuthResult>()

        bot.refresh(auth.expires, auth.accessToken)

        return bot.accessToken!!
    }

    suspend fun refreshUserAuth(auth: UserAuth): UserAuth {
        val id = auth.getClient()?.id ?: clientID

        val secret = auth.getClient()?.secret ?: clientToken

        val redirect = auth.getClient()?.redirectUrl ?: config.redirectUri

        val body = Parameters.build {
            append("client_id", id.toString())
            append("client_secret", secret)
            append("redirect_uri", redirect)
            if (auth.accessToken == null) {
                append("code", auth.refreshToken!!)
                append("grant_type", "authorization_code")
            } else {
                append("refresh_token", auth.refreshToken!!)
                append("grant_type", "refresh_token")
            }
        }

        val response = client.request {
            method = HttpMethod.Post
            url("https://osu.ppy.sh/oauth/token")
            headers {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.FormUrlEncoded)
            }
            setBody(FormDataContent(body))
        }

        if (response.status.isSuccess().not()) {
            val text = response.bodyAsText()
            log.error { "更新令牌时请求失败[${response.status.description}]: $text" }
            throw OsuApiException(response, text)
        }

        val data = response.body<AuthResult>()

        auth.refresh(
            data.expires,
            data.accessToken,
            data.refreshToken,
        )

        auth.update()

        return auth
    }

    fun init(config: OsuApiConfig) {
        this.config = config
        val httpClient = HttpClient(CIO) {
            // proxy
            config.proxy?.let {
                engine {
                    proxy = it.toProxy()
                }
            }

            // json
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(Json.mapper))
            }

            // default
            defaultRequest {
                url("https://osu.ppy.sh/api/v2/")
            }
        }

        client = HttpClientRateLimiter(httpClient)

        bot = object : UserAuth {
            override var refreshToken: String? = clientToken
            override var accessToken: String? = null
            override var expires: Long = 0
            override fun isAuthed() = false
            override suspend fun update() {}
        }
    }

    internal suspend inline fun <reified T> request(
        auth: UserAuth? = null,
        lazer: Boolean = false,
        build: HttpRequestBuilder.() -> Unit
    ): T {
        if (!isInited) throw OsuApiNotInitException()

        val request = HttpRequestBuilder().apply(build)

        request.apply {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            headers.setAuth(auth)
            if (lazer) {
                headers["x-api-version"] = "20241101"
                headers[HttpHeaders.AcceptLanguage] = "zh-CN,zh;q=0.9,en;q=0.8"
            }
        }

        log.debug { "request for ${request.url.encodedPath}" }

        val response = client.request(request)

        if (!response.status.isSuccess()) {
            val text = response.bodyAsText()
            log.error { "request error [${response.status}] ${response.request.url}: $text" }
            throw OsuApiException(response, text)
        }

        return if (T::class.isSubclassOf(String::class)) {
            response.bodyAsText() as T
        } else {
            response.body<T>()
        }
    }

    suspend fun HeadersBuilder.setAuth(auth: UserAuth?) {
        val accessToken = if (auth == null) {
            getBotAuth()
        } else {
            if (auth.isExpired()) refreshUserAuth(auth)
            auth.accessToken
        }
        append(HttpHeaders.Authorization, "Bearer $accessToken")
    }

    private data class AuthResult(
        @JsonProperty("expires_in") val expires: Long,
        @JsonProperty("access_token") val accessToken: String,
        @JsonProperty("refresh_token") val refreshToken: String?,
    )
}