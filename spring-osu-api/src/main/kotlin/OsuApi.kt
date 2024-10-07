@file:Suppress("unused")

package org.spring.osu

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import org.spring.core.*
import org.spring.osu.OsuMode.*
import org.spring.osu.module.*
import kotlin.io.path.Path

object OsuApi {
    private lateinit var config: OsuApiConfig
    private lateinit var bot: UserAuth
    private lateinit var client: HttpClient
    private val log = KotlinLogging.logger { }
    private val clientID
        get() = config.clientID
    private val clientToken
        get() = config.clientToken

    private val isInited: Boolean
        get() = ::client.isInitialized

    /********************************* Oauth ******************************************/

    @JvmStatic
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

        val response = client.post {
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

    @JvmStatic
    suspend fun refreshUserAuth(auth: UserAuth): UserAuth {
        val body = Parameters.build {
            append("client_id", clientID.toString())
            append("client_secret", clientToken)
            append("redirect_uri", config.redirectUri)
            if (auth.accessToken == null) {
                append("code", auth.refreshToken!!)
                append("grant_type", "authorization_code")
            } else {
                append("refresh_token", auth.refreshToken!!)
                append("grant_type", "refresh_token")
            }
        }


        val response = client.post {
            url("https://osu.ppy.sh/oauth/token")
            headers {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.FormUrlEncoded)
            }
            setBody(FormDataContent(body))
        }

        if (response.status.isSuccess().not()) {
            val text = response.bodyAsText()
            val user = auth.osuName ?: "{new user}"
            log.error { "更新 $user 令牌时请求失败[${response.status.description}]: $text" }
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

    /********************************* Beatmap ******************************************/

    @JvmStatic
    suspend fun getBeatmapPacks(
        type: BeatmapPackType? = null,
        cursor: String? = null,
        auth: UserAuth? = null,
    ): List<BeatmapPack> {
        val node = request<JsonNode>(auth) {
            url.path("beatmaps/packs")
            url.parameters.apply {
                type?.let { append("type", it.describe) }
                cursor?.let { append("cursor_string", it) }
            }
        }
        return node.jsonList("beatmap_packs")
    }

    @JvmStatic
    suspend fun getBeatmapPack(
        pack: String,
        legacyOnly: Boolean = false,
        auth: UserAuth? = null,
    ): BeatmapPack {
        return request(auth) {
            url.path("beatmaps/packs", pack)
            if (legacyOnly) {
                url.parameters.append("legacy_only", "1")
            } else {
                url.parameters.append("legacy_only", "0")
            }
        }
    }

    /**
     * get beatmap from [checksum]
     * just one not null
     */
    @JvmStatic
    suspend fun lookupBeatmap(
        name: String? = null,
        checksum: String? = null,
        id: Long? = null,
        auth: UserAuth? = null,
    ): Beatmap {
        return request(auth) {
            url.path("beatmaps/lookup")
            url.parameters.apply {
                name?.let { append("filename", it) }
                checksum?.let { append("checksum", it) }
                id?.let { append("id", it.toString()) }
            }
        }
    }

    @JvmStatic
    suspend fun getBeatmaps(vararg ids: Long, idList: List<Long>? = null, auth: UserAuth? = null): List<Beatmap> {
        if (idList == null && ids.isEmpty()) return emptyList()
        val allID = idList ?: ids.toList()

        if (allID.size > 50) {
            val splitList = allID.chunked(50) {
                suspend { getBeatmaps(idList = it, auth = auth) }
            }

            return synchronizedRun(splitList) {
                log.error(it) { "failed to obtain beatmap in batches" }
            }.flatten()
        }

        val node: JsonNode = request(auth) {
            url.path("beatmaps")
            url.parameters.apply {
                allID.forEach { append("ids[]", it.toString()) }
            }
        }
        return node.jsonList("beatmaps")
    }

    /**
     * @param beatmap [beatmap] beatmap id
     */
    @JvmStatic
    suspend fun getBeatmap(beatmap: Long, auth: UserAuth? = null): Beatmap {
        return request(auth) {
            url.path("beatmaps", beatmap.toString())
        }
    }

    /**
     * return class [BeatmapDifficultyAttributesOsu] [BeatmapDifficultyAttributesTaiko]
     *  [BeatmapDifficultyAttributesMania] [BeatmapDifficultyAttributesFruits]
     *
     * use
     * ```
     * when(return) {
     *     is BeatmapDifficultyAttributesOsu -> ...
     *     is BeatmapDifficultyAttributesTaiko -> ...
     *     ...
     * }
     * ```
     * @param beatmap [beatmap] beatmap id
     * @return is a subclass of [BeatmapDifficultyAttributes],
     */
    @JvmStatic
    suspend fun getBeatmapAttributes(
        beatmap: Long,
        mode: OsuMode = Default,
        mods: List<OsuMod> = emptyList(),
        auth: UserAuth? = null,
    ): BeatmapDifficultyAttributes {

        var node: JsonNode = request(auth) {
            method = HttpMethod.Post
            url.path("beatmaps", beatmap.toString(), "attributes")
            url.parameters.apply {
                if (mode != Default) {
                    append("ruleset", mode.describe)
                }
                if (mods.contains(OsuMod.None))
                    append("mods[]", "NM")
                else
                    mods.forEach { m -> append("mods[]", m.mod) }
            }
        }
        node = node.get("attributes")
        return when (mode) {
            Osu -> node.json<BeatmapDifficultyAttributesOsu>()
            Catch -> node.json<BeatmapDifficultyAttributesFruits>()
            Taiko -> node.json<BeatmapDifficultyAttributesTaiko>()
            Mania -> node.json<BeatmapDifficultyAttributesMania>()
            Default -> when {
                node.has("aim_difficulty") -> node.json<BeatmapDifficultyAttributesOsu>()
                node.has("stamina_difficulty") -> node.json<BeatmapDifficultyAttributesTaiko>()
                node.has("approach_rate") -> node.json<BeatmapDifficultyAttributesFruits>()
                else -> node.json<BeatmapDifficultyAttributesMania>()
            }
        }
    }

    @JvmStatic
    suspend fun getUserBeatmaps(
        user: Long,
        type: UserBeatmapType,
        limit: Int = 100,
        offset: Int = 0,
        auth: UserAuth? = null,
    ): List<Beatmapset> {
        if (type == UserBeatmapType.MostPlayed) throw IllegalArgumentException("MostPlayed is not supported, use getUserMostPlayedBeatmaps")
        return request(auth) {
            url.path("users", user.toString(), "beatmapsets", type.type)
            url.parameters.apply {
                append("limit", limit.toString())
                append("offset", offset.toString())
            }
        }
    }

    @JvmStatic
    suspend fun getUserMostPlayedBeatmaps(
        user: Long,
        limit: Int = 100,
        offset: Int = 0,
        auth: UserAuth? = null,
    ): List<BeatmapPlaycount> {
        return request(auth) {
            url.path("users", user.toString(), "beatmapsets", UserBeatmapType.MostPlayed.type)
            url.parameters.apply {
                append("limit", limit.toString())
                append("offset", offset.toString())
            }
        }
    }

    /**
     * todo: search parameters
     */
    @JvmStatic
    internal suspend fun searchBeatmaps(): JsonNode {
        return request {
            url.path("beatmapsets/search")
            url.parameters.apply {
                append("query", "Giniro Hikousen")
            }
        }
    }

    /**
     * Because <a href="https://osu.ppy.sh/docs/#get-apiv2beatmapsetsbeatmapset">there</a> is no doc,
     * there is no way to know whether there are other parameters
     */
    @JvmStatic
    suspend fun getBeatmapset(beatmapsets: Long): Beatmapset {
        return request {
            url.path("beatmapsets", beatmapsets.toString())
        }
    }

    /********************************* User ******************************************/
    @JvmStatic
    suspend fun getOwnData(auth: UserAuth, mode: OsuMode = Default): User {
        return request<User>(auth) {
            url.path("me")
            if (mode != Default) url.path(mode.describe)
        }.apply {
            auth.osuID = id
            auth.osuName = username
        }
    }

    @JvmStatic
    suspend fun getUserKudosu(
        user: Long,
        auth: UserAuth? = null,
        limit: Int = 100,
        offset: Int = 0,
    ): List<KudosuHistory> {
        return request(auth) {
            url.path("users", user.toString(), "kudosu")
            url.parameters.apply {
                append("limit", limit.toString())
                append("offset", offset.toString())
            }
        }
    }

    @JvmStatic
    suspend fun getUser(
        user: Long? = null,
        name: String? = null,
        auth: UserAuth? = null,
        mode: OsuMode = Default,
    ): User {
        val userKey: String? = user?.toString() ?: if (name != null) "@$name" else null
        if (userKey == null && auth?.isAuthed() == true) return getOwnData(auth, mode)
        if (userKey == null) throw IllegalArgumentException("user and name can't be null at the same time")
        return request(auth) {
            url.path("users", userKey)
            if (mode != Default) url.path(mode.describe)
        }
    }

    @JvmStatic
    suspend fun getUsers(includeVariantStatistics: Boolean, vararg userID: Long): List<User> {
        return getUsers(includeVariantStatistics, userID.toList())
    }

    @JvmStatic
    suspend fun getUsers(
        includeVariantStatistics: Boolean = false,
        userID: List<Long>,
    ): List<User> {
        return request<JsonNode> {
            url.path("users")
            url.parameters.apply {
                userID.forEach { append("ids[]", it.toString()) }
                append("include_variant_statistics", includeVariantStatistics.toString())
            }
        }.jsonList("users")
    }

    /**
     * I do not know what is this
     * default return is empty string
     */
    @JvmStatic
    suspend fun sessionVerify(auth: UserAuth): String {
        return request<String>(auth) {
            method = HttpMethod.Post
            url.path("session/verify")
        }
    }

    /********************************* Score ******************************************/

    /**
     * @param beatmap [beatmap] beatmap id
     * @param user [user] user id
     */
    @JvmStatic
    suspend fun getUserBeatmapScore(
        beatmap: Long,
        user: Long,
        legacyOnly: Boolean = false,
        mode: OsuMode = Default,
        mods: List<OsuMod> = emptyList(),
        auth: UserAuth? = null,
    ): BeatmapUserScore {
        return request(auth) {
            url.path("beatmaps", beatmap.toString(), "scores/users", user.toString())
            url.parameters.apply {
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mods.contains(OsuMod.None))
                    append("mods[]", "NM")
                else
                    mods.forEach { m -> append("mods[]", m.mod) }

            }
        }
    }

    /**
     * @param beatmap [beatmap] beatmap id
     * @param user [user] user id
     */
    @JvmStatic
    suspend fun getUserBeatmapScores(
        beatmap: Long,
        user: Long,
        legacyOnly: Boolean = false,
        mode: OsuMode = Default,
        auth: UserAuth? = null,
    ): List<Score> {
        val node: JsonNode = request(auth) {
            url.path("beatmaps", beatmap.toString(), "scores/users", user.toString(), "all")
            url.parameters.apply {
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
            }
        }
        return node.jsonList("scores")
    }

    /**
     * @param beatmap [beatmap] beatmap id
     * @param mods I don't know if it will work
     * @param type Beatmap score ranking type, I don't know if it will work
     */
    @JvmStatic
    suspend fun getBeatmapScores(
        beatmap: Long,
        legacyOnly: Boolean = false,
        mode: OsuMode = Default,
        mods: List<OsuMod> = emptyList(),
        type: String? = null,
        auth: UserAuth? = null,
    ): BeatmapScores {
        return request(auth) {
            url.path("beatmaps", beatmap.toString(), "scores")
            url.parameters.apply {
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                type?.let { append("type", it) }
                if (mods.contains(OsuMod.None))
                    append("mods[]", "NM")
                else
                    mods.forEach { m -> append("mods[]", m.mod) }
            }
        }
    }

    /**
     * just for lazer
     * @param beatmap [beatmap] beatmap id
     * @param mods I don't know if it will work
     * @param type Beatmap score ranking type, I don't know if it will work
     */
    @JvmStatic
    suspend fun getBeatmapScoresLazer(
        beatmap: Long,
        mode: OsuMode = Default,
        mods: List<OsuMod> = emptyList(),
        type: String? = null,
        auth: UserAuth? = null,
    ): BeatmapScores {
        return request(auth) {
            url.path("beatmaps", beatmap.toString(), "solo-scores")
            url.parameters.apply {
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                if (mods.contains(OsuMod.None))
                    append("mods[]", "NM")
                else
                    mods.forEach { m -> append("mods[]", m.mod) }
                type?.let { append("type", it) }
            }
        }
    }

    @JvmStatic
    suspend fun getMatches(
        limit: Int = 50,
        sortDesc: MatchSearch.SortType = MatchSearch.SortType.Desc,
        cursor: String? = null,
        auth: UserAuth? = null,
    ): MatchSearch {
        return request(auth) {
            url.path("matches")
            url.parameters.apply {
                append("limit", limit.toString())
                append("sort", sortDesc.sort)
                cursor?.let { append("cursor_string", it) }
            }
        }
    }

    /**
     * for stable multi play
     */
    @JvmStatic
    suspend fun getMatch(
        match: Long,
        limit: Int = 100,
        before: Long? = null,
        after: Long? = null,
        auth: UserAuth? = null,
    ): Match {
        return request(auth) {
            url.path("matches", match.toString())
            url.parameters.apply {
                append("limit", limit.toString())
                before?.let { append("before", it.toString()) }
                after?.let { append("after", it.toString()) }
            }
        }
    }

    @JvmStatic
    suspend fun getUserScores(
        user: Long,
        type: UserScoreType,
        mode: OsuMode = Default,
        limit: Int = 100,
        offset: Int = 0,
        legacyOnly: Boolean = false,
        includeFails: Boolean = false,
        auth: UserAuth? = null,
    ): List<Score> {
        return request(auth) {
            url.path("users", user.toString(), "scores", type.type)
            url.parameters.apply {
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                append("limit", limit.toString())
                append("offset", offset.toString())
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (includeFails) append("include_fails", "1") else append("include_fails", "0")
            }
        }
    }

    /********************************* Other ******************************************/

    @JvmStatic
    suspend fun getFriend(auth: UserAuth): List<User> {
        return request(auth) {
            url.path("friends")
        }
    }

    @JvmStatic
    suspend fun getSeasonalBackgrounds(): SeasonalBackgrounds {
        return request { url.path("seasonal-backgrounds") }
    }

    @JvmStatic
    suspend fun downloadScore(
        auth: UserAuth,
        score: Long,
    ): ByteArray {
        return request(auth) {
            url.path("scores", score.toString(), "download")
        }
    }

    /***************************************************************************/
    @JvmStatic
    fun init(config: OsuApiConfig) {
        this.config = config
        client = HttpClient(CIO) {
            // proxy
            if (config.proxyUrl != null) {
                engine {
                    proxy = ProxyBuilder.http(config.proxyUrl)
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

        bot = object : UserAuth {
            override var osuID: Long? = clientID
            override var osuName: String? = null
            override var refreshToken: String? = clientToken
            override var accessToken: String? = null
            override var expires: Long = 0
            override fun isAuthed() = false
            override suspend fun update() {}
        }
    }

    private suspend inline fun <reified T> request(auth: UserAuth? = null, build: HttpRequestBuilder.() -> Unit): T {
        if (!isInited) throw OsuApiNotInitException()

        val request = HttpRequestBuilder().apply(build)

        request.apply {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            headers.setAuth(auth)
        }

        val response = client.request(request)

        if (!response.status.isSuccess()) {
            val text = response.bodyAsText()
            log.error { "request error [${response.status}] ${response.request.url}: $text" }
            throw OsuApiException(response, text)
        }

        return response.body<T>()
    }

    private suspend fun HeadersBuilder.setAuth(auth: UserAuth?) {
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

fun main() {
    val config = OsuApiConfig(
        redirectUri = "http://1.116.209.39:20006/oauth",
        clientID = 12693,
        clientToken = "456nIIj0RKUOO6WuFFCn4SAucdn22pU1rr9doz7X",
        proxyUrl = "http://127.0.0.1:7890"
    )
    OsuApi.init(config)
    val local = Path("/home/spring/a.osz")
//    val auth = UserAuth.UserAuthImpl(
//        accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxMjY5MyIsImp0aSI6ImYyMDljZDIwYWY1OTQ2OWJkZTBjMWM5NjU4ZWJiZjM4NjA2Nzc3YWFlZGM1NjVkNDA1MzRjNTUwYTRkYWVkZjFkYTRkMzg3ZmU2NTYzNDRjIiwiaWF0IjoxNzI3NDI2MjAzLjEyMzgyMywibmJmIjoxNzI3NDI2MjAzLjEyMzgyNSwiZXhwIjoxNzI3NTEyNjAzLjEwOTQ3Miwic3ViIjoiMTcwNjQzNzEiLCJzY29wZXMiOlsiZnJpZW5kcy5yZWFkIiwiaWRlbnRpZnkiLCJwdWJsaWMiXX0.X82jKQEOviPXPcS5IYbT7xh0wSW0sMMkqELXdCwrV89KGpIoKM41kBlIU_Vhaarwv2EpJv22eJ7EJEMKDMTb526N7PI9jeqp_d8ak7eqgxq3tc7jaXBSU9G92ocGGIkYW2T-ol4eQEUMwWF61A0Hbl9enxtoYIa3FOub78Rs7zWy8VJCvkzj-byXduRzPptSZ-bp_9e9NtR8qNEXkXdNsrHBegZTxw-N6Q5nOVwBqQSYI3e8EYruIHlby9Iuw9LCgpaJTEjFskm_AT5oW4wHl9N3WxwTkYPWWVPU1pfVs48krKqP6GQRToXdoKd6bHf5rd79uOQYcLL0hF-1YP-6SQwjlUxGZvt-PMN6DG41PR_Np25yvL-uhz6aIQTVLeOXSodCiCPNTiKFCMq2awq06dqFBS7buaKHlkZ_KKoL5iT_qipQuWCeksutdVnXqPWjOLb_F5KDKn1GS1wgkBDJjpHADcUuzzxXExdFJ6acZY0L1U9Fk1IvmbapDPU3_xo1xAe88kmUOA5_Iw8OiwNTLDrywLLuI4lFNh_jOV-tdv61bZPhKoxA_bDD3n6JQIPcv3MNFZ2vTFTmvMiTc2Ixl4dTtAcWVINyVo5jRH8yLzCtsQAIY4K0_S55hf_Pk7ezddRPCN_2kOM_KawQc9qTs51zsgQ9Sh3OQnpwqthlQPs",
//        expires = 1727512603360,
//    )
    runBlocking {
        tryRun {
            val auth = object : UserAuth {
                override var osuID: Long? = 1
                override var osuName: String? = ""
                override var refreshToken: String? = ""
                override var accessToken: String? =
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxMjY5MyIsImp0aSI6ImFmMDFkZmU2Mjc2YmQ2MmExYmU0NmFmYTZjODU3YTA4ODIyN2NlMzI2NGRkMGQzNDJjMzM2ZDUzMGM5NTM1MDk0MjJlN2IwMmQ2YzliNzQxIiwiaWF0IjoxNzI3OTIzOTU0Ljk0ODE3NSwibmJmIjoxNzI3OTIzOTU0Ljk0ODE3NywiZXhwIjoxNzI4MDEwMzU0LjkzNDgyLCJzdWIiOiIxNzA2NDM3MSIsInNjb3BlcyI6WyJjaGF0LnJlYWQiLCJjaGF0LndyaXRlIiwiY2hhdC53cml0ZV9tYW5hZ2UiLCJmb3J1bS53cml0ZSIsImZyaWVuZHMucmVhZCIsImlkZW50aWZ5IiwicHVibGljIl19.d4hDeIqxZgmzfAkxnCCzihBolskZFdnzx5Twnr-vWSu9FxjIjt-BmcEeCnyOEGgtV8OLIivHBhER2__pQc09RcYtUmivUDRBvuk8A-_XFDI7krTUajSR1Hf_5tG1qoJOj8qMzy2sJnjNWKA97yTZH_YVc9JAS4w4M0qLtvGJUyPQMCJD0lTNyW50jCdidWABKoSNIjUVX2CwDcdTcMMh8WifAzFwqW9hWqOakRJvF4RTj-ry9r1ZYFMwdL5XY8rwiIrHWwqnIroJz20GntymIuGyilNHA6M2q98pFpHz9PoxlouvYYO-MHjV09MFQ7KQ-eA9Qn7anOCjoGPqSckXUA4NmU2RcA5n1jgclhZEhneioWLF8FDQKdJDInO4yhB8EKdEsCvpRoAKSR8AtRgXwk9bMsaDwNA_D9rcTsNMCHQLm1hMHQNnnMEpFwjEkvl-r86hi1uu-Bk38nkUrZTQA5zNZsAOpJ8gHXXMUhsfbZ0eX9dzcx9pmqHasII85hPotREKqD-08up6rrfk1oLVJjDBho80OfrxUoPrg46Tm3wDMp1hmkc6MpfOJiJkJ91u7iUhm1QzCepklouJuZsNEeHurSPEAD9r1ZEeBwV5Cih676Mj5RFENjIYIPu2zgjuZU4YRXvRW11JwTPjdmZ51aM1CUA3qSCLWNiog0XdX2I"
                override var expires: Long = 1728010355379

                override suspend fun update() {
                    println(refreshToken)
                    println(accessToken)
                    println(expires)
                }
            }
            val x = OsuApi.getFriend(auth)
            println(x.toJson())
        }
    }
}