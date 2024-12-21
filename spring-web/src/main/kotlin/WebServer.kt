package org.spring.web

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.spring.core.Json
import org.spring.osu.OsuApi
import org.spring.osu.beatmap.mirror.OsuBeatmapMirror
import org.spring.osu.extended.api.OsuWebApi
import org.spring.osu.persistence.OsuDatabases
import org.spring.web.databases.OsuAuth
import org.spring.web.databases.WebDataBase

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
            WebDataBase.initDataBase(db)
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
            status(HttpStatusCode.NotFound) { call, _ ->
                val result = DataVo(404, "Not Found", null)
                call.respond(HttpStatusCode.OK, result)
            }
            exception<HttpTipsException> { call, cause ->
                if (cause.cuse != null) {
                    log.error(cause.cuse) { cause.message }
                }
                val result = DataVo(cause.code, cause.message!!, null)
                call.respond(status = HttpStatusCode.OK, result)
            }
            exception<Throwable> { call, cause ->
                log.error(cause) { "" }
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
            allowHost("a.yasunaori.be", schemes = listOf("https"))
            allowHost("www.websocket-test.com", schemes = listOf("http"))
        }
        install(AutoHeadResponse)
        install(PartialContent)
        install(Compression)
    }


    private fun Application.initServerRouting() {
        routing {
            route("api") {
                userController()
                public()
                yasunaori()
                mirror()
                authenticate {
                    get("selfInfo") {
                        val u = call.getAuthUser()
                        val auth = OsuAuth.getByID(u!!.uid)
                        val user = OsuApi.getOwnData(auth!!)
                        call.respond(DataVo(data = user))
                    }
                }
            }
        }
    }
}