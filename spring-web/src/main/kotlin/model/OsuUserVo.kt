package org.spring.web.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import org.spring.osu.model.User
import org.spring.osu.model.UserStatistics

data class OsuUserVo(
    @JsonProperty("id")
    var id: Long,

    @JsonProperty("name")
    var name: String,

    @JsonProperty("avatar")
    var avatar: String,

    @JsonProperty("cover")
    var cover: String,

    @JsonProperty("country")
    var country: String = "",

    @JsonUnwrapped
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
        @JsonProperty("fans")
        var fans: Int,

        @JsonProperty("mode")
        @JsonSerialize(using = OsuMode.RulesetSerializer::class)
        var mode: OsuMode,

        @JsonProperty("pp")
        var pp: Float,

        @JsonProperty("level")
        var level: UserStatistics.Level,

        @JsonProperty("global_rank")
        var globalRank: Int,

        @JsonProperty("country_rank")
        var countryRank: Int?,

        @JsonProperty("ranked_score")
        var rankedScore: Long,

        @JsonProperty("total_score")
        var totalScore: Long,

        @JsonProperty("total_hits")
        var totalHits: Long,
    )
}



