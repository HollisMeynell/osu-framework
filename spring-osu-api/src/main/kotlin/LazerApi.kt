@file:Suppress("DuplicatedCode", "unused")

package org.spring.osu

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.*
import org.spring.core.jsonList
import org.spring.osu.OsuMode.Default
import org.spring.osu.model.*

object LazerApi {
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
    ): List<LazerScore> {
        return ApiRequest.request(auth,true) {
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
        mods: List<LazerMod> = emptyList(),
        auth: UserAuth? = null,
    ): LazerBeatmapUserScore {
        return ApiRequest.request(auth, true) {
            url.appendPathSegments("beatmaps", beatmap.toString(), "scores/users", user.toString())
            url.parameters.apply {
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
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
    ): List<LazerScore> {
        val node: JsonNode = ApiRequest.request(auth, true) {
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
        mods: List<LazerMod> = emptyList(),
        type: String? = null,
        auth: UserAuth? = null,
    ): LazerBeatmapScores {
        return ApiRequest.request(auth, true) {
            url.appendPathSegments("beatmaps", beatmap.toString(), "scores")
            url.parameters.apply {
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                type?.let { append("type", it) }
                mods.forEach { m -> append("mods[]", m.acronym) }
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
    suspend fun getBeatmapSoloScores(
        beatmap: Long,
        mode: OsuMode = Default,
        mods: List<LazerMod> = emptyList(),
        type: String? = null,
        auth: UserAuth? = null,
    ): LazerBeatmapScores {
        return ApiRequest.request(auth, true) {
            url.appendPathSegments("beatmaps", beatmap.toString(), "solo-scores")
            url.parameters.apply {
                if (mode != Default) {
                    append("mode", mode.describe)
                    append("ruleset", mode.describe)
                }
                mods.forEach { m -> append("mods[]", m.acronym) }
                type?.let { append("type", it) }
            }
        }
    }

    suspend fun getFriend(auth: UserAuth): List<LazerFriend> {
        return ApiRequest.request(auth, true) {
            url.appendPathSegments("friends")
        }
    }
}