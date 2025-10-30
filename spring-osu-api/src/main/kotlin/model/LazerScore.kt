package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.model.Score.Weight
import java.time.OffsetDateTime

data class LazerScore(
    @field:JsonProperty("classic_total_score")
    var classicTotalScore: Int,

    @field:JsonProperty("preserve")
    var preserve: Boolean,

    @field:JsonProperty("processed")
    var processed: Boolean,

    @field:JsonProperty("ranked")
    var ranked: Boolean,

    @field:JsonProperty("maximum_statistics")
    var maximumStatistics: LazerScoreStatistics,

    @field:JsonProperty("statistics")
    var statistics: LazerScoreStatistics,

    @field:JsonProperty("mods")
    var mods: List<LazerMod>,

    @field:JsonProperty("beatmap_id")
    var beatmapId: Long,

    @field:JsonProperty("best_id")
    var bestId: Long?,

    @field:JsonProperty("id")
    var id: Long,

    @field:JsonProperty("rank")
    var rank: String,

    @field:JsonProperty("type")
    @field:JsonSerialize(using = ScoreType.Serializer::class)
    @field:JsonDeserialize(using = ScoreType.Deserializer::class)
    var type: ScoreType,

    @field:JsonProperty("user_id")
    var userId: Long,

    @field:JsonProperty("accuracy")
    var accuracy: Double,

    @field:JsonProperty("build_id")
    var buildId: Long,

    @field:JsonProperty("ended_at")
    var endedAt: OffsetDateTime,

    @field:JsonProperty("has_replay")
    var hasReplay: Boolean,

    @field:JsonProperty("replay")
    var replay: Boolean,

    @field:JsonProperty("is_perfect_combo")
    var isPerfectCombo: Boolean,

    @field:JsonProperty("legacy_perfect")
    var legacyPerfect: Boolean,

    @field:JsonProperty("legacy_score_id")
    var legacyScoreId: Long?,

    @field:JsonProperty("legacy_total_score")
    var legacyTotalScore: Int?,

    @field:JsonProperty("max_combo")
    var maxCombo: Int,

    @field:JsonProperty("passed")
    var passed: Boolean,

    @field:JsonProperty("pp")
    var pp: Double?,

    @field:JsonProperty("ruleset_id")
    var rulesetId: Int,

    @field:JsonProperty("started_at")
    var startedAt: OffsetDateTime?,

    @field:JsonProperty("total_score")
    var totalScore: Int,

    @field:JsonProperty("current_user_attributes")
    var currentUserAttributes:  Map<String, Any?>,

    @field:JsonProperty("beatmap")
    var beatmap: Beatmap?,

    @field:JsonProperty("beatmapset")
    var beatmapset: Beatmapset?,

    @field:JsonProperty("user")
    var user: User?,


    @field:JsonProperty("weight")
    var weight: Weight?,
)