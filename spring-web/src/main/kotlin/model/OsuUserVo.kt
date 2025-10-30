package org.spring.web.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import org.spring.osu.model.User
import org.spring.osu.model.UserStatistics

data class OsuUserVo(
    @field:JsonProperty("id")
    var id: Long,

    @field:JsonProperty("name")
    var name: String,

    @field:JsonProperty("avatar")
    var avatar: String,

    @field:JsonProperty("cover")
    var cover: String,

    @field:JsonProperty("country")
    var country: String = "",

    @field:JsonUnwrapped
    var extended: OsuUserExtended? = null,
) {
    companion object {
        fun baseFromUser(user: User): OsuUserVo {
            return OsuUserVo(
                id = user.id,
                name = user.username,
                avatar = user.avatarUrl,
                cover = user.cover?.url ?: "",
                country = user.countryCode,
            )
        }

        fun extendedFromUser(user: User, mode: OsuMode? = null): OsuUserVo {
            val statistics = user.statistics ?: throw IllegalArgumentException("User has no statistics")
            val extended = OsuUserExtended(
                fans = user.followerCount ?: 0,
                mode = mode ?: user.playmode!!,
                pp = statistics.pp,
                level = statistics.level,
                globalRank = statistics.globalRank ?: 0,
                countryRank = statistics.countryRank ?: 0,
                rankedScore = statistics.rankedScore,
                totalScore = statistics.totalScore,
                totalHits = statistics.totalHits,
            )
            val base = baseFromUser(user)
            base.extended = extended
            return base
        }
    }

    data class OsuUserExtended(
        @field:JsonProperty("fans")
        var fans: Int,

        @field:JsonProperty("mode")
        @field:JsonSerialize(using = OsuMode.RulesetSerializer::class)
        var mode: OsuMode,

        @field:JsonProperty("pp")
        var pp: Float,

        @field:JsonProperty("level")
        var level: UserStatistics.Level,

        @field:JsonProperty("global_rank")
        var globalRank: Int,

        @field:JsonProperty("country_rank")
        var countryRank: Int?,

        @field:JsonProperty("ranked_score")
        var rankedScore: Long,

        @field:JsonProperty("total_score")
        var totalScore: Long,

        @field:JsonProperty("total_hits")
        var totalHits: Long,
    )
}



