package org.spring.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.copyTo
import org.spring.web.service.MirrorService
import org.spring.web.service.UserService
import org.spring.web.service.YasunaoriService
import org.spring.web.service.YasunaoriUserInfo


private val log = KotlinLogging.logger { }
private val client = WebClient.client

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
        val key = call.getDataNullable<String>("key")
        if (key != WebConfig.Instance.server.secret) {
            throw HttpTipsException(403, "no permission")
        }
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
        val uid = call.getDataNullable<Long>("uid")
        val name = call.getDataNullable<String>("name")
        val response = try {
            YasunaoriService.getUser(uid, name, call.getData("mode"))
        } catch (e: Exception) {
            YasunaoriUserInfo("获取用户信息失败: ${e.message}")
        }
        call.respond(response)
    }

    get("beatmap/{bid}") {
        val bid = call.getData<Long>("bid")
        val mods = call.getDataNullable<String>("mods")
        val mode = call.getDataNullable<String>("mode")
        val response = try {
            YasunaoriService.getBeatmap(bid, mods, mode)
        } catch (e: Exception) {
            YasunaoriUserInfo("获取谱面信息失败: ${e.message}")
        }
        call.respond(response)
    }

    get("avatar/{id}") {
        val id = call.getData<String>("id")
        call.respondBytesWriter(
            contentType = ContentType.Image.JPEG
        ) {
            val out = this
            YasunaoriService.outAvatar(id) {
                copyTo(out)
            }
        }
    }
}

fun Route.mirror() = route("mirror") {
    get("fileName/{type}/{bid}") {
        val bid = call.getData<Long>("bid")
        val type = call.getData<String>("type")
        val file = MirrorService.getFileName(bid, type)
        call.respond(file)
    }

    authenticate {
        post("upload/map/{sid}") {
            val read = call.receiveChannel()
            MirrorService.updateFile(3, read)
        }
    }
}