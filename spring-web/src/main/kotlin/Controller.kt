package org.spring.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
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
                p.parameter?.forEach { (k, v) ->
                    parameters.append(k, v)
                }
            }
            p.headers?.forEach { (k, v) ->
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

fun Route.yasunaori() = route("yasunaori") {
    get("user") {
        val uid = call.getData<Long>("uid")
        val name = call.getData<String>("name")

        call.respond(YasunaoriService.getUser(uid, name, call.getData("mode")))
    }

    get("beatmap/{bid}") {
        val bid = call.getData<Long>("bid")
        val mods = call.getData<String>("mods")
        val mode = call.getData<String>("mode")
        call.respond(YasunaoriService.getBeatmap(bid, mods, mode))
    }

    get("avatar/{id}") {
        val id = call.getData<Long>("id")!!
        call.respondBytesWriter(
            contentType = ContentType.Image.JPEG
        ) {
            val out = this
            val request = client.prepareRequest {
                url("https://a.ppy.sh/$id")
            }

            request.execute {
                if (!it.status.isSuccess()) {
                    val message = it.bodyAsText()
                    throw HttpTipsException(message = message)
                }
                it.bodyAsChannel().copyTo(out)
            }
        }
    }
}