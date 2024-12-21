package org.spring.web

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import org.spring.osu.extended.api.OsuWebApi
import org.spring.osu.persistence.model.OsuWebUserRecord
import org.spring.web.databases.OsuAuth
import org.spring.web.service.OsuMirrorService
import org.spring.web.service.UserService
import org.spring.web.service.YasunaoriService
import org.spring.web.service.YasunaoriUserInfo


private val log = KotlinLogging.logger { }
private val client = WebClient.client

fun Route.userController() = route("user") {
    get("login/osu") {
        val session = call.getData<String>("code")
        val user = OsuWebApi.checkAccount(OsuWebUserRecord(session))
            ?: throw HttpTipsException(400, "登陆失败, 请检查账号是否正确")
        call.respond(DataVo(data = "用户<$user>录入成功"))
    }

    get("login") {
        val code = call.getDataNullable<String>("code") ?: throw HttpTipsException(message = "no code")
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
            YasunaoriService.getUser(uid, name, call.getDataNullable("mode"))
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
        val file = OsuMirrorService.getFilePath(bid, type)
        call.respond(file.fileName.toString())
    }

    /**
     * type: bg, song, osufile
     */
    get("beatmap/{type}/{bid}") {
        val type = call.getData<String>("type")
        val bid = call.getData<Long>("bid")
        val path = OsuMirrorService.getFilePath(bid, type)
        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition
                .Attachment
                .withParameter(ContentDisposition.Parameters.FileName, "${path.fileName}")
                .toString()
        )
        call.respond(LocalPathContent(path))
    }

    get("beatmapset/{sid}") {
        val sid = call.getData<Long>("sid")
        val video = call.getDataNullable<Boolean>("video") ?: true
        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition
                .Attachment
                .withParameter(ContentDisposition.Parameters.FileName, "$sid.osz")
                .toString()
        )
        call.respondBytesWriter {
            OsuMirrorService.getZipOutput(sid, toOutputStream(), video)
        }
    }

    /**
     * 使用sid参数, 可以多个
     */
    get("beatmapset/all") {
        val video = call.getDataNullable<Boolean>("video") ?: true
        val sids = call.request
            .queryParameters
            .getAll("sid")
            ?.map { it.toLong() }
            ?: throw HttpTipsException(400, "no sid")
        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition
                .Attachment
                .withParameter(ContentDisposition.Parameters.FileName, "package.osz")
                .toString()
        )
        call.respondBytesWriter {
            OsuMirrorService.getZipOutput(sids, toOutputStream(), video)
        }
    }

    authenticate("bot") {
        get("async/beatmap/{bid}") {
            val bid = call.getData<Long>("bid")
            OsuMirrorService.asyncDownload(bid)
            call.respond(DataVo(data = "正在下载"))
        }
    }

    authenticate {
        post("upload/beatmap/{sid}") {
            val read = call.receiveChannel()
            OsuMirrorService.updateFile(3, read)
        }

        get("count") {
            val user = call.getAuthUser()
            if (!user.isAdmin()) throw PermissionException()
            val count = OsuMirrorService.getAllCount()
            call.respond(DataVo(data = count))
        }
    }
}