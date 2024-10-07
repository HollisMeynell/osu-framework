package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode

data class UserStatistics (
    @JsonProperty("count_100")
    var count100: Int,

    @JsonProperty("count_300")
    var count300: Int,

    @JsonProperty("count_50")
    var count50: Int,

    @JsonProperty("count_miss")
    var countMiss: Int,

    @JsonProperty("country_rank")
    var countryRank: Int?,

    @JsonProperty("grade_counts")
    var gradeCounts: GradeCounts,

    @JsonProperty("hit_accuracy")
    var hitAccuracy: Float,

    @JsonProperty("is_ranked")
    var isRanked: Boolean,

    @JsonProperty("level")
    var level: Level,

    @JsonProperty("maximum_combo")
    var maximumCombo: Int,

    @JsonProperty("play_count")
    var playCount: Int,

    @JsonProperty("play_time")
    var playTime: Int,

    @JsonProperty("pp")
    var pp: Float,

    @JsonProperty("pp_exp")
    var ppExp: Float,

    @JsonProperty("global_rank")
    var globalRank: Int?,

    @JsonProperty("global_rank_exp")
    var globalRankExp: Int?,

    @JsonProperty("ranked_score")
    var rankedScore: Long,

    @JsonProperty("replays_watched_by_others")
    var replaysWatchedByOthers: Int,

    @JsonProperty("total_hits")
    var totalHits: Long,

    @JsonProperty("total_score")
    var totalScore: Long
) {
    data class GradeCounts(
        @JsonProperty("a")
        var a: Int,

        @JsonProperty("s")
        var s: Int,

        @JsonProperty("sh")
        var sh: Int,

        @JsonProperty("ss")
        var ss: Int,

        @JsonProperty("ssh")
        var ssh: Int
    )

    data class Level(
        @JsonProperty("current")
        var current: Int,

        @JsonProperty("progress")
        var progress: Float
    )
}

data class UserStatisticsRulesets (
    @JsonProperty("osu")
    var osu: UserStatistics,

    @JsonProperty("taiko")
    var taiko: UserStatistics,

    @JsonProperty("fruits")
    var fruits: UserStatistics,

    @JsonProperty("mania")
    var mania: UserStatistics,

    @JsonProperty("variants")
    var variants: List<Variants>?,
)

data class Variants(
    @JsonProperty("mode")
    @JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var mode: OsuMode,

    @JsonProperty("variant")
    var variant: String,

    @JsonProperty("country_rank")
    var countryRank: Int,

    @JsonProperty("global_rank")
    var globalRank: Int,

    @JsonProperty("pp")
    var pp: Float,
)