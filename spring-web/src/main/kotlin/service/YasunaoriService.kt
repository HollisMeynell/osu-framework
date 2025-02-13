package org.spring.web.service

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import org.spring.osu.OsuApi
import org.spring.osu.OsuMode
import org.spring.osu.model.Beatmap
import org.spring.osu.model.OsuMod
import org.spring.osu.model.User
import org.spring.web.HttpTipsException
import org.spring.web.WebClient
import org.spring.web.WebConfig

object YasunaoriService {
    private val BACKGROUND_URL_PREFIX by lazy {
        "${WebConfig.Instance.selfUrl()}/api/file/map/bg/"
    }
    private val AVATAR_URL_PREFIX by lazy {
        "${WebConfig.Instance.selfUrl()}/api/yasunaori/avatar/"
    }


    suspend fun getUser(uid: Long?, name: String?, mode: String?): YasunaoriUserInfo {
        val osuMode = OsuMode.getMode(mode)
        if (uid == null && name == null) {
            return YasunaoriUserInfo("uid 与 name 不能同时为空.")
        }
        val user = OsuApi.getUser(user = uid, name = name, mode = osuMode)
        return YasunaoriUserInfo(user)
    }

    fun getAvatarUrl(user: Long): String {
        val id = zipNumber(user)
        return "$AVATAR_URL_PREFIX$id"
    }

    suspend fun outAvatar(uid: String, out: suspend ByteReadChannel.() -> Unit) {
        val id = unzipNumber(uid)
        val request = WebClient.proxyClient.prepareRequest {
            url("https://a.ppy.sh/$id")
        }

        request.execute {
            if (!it.status.isSuccess()) {
                val message = it.bodyAsText()
                throw HttpTipsException(message = message)
            }
            it.bodyAsChannel().out()
        }
    }

    private fun zipNumber(n: Long): String {
        val sb = StringBuilder()
        var number = n
        var mod: Int
        var c: Char
        while (number > 0) {
            mod = (number % 62).toInt()
            c = when {
                mod < 10 -> '0' + mod
                mod < 36 -> 'a' + mod - 10
                else -> 'A' + mod - 36
            }
            sb.append(c)
            number /= 62
        }
        return sb.reverse().toString()
    }

    private fun unzipNumber(s: String): Long {
        var result = 0L
        for (c in s) {
            result = result * 62 + when (c) {
                in '0'..'9' -> c - '0'
                in 'a'..'z' -> c - 'a' + 10
                in 'A'..'Z' -> c - 'A' + 36
                else -> 0
            }
        }
        return result
    }

    suspend fun getBeatmap(bid: Long?, mods: String?, mode: String?): YasunaoriBeatmapInfo {
        if (bid == null) {
            return YasunaoriBeatmapInfo("bid 不能为空")
        }
        val osuMode = OsuMode.getMode(mode)
        val beatmapInfo = OsuApi.getBeatmap(bid)

        val stats = YasunaoriBeatmapInfo.Stats(
            length = beatmapInfo.totalLength,
            bpm = beatmapInfo.bpm!!,
            cs = beatmapInfo.cs!!,
            ar = beatmapInfo.ar!!,
            od = beatmapInfo.accuracy!!,
            hp = beatmapInfo.drain!!,
        )
        val difficulty = if (mods != null || osuMode != beatmapInfo.mode) {
            if (osuMode != beatmapInfo.mode && beatmapInfo.mode != OsuMode.Osu) {
                return YasunaoriBeatmapInfo("该谱面不支持转换为该模式")
            }
            val modList = mods?.let { OsuMod.getAllMod(it) }
            val attr = OsuApi.getBeatmapAttributes(bid, osuMode, modList)
            YasunaoriBeatmapInfo.Difficulty(
                star = attr.starRating,
                name = beatmapInfo.version,
            )
        } else {
            YasunaoriBeatmapInfo.Difficulty(
                star = beatmapInfo.difficultyRating,
                name = beatmapInfo.version,
            )
        }

        return YasunaoriBeatmapInfo(beatmapInfo, stats, difficulty)
    }

    fun getBeatmapCover(bid: Long): String {
        return "$BACKGROUND_URL_PREFIX$bid"
    }
}

data class YasunaoriUserInfo(
    val error: String? = null,
    val id: Long?,
    val username: String?,
    val avatarUrl: String?,
    val countryCode: String?,
    val globalRank: Int?,
    val countryRank: Int?,
) {
    constructor(error: String) : this(
        error = error,
        id = null,
        username = null,
        avatarUrl = null,
        countryCode = null,
        globalRank = null,
        countryRank = null,
    )

    constructor(user: User) : this(
        id = user.id,
        username = user.username,
        avatarUrl = YasunaoriService.getAvatarUrl(user.id),
        countryCode = user.countryCode,
        globalRank = user.statistics?.globalRank ?: 0,
        countryRank = user.statistics?.countryRank ?: 0,
    )
}

data class YasunaoriBeatmapInfo(
    val error: String? = null,
    val id: Long?,
    val title: String?,
    val titleUnicode: String?,
    val artist: String?,
    val artistUnicode: String?,
    val creator: String?,
    val coverUrl: String?,
    val status: String?,
    val mode: String?,
    val stats: Stats?,
    val difficulty: Difficulty?,
) {
    constructor(beatmap: Beatmap, stats: Stats, difficulty: Difficulty) : this(
        id = beatmap.id,
        title = beatmap.beatmapset?.title ?: "unknown",
        titleUnicode = beatmap.beatmapset?.titleUnicode ?: "unknown",
        artist = beatmap.beatmapset?.artist ?: "unknown",
        artistUnicode = beatmap.beatmapset?.artistUnicode ?: "unknown",
        creator = beatmap.beatmapset?.creator ?: "unknown",
        coverUrl = YasunaoriService.getBeatmapCover(beatmap.id),
        status = beatmap.status.name,
        mode = beatmap.mode.name,
        stats = stats,
        difficulty = difficulty,
    )

    constructor(error: String) : this(
        error = error,
        id = null,
        title = null,
        titleUnicode = null,
        artist = null,
        artistUnicode = null,
        creator = null,
        coverUrl = null,
        status = null,
        mode = null,
        stats = null,
        difficulty = null,
    )

    data class Stats(
        var length: Int,
        var bpm: Float,
        var cs: Float,
        var ar: Float,
        var od: Float,
        var hp: Float,
    ) {
        fun setMods(mods: List<OsuMod>) {
            if (OsuMod.hasChangeRating(mods).not()) return

            if (mods.contains(OsuMod.HardRock)) {
                setHR()
            }
            if (mods.contains(OsuMod.DoubleTime)) {
                setDT()
            }
            if (mods.contains(OsuMod.Easy)) {
                setEZ()
            }
            if (mods.contains(OsuMod.HalfTime)) {
                setHT()
            }
        }

        private fun setHR() {
            ar = OsuMod.changeAR(ar, hr = true)
            od = OsuMod.changeOD(od, hr = true)
            cs = OsuMod.changeCS(cs, isHR = true)
            hp = OsuMod.changeHP(hp, isHR = true)
        }

        private fun setDT() {
            length = (length / 1.5).toInt()
            bpm = OsuMod.changeBPM(bpm, isDT = true)
            ar = OsuMod.changeAR(ar, dt = true)
            od = OsuMod.changeOD(od, dt = true)
        }

        private fun setEZ() {
            ar = OsuMod.changeAR(ar, ez = true)
            od = OsuMod.changeOD(od, ez = true)
            cs = OsuMod.changeCS(cs, isHR = false)
            hp = OsuMod.changeHP(hp, isHR = false)
        }

        private fun setHT() {
            length = (length / 0.75).toInt()
            bpm = OsuMod.changeBPM(bpm, isDT = false)
            ar = OsuMod.changeAR(ar, ht = true)
            od = OsuMod.changeOD(od, ht = true)
        }
    }

    data class Difficulty(
        val star: Float,
        val name: String,
    )
}