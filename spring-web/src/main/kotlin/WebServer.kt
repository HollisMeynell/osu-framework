package org.spring.web

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.spring.core.Json
import org.spring.osu.OsuApi
import org.spring.osu.extended.api.OsuWebApi
import org.spring.osu.persistence.OsuDatabases
import org.spring.web.databases.OsuAuth

object WebServer {
    private val log = KotlinLogging.logger {  }

    fun initServer(wait: Boolean = true) {
        val config = WebConfig.loadFromFile()
        startServer(config, wait)
    }

    fun startServer(config: WebConfig, wait: Boolean) {
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

        // start server
        val server = embeddedServer(CIO, port = config.server.port) {
            configureJson()
            configureRouting()
            configureHTTP(config.server.cros)
            setupJwt(config.server.secret)

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
            status(HttpStatusCode.NotFound) { call, cause ->
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
        }
        install(AutoHeadResponse)
        install(PartialContent)
        install(Compression)
    }


    fun Application.initServerRouting() {
        routing {
            route("api") {
                get("alumni") {
                    call.respondText("okok")
                }
                userController()
                public()
            }
            authenticate {
                get("selfInfo") {
                    val u = call.authentication.principal<JwtUser>()
                    val auth = OsuAuth.getByID(u!!.uid)
                    val beatmap = OsuApi.getOwnData(auth!!)
                    call.respond(DataVo(data = beatmap))
                }
                get("f") {
                    val type = call.request.httpMethod.value
                    val u = call.authentication.principal<JwtUser>()
                    val name = u?.name ?: "no user"
                    val x = call.parameters.get("o") ?: throw IllegalArgumentException("no o")
                    call.respondText { "$x (${type}) okok~ $name" }
                }
            }

            post("f") {
                call.getData<JwtUser>()
            }
        }
    }
}