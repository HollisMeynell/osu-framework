package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode

data class UserStatistics (
    @field:JsonProperty("count_100")
    var count100: Int,

    @field:JsonProperty("count_300")
    var count300: Int,

    @field:JsonProperty("count_50")
    var count50: Int,

    @field:JsonProperty("count_miss")
    var countMiss: Int,

    @field:JsonProperty("country_rank")
    var countryRank: Int?,

    @field:JsonProperty("grade_counts")
    var gradeCounts: GradeCounts,

    @field:JsonProperty("hit_accuracy")
    var hitAccuracy: Float,

    @field:JsonProperty("is_ranked")
    var isRanked: Boolean,

    @field:JsonProperty("level")
    var level: Level,

    @field:JsonProperty("maximum_combo")
    var maximumCombo: Int,

    @field:JsonProperty("play_count")
    var playCount: Int,

    @field:JsonProperty("play_time")
    var playTime: Int,

    @field:JsonProperty("pp")
    var pp: Float,

    @field:JsonProperty("pp_exp")
    var ppExp: Float,

    @field:JsonProperty("global_rank")
    var globalRank: Int?,

    @field:JsonProperty("global_rank_exp")
    var globalRankExp: Int?,

    @field:JsonProperty("ranked_score")
    var rankedScore: Long,

    @field:JsonProperty("replays_watched_by_others")
    var replaysWatchedByOthers: Int,

    @field:JsonProperty("total_hits")
    var totalHits: Long,

    @field:JsonProperty("total_score")
    var totalScore: Long,
) {
    data class GradeCounts(
        @field:JsonProperty("a")
        var a: Int,

        @field:JsonProperty("s")
        var s: Int,

        @field:JsonProperty("sh")
        var sh: Int,

        @field:JsonProperty("ss")
        var ss: Int,

        @field:JsonProperty("ssh")
        var ssh: Int
    )

    data class Level(
        @field:JsonProperty("current")
        var current: Int,

        @field:JsonProperty("progress")
        var progress: Float
    )
}

data class UserStatisticsRulesets (
    @field:JsonProperty("osu")
    var osu: UserStatistics,

    @field:JsonProperty("taiko")
    var taiko: UserStatistics,

    @field:JsonProperty("fruits")
    var fruits: UserStatistics,

    @field:JsonProperty("mania")
    var mania: UserStatistics,

    @field:JsonProperty("variants")
    var variants: List<Variants>?,
)

data class Variants(
    @field:JsonProperty("mode")
    @field:JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @field:JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var mode: OsuMode,

    @field:JsonProperty("variant")
    var variant: String,

    @field:JsonProperty("country_rank")
    var countryRank: Int,

    @field:JsonProperty("global_rank")
    var globalRank: Int,

    @field:JsonProperty("pp")
    var pp: Float,
)