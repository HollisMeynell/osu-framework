package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty


sealed class BeatmapDifficultyAttributes(
    @field:JsonProperty("max_combo")
    open var maxCombo: Int,

    @field:JsonProperty("star_rating")
    open var starRating: Float,
)

data class BeatmapDifficultyAttributesOsu(
    @field:JsonProperty("max_combo")
    override var maxCombo: Int,

    @field:JsonProperty("star_rating")
    override var starRating: Float,

    @field:JsonProperty("aim_difficulty")
    var aimDifficulty: Float,

    @field:JsonProperty("approach_rate")
    var approachRate: Float,

    @field:JsonProperty("flashlight_difficulty")
    var flashlightDifficulty: Float,

    @field:JsonProperty("overall_difficulty")
    var overallDifficulty: Float,

    @field:JsonProperty("slider_factor")
    var sliderFactor: Float,

    @field:JsonProperty("speed_difficulty")
    var speedDifficulty: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)

data class BeatmapDifficultyAttributesTaiko(
    @field:JsonProperty("max_combo")
    override var maxCombo: Int,

    @field:JsonProperty("star_rating")
    override var starRating: Float,

    @field:JsonProperty("stamina_difficulty")
    var staminaDifficulty: Float,

    @field:JsonProperty("rhythm_difficulty")
    var rhythmDifficulty: Float,

    @field:JsonProperty("colour_difficulty")
    var colourDifficulty: Float,

    @field:JsonProperty("approach_rate")
    var approachRate: Float,

    @field:JsonProperty("great_hit_window")
    var greatHitWindow: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)

data class BeatmapDifficultyAttributesFruits(
    @field:JsonProperty("max_combo")
    override var maxCombo: Int,

    @field:JsonProperty("star_rating")
    override var starRating: Float,

    @field:JsonProperty("approach_rate")
    var approachRate: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)

data class BeatmapDifficultyAttributesMania(
    @field:JsonProperty("max_combo")
    override var maxCombo: Int,

    @field:JsonProperty("star_rating")
    override var starRating: Float,

    @field:JsonProperty("great_hit_window")
    var greatHitWindow: Float,

    @field:JsonProperty("score_multiplier")
    var scoreMultiplier: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)
