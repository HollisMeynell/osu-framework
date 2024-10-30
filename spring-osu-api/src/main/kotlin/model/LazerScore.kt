package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.model.Score.Weight
import java.time.OffsetDateTime

data class LazerScore(
    @JsonProperty("classic_total_score")
    var classicTotalScore: Int,

    @JsonProperty("preserve")
    var preserve: Boolean,

    @JsonProperty("processed")
    var processed: Boolean,

    @JsonProperty("ranked")
    var ranked: Boolean,

    @JsonProperty("maximum_statistics")
    var maximumStatistics: LazerScoreStatistics,

    @JsonProperty("statistics")
    var statistics: LazerScoreStatistics,

    @JsonProperty("mods")
    var mods: List<LazerMod>,

    @JsonProperty("beatmap_id")
    var beatmapId: Long,

    @JsonProperty("best_id")
    var bestId: Long?,

    @JsonProperty("id")
    var id: Long,

    @JsonProperty("rank")
    var rank: String,

    @JsonProperty("type")
    @JsonSerialize(using = ScoreType.Serializer::class)
    @JsonDeserialize(using = ScoreType.Deserializer::class)
    var type: ScoreType,

    @JsonProperty("user_id")
    var userId: Long,

    @JsonProperty("accuracy")
    var accuracy: Double,

    @JsonProperty("build_id")
    var buildId: Long,

    @JsonProperty("ended_at")
    var endedAt: OffsetDateTime,

    @JsonProperty("has_replay")
    var hasReplay: Boolean,

    @JsonProperty("replay")
    var replay: Boolean,

    @JsonProperty("is_perfect_combo")
    var isPerfectCombo: Boolean,

    @JsonProperty("legacy_perfect")
    var legacyPerfect: Boolean,

    @JsonProperty("legacy_score_id")
    var legacyScoreId: Long?,

    @JsonProperty("legacy_total_score")
    var legacyTotalScore: Int?,

    @JsonProperty("max_combo")
    var maxCombo: Int,

    @JsonProperty("passed")
    var passed: Boolean,

    @JsonProperty("pp")
    var pp: Double?,

    @JsonProperty("ruleset_id")
    var rulesetId: Int,

    @JsonProperty("started_at")
    var startedAt: OffsetDateTime,

    @JsonProperty("total_score")
    var totalScore: Int,

    @JsonProperty("current_user_attributes")
    var currentUserAttributes:  Map<String, Any?>,

    @JsonProperty("beatmap")
    var beatmap: Beatmap?,

    @JsonProperty("beatmapset")
    var beatmapset: Beatmapset?,

    @JsonProperty("user")
    var user: User?,


    @JsonProperty("weight")
    var weight: Weight?,
)