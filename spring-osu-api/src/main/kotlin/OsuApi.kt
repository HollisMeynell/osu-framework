@file:Suppress("unused")

package org.spring.osu

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import org.spring.core.json
import org.spring.core.jsonList
import org.spring.core.log
import org.spring.core.synchronizedRun
import org.spring.osu.ApiRequest.setAuth
import org.spring.osu.OsuMode.*
import org.spring.osu.model.*

object OsuApi {
    val lazer = LazerApi

    /********************************* Oauth ******************************************/

    @JvmStatic
    fun getOauthUrl(state: String, vararg scopes: AuthScope): String =
        ApiRequest.getOauthUrl(state, *scopes)

    @JvmStatic
    suspend fun refreshUserAuth(auth: UserAuth): UserAuth =
        ApiRequest.refreshUserAuth(auth)

    /********************************* Beatmap ******************************************/

    @JvmStatic
    suspend fun getBeatmapPacks(
        type: BeatmapPackType? = null,
        cursor: String? = null,
        auth: UserAuth? = null,
    ): List<BeatmapPack> {
        val node = ApiRequest.request<JsonNode>(auth) {
            url.appendPathSegments("beatmaps/packs")
            url.parameters.apply {
                type?.let { append("type", it.describe) }
                cursor?.let { append("cursor_string", it) }
            }
        }
        return node.jsonList("beatmap_packs")
    }

    /**
     * @param [tag] pack tag
     */
    @JvmStatic
    suspend fun getBeatmapPack(
        tag: String,
        legacyOnly: Boolean = false,
        auth: UserAuth? = null,
    ): BeatmapPack {
        return ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps/packs", tag)
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps/lookup")
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

        val node: JsonNode = ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps")
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps", beatmap.toString())
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
        mods: List<OsuMod>? = null,
        auth: UserAuth? = null,
    ): BeatmapDifficultyAttributes {

        var node: JsonNode = ApiRequest.request(auth) {
            method = HttpMethod.Post
            url.appendPathSegments("beatmaps", beatmap.toString(), "attributes")
            url.parameters.apply {
                if (mode != Default) {
                    append("ruleset", mode.describe)
                }
                if (mods.isNullOrEmpty()) {
                    // do nothing
                } else if (mods.contains(OsuMod.None)) {
                    append("mods[]", "NM")
                } else {
                    mods.forEach { m -> append("mods[]", m.acronym) }
                }
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("users", user.toString(), "beatmapsets", type.type)
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("users", user.toString(), "beatmapsets", UserBeatmapType.MostPlayed.type)
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
        return ApiRequest.request {
            url.appendPathSegments("beatmapsets/search")
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
    suspend fun getBeatmapset(beatmapsets: Long, auth: UserAuth? = null): Beatmapset {
        return ApiRequest.request(auth) {
            url.appendPathSegments("beatmapsets", beatmapsets.toString())
        }
    }

    /********************************* User ******************************************/
    @JvmStatic
    suspend fun getOwnData(auth: UserAuth, mode: OsuMode = Default): User {
        return ApiRequest.request<User>(auth) {
            url.appendPathSegments("me")
            if (mode != Default) url.appendPathSegments(mode.describe)
        }
    }

    @JvmStatic
    suspend fun getUserKudosu(
        user: Long,
        auth: UserAuth? = null,
        limit: Int = 100,
        offset: Int = 0,
    ): List<KudosuHistory> {
        return ApiRequest.request(auth) {
            url.appendPathSegments("users", user.toString(), "kudosu")
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("users", userKey)
            if (mode != Default) url.appendPathSegments(mode.describe)
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
        return ApiRequest.request<JsonNode> {
            url.appendPathSegments("users")
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
        return ApiRequest.request<String>(auth) {
            method = HttpMethod.Post
            url.appendPathSegments("session/verify")
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps", beatmap.toString(), "scores/users", user.toString())
            url.parameters.apply {
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mods.contains(OsuMod.None))
                    append("mods[]", "NM")
                else
                    mods.forEach { m -> append("mods[]", m.acronym) }

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
        val node: JsonNode = ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps", beatmap.toString(), "scores/users", user.toString(), "all")
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("beatmaps", beatmap.toString(), "scores")
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
                    mods.forEach { m -> append("mods[]", m.acronym) }
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("matches")
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("matches", match.toString())
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
        return ApiRequest.request(auth) {
            url.appendPathSegments("users", user.toString(), "scores", type.type)
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
    suspend fun getFriend(auth: UserAuth): List<Friend> {
        return ApiRequest.request(auth) {
            url.appendPathSegments("friends")
        }
    }

    @JvmStatic
    suspend fun getSeasonalBackgrounds(): SeasonalBackgrounds {
        return ApiRequest.request { url.appendPathSegments("seasonal-backgrounds") }
    }

    /**
     * must require the use of tokens that have a Resource Owner.
     */
    @JvmStatic
    suspend fun downloadScore(
        auth: UserAuth,
        score: Long,
        output: ByteWriteChannel
    ) {
        val stream = ApiRequest.client.prepareGet {
            url.appendPathSegments("scores", score.toString(), "download")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            headers.setAuth(auth)
        }
        stream.execute { response ->
            if (!response.status.isSuccess()) {
                val text = response.bodyAsText()
                log.error { "request error [${response.status}] ${response.request.url}: $text" }
                throw OsuApiException(response, text)
            }
            response.bodyAsChannel().copyAndClose(output)
        }
    }

    /***************************************************************************/
    @JvmStatic
    fun init(config: OsuApiConfig) {
        ApiRequest.init(config)
    }

    private data class AuthResult(
        @JsonProperty("expires_in") val expires: Long,
        @JsonProperty("access_token") val accessToken: String,
        @JsonProperty("refresh_token") val refreshToken: String?,
    )
}