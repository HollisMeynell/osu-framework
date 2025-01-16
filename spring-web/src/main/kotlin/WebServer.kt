package org.spring.web

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.spring.core.Json
import org.spring.osu.OsuApi
import org.spring.osu.beatmap.mirror.OsuBeatmapMirror
import org.spring.osu.extended.api.OsuWebApi
import org.spring.osu.persistence.OsuDatabases
import org.spring.web.entity.OsuAuth
import kotlin.time.Duration.Companion.minutes

object WebServer {
    lateinit var httpClient: HttpClient
    private val log = KotlinLogging.logger { }

    fun initServer(wait: Boolean = true) {
        val config = WebConfig.loadFromFile()
        startServer(config, wait)
    }

    private fun startServer(config: WebConfig, wait: Boolean) {
        // init database
        HikariConfig().apply {
            jdbcUrl = config.database.url
            driverClassName = config.database.driver
            username = config.database.username
            password = config.database.password
        }.let {
            val db = Database.connect(HikariDataSource(it))
            OsuDatabases.initDataBase(db)
        }

        // init osu api
        val proxy = config.proxy?.toProxy() ?: config.osu.proxy?.toProxy()
        OsuWebApi.init(proxy)
        OsuApi.init(config.osu)
        OsuBeatmapMirror.config = config.mirror

        // init http client
        WebClient.initClient(config.proxy)

        // start server
        val server = embeddedServer(CIO, port = config.server.port) {
            configureJson()
            configureRouting()
            configureHTTP(config.server.cors)
            setupJwt()
            rateLimit()
            initServerRouting()
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            server.stop(500, 800)
        })
        server.start(wait)
    }

    private fun Application.configureJson() {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(Json.mapper))
        }
    }

    private fun Application.configureRouting() {
        install(StatusPages) {
            status(HttpStatusCode.TooManyRequests) { call, _ ->
                val retryAfter = call.response.headers["Retry-After"]
                val result = DataVo(429, "429: Too many requests. Wait for $retryAfter seconds.", retryAfter)
                call.respond(status = HttpStatusCode.OK, result)
            }
            status(HttpStatusCode.NotFound) { call, _ ->
                val result = DataVo(404, "Not Found", null)
                call.respond(HttpStatusCode.OK, result)
            }
            exception<HttpTipsException> { call, cause ->
                if (cause.cuse != null) {
                    org.spring.core.log.error(cause.cuse) { cause.message }
                }
                val result = DataVo(cause.code, cause.message!!, null)
                call.respond(status = HttpStatusCode.OK, result)
            }
            exception<Throwable> { call, cause ->
                org.spring.core.log.error(cause) { "" }
                val result = DataVo(500, cause.message ?: "server error", null)
                call.respond(status = HttpStatusCode.OK, result)
            }
        }
    }

    private fun Application.configureHTTP(cros: List<String>? = null) {
        install(CORS) {
            cros?.forEach {
                allowHost(it)
            }
        }
        install(AutoHeadResponse)
        install(PartialContent)
        install(Compression)
    }

    private fun Application.rateLimit() {
        install(RateLimit) {
            register {
                requestKey { call ->
                    val headers = call.request.headers
                    headers["X-Real-IP"] ?: headers["X-Forwarded-For"] ?: call.request.origin.remoteHost
                }
                rateLimiter(limit = 6000, refillPeriod = 1.minutes)

                requestWeight { call, _ ->
                    return@requestWeight 10
                    /* todo: 实现区分身份的请求权重
                    val user = call.getAuthUser()
                    when {
                        !user.isLogin() -> 20
                        user.role == AuthUser.Role.Bot -> 0
                        user.isAdmin() -> 1
                        else -> 8
                    }

                     */
                }
            }
        }
    }

    private fun Application.initServerRouting() {
        rootPath = "api"
        routing {
            rateLimit {
                public()
                user()
                mirror()
                osu()
                yasunaori()
                authenticate {
                    get("selfInfo") {
                        val u = call.getAuthUser()
                        val auth = OsuAuth.getByID(u.uid)
                        val user = OsuApi.getOwnData(auth!!)
                        call.respond(DataVo(data = user))
                    }
                }
            }
        }
    }
}