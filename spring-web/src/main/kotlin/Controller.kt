package org.spring.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.jvm.javaio.copyTo
import org.spring.web.service.UserService
import org.spring.web.service.YasunaoriService


private val log = KotlinLogging.logger { }
private val client = HttpClient()

fun Route.userController() = route("user") {
    get("login") {
        val code = call.parameters["code"] ?: throw HttpTipsException(message = "no code")
        val result = try {
            val user = UserService.login(code)
            DataVo(message = "登陆成功", data = user)
        } catch (e: Exception) {
            log.error { }
            throw HttpTipsException(message = "登陆失败 ${e.message}")
        }
        call.respond(result)
    }
}

fun Route.public() = route("public") {

    get("getOauthUrl") {
        call.respond(UserService.oauthUrl())
    }
    post("proxy/{key?}") {
        val p = call.getData<ProxyDto>()
        val response = client.prepareRequest {
            method = HttpMethod.parse(p.method)
            url {
                url(p.url)
                p.parameter?.forEach { k, v ->
                    parameters.append(k, v)
                }
            }
            p.headers?.forEach { k, v ->
                headers.append(k, v)
            }
            p.body?.let { setBody(it) }
        }

        response.execute { req ->
            call.response.status(req.status)
            req.headers.forEach { k, v ->
                call.response.header(k, v.joinToString("; "))
            }
            val contentType = req.headers["Content-Type"]?.let { ContentType.parse(it) }
            call.respondOutputStream(contentType = contentType, status = req.status) {
                req.bodyAsChannel().copyTo(this)
            }
        }
    }
}