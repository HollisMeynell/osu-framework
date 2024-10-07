package org.spring.osu.extended

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.OutputStream
import java.net.Proxy

object OsuWebApi {
    private lateinit var client: HttpClient
    private val userIDReg = "\"https://osu.ppy.sh/users/(?<uid>\\d+)\"".toRegex()
    private fun homeUrl() = "home"
    private fun downloadOszUrl(sid: Long) = "beatmapsets/$sid/download"

    suspend fun visitHomePage(account: OsuWebAccount) {
        val response = client.get {
            url.path(homeUrl())
            headers.setHeader(account)
        }
        if (response.status.isSuccess().not()) {
            throw Exception("Visit home page failed [${response.status.description}]")
        }
        val text = response.bodyAsText()
        userIDReg.find(text)?.let {
            account.userID = it.groups["uid"]?.value?.toLong()
        }
        response.headers.parseCookie(account)
    }

    suspend fun doDownloadOsz(account: OsuWebAccount, output: OutputStream, sid: Long) {
        val statement = client.prepareGet {
            url.path(downloadOszUrl(sid))
            headers.append(HttpHeaders.Referrer, "https://osu.ppy.sh/")
            headers.setHeader(account)
        }

        statement.execute { response ->
            if (response.status.isSuccess().not()) {
                throw Exception("Download osz failed [${response.status.description}]")
            }
            response.bodyAsChannel().copyTo(output)
        }
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
