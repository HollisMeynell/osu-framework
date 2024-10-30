package org.spring.web.service

import org.spring.osu.OsuApi
import org.spring.osu.OsuMode
import org.spring.osu.extended.OsuWebApi
import org.spring.osu.model.User
import org.spring.osu.persistence.model.OsuWebUserRecord
import org.spring.web.HttpTipsException

object YasunaoriService {

    suspend fun getUser(uid: Long?, name: String?, mode: String?): YasunaoriUserInfo {
        val mode = OsuMode.getMode(mode)
        val user = OsuApi.getUser(user = uid, name = name, mode = mode)
        return YasunaoriUserInfo(
            id = user.id,
            username = user.username,
            avatarUrl = "",
            countryCode = user.countryCode,
            globalRank = user.statistics?.globalRank ?: 0,
            countryRank = user.statistics?.countryRank ?: 0,
        )
    }

    fun getAvatarUrl(user: Long): String {
        return ""
    }

    suspend fun getAvatar(user: Long): ByteArray {
        val session = OsuWebUserRecord.getRandomRecord() ?: throw HttpTipsException(500, "server unsupported download")
        return OsuWebApi.doDownloadAvatar(session, user)
    }
}

data class YasunaoriUserInfo(
    val id: Long,
    val username: String,
    val avatarUrl: String,
    val countryCode: String,
    val globalRank: Int,
    val countryRank: Int,
) {
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
    val error: String,
    val id: Long,
    val title: String,
    val titleUnicode: String,
    val artist: String,
    val artistUnicode: String,
    val creator: String,
    val coverUrl: String,
    val status: String,
    val mode: String,
    val stats: Stats,
    val difficulty: Difficulty,
) {
    data class Stats(
        val length: Int,
        val bpm: Float,
        val cs: Float,
        val ar: Float,
        val od: Float,
        val hp: Float,
    )

    data class Difficulty(
        val star: Float,
        val name: String,
    )
}