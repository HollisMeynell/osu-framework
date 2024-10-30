@file:Suppress("DuplicatedCode", "unused")

package org.spring.osu

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.path
import org.spring.core.jsonList
import org.spring.osu.OsuMode.Default
import org.spring.osu.model.LazerBeatmapScores
import org.spring.osu.model.LazerBeatmapUserScore
import org.spring.osu.model.LazerMod
import org.spring.osu.model.LazerScore
import org.spring.osu.model.UserAuth

object LazerApi {
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
            url.path("beatmaps", beatmap.toString(), "scores/users", user.toString())
            url.parameters.apply {
                if (mode != Default) {
                    append("ruleset", mode.describe)
                }
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                mods.forEach { m -> append("mods[]", m.type) }
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
            url.path("beatmaps", beatmap.toString(), "scores/users", user.toString(), "all")
            url.parameters.apply {
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mode != Default) {
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
            url.path("beatmaps", beatmap.toString(), "scores")
            url.parameters.apply {
                if (legacyOnly) append("legacy_only", "1") else append("legacy_only", "0")
                if (mode != Default) {
                    append("ruleset", mode.describe)
                }
                type?.let { append("type", it) }
                mods.forEach { m -> append("mods[]", m.type) }
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
            url.path("beatmaps", beatmap.toString(), "solo-scores")
            url.parameters.apply {
                if (mode != Default) {
                    append("ruleset", mode.describe)
                }
                mods.forEach { m -> append("mods[]", m.type) }
                type?.let { append("type", it) }
            }
        }
    }
}