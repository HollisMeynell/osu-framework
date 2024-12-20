package org.spring.osu.extended.api

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import org.spring.core.json
import org.spring.osu.extended.Replay
import org.spring.osu.extended.model.OsuWebAccount
import org.spring.osu.extended.model.WebUser
import java.net.Proxy

object OsuWebApi {
    private lateinit var client: HttpClient
    private val csrfReg = "var csrf = \"(?<csrf>\\w+)\";".toRegex()
    private val userInfoReg =
        "<script id=\"json-current-user\".*>\\n\\s*(?<json>\\{.*})\\n</script>".toRegex(RegexOption.MULTILINE)

    suspend fun checkAccount(account: OsuWebAccount): String? {
        val user = try {
            visitHomePage(account)
        } catch (e: Exception) {
            return null
        }
        return user?.username
    }

    suspend fun visitHomePage(account: OsuWebAccount): WebUser? {
        val response = client.get {
            url.path("home")
            headers.setHeader(account)
        }
        if (response.status.isSuccess().not()) {
            throw Exception("Visit home page failed [${response.status.description}]")
        }
        val text = response.bodyAsText()
        csrfReg.find(text)?.let {
            account.csrf = it.groups["csrf"]?.value
        }
        val user = userInfoReg.find(text)?.let {
            it.groups["json"]?.value?.json<WebUser>()?.apply {
                account.userID = this.id
                account.name = this.username
            }
        }
        response.headers.parseCookie(account)
        return user
    }

    suspend fun getMostPlayed(account: OsuWebAccount, limit: Int = 100, offset: Int = 0): JsonNode {
        val response = client.get {
            url.path("users/${account.userID}/beatmapsets/most_played")
            parameter("limit", limit)
            parameter("offset", offset)
        }
        response.headers.parseCookie(account)
        return response.body()
    }

    suspend fun doDownloadOsz(account: OsuWebAccount, output: ByteWriteChannel, sid: Long) {
        val statement = client.prepareGet {
            url.path("beatmapsets/$sid/download")
            headers.append(HttpHeaders.Referrer, "https://osu.ppy.sh/")
            headers.setHeader(account)
        }

        statement.execute { response ->
            if (response.status.isSuccess().not()) {
                throw Exception("Download osz failed [${response.status.description}]")
            }
            response.bodyAsChannel().copyAndClose(output)
        }
    }

    suspend fun doDownloadReplay(account: OsuWebAccount, scoreID: Long): Replay {
        val statement = client.prepareGet {
            url.path("scores/$scoreID/download")
            headers.append(HttpHeaders.Referrer, "https://osu.ppy.sh/")
            headers.setHeader(account)
        }
        var replay: Replay? = null
        statement.execute { response ->
            if (response.status.isSuccess().not()) {
                throw Exception("Download replay failed [${response.status.description}]")
            }
            response.bodyAsChannel().toInputStream().use {
                replay = Replay(it)
            }
        }

        return replay ?: throw Exception("Download replay failed.")
    }

    suspend fun doDownloadOsuFile(bid: Long): ByteArray {
        val response = client.get {
            url.path("osu/$bid")
        }

        if (response.status.isSuccess().not()) {
            throw Exception("Download osu file failed [${response.status.description}]")
        }

        return response.readRawBytes()
    }

    suspend fun doDownloadAvatar(account: OsuWebAccount, user: Long): ByteArray {
        val response = client.get("https://a.ppy.sh/$user") {
            headers.setHeader(account)
        }

        if (response.status.isSuccess().not()) {
            throw Exception("Download avatar [${response.status.description}]")
        }

        return response.readRawBytes()
    }

    private suspend fun Headers.parseCookie(account: OsuWebAccount) {
        val setCookie = get("set-cookie") ?: return
        val cookies = setCookie.split(';')
        for (cookie in cookies) {
            val it = cookie.trim()
            if (it.startsWith("osu_session=")) {
                account.session = it.substringAfter("osu_session=")
                break
            }
        }
        account.update()
    }

    private fun HeadersBuilder.setHeader(account: OsuWebAccount) {
        append(HttpHeaders.Cookie, "osu_session=${account.session}")
    }

    fun init(proxyConfig: Proxy? = null) {
        client = HttpClient {
            if (proxyConfig != null) {
                engine { proxy = proxyConfig }
            }
            defaultRequest {
                url("https://osu.ppy.sh/")
                headers { accept(ContentType.Application.FormUrlEncoded) }
            }
        }
    }
}