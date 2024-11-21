package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty


sealed class BeatmapDifficultyAttributes(
    @JsonProperty("max_combo")
    open var maxCombo: Int,

    @JsonProperty("star_rating")
    open var starRating: Float,
)

data class BeatmapDifficultyAttributesOsu(
    @JsonProperty("max_combo")
    override var maxCombo: Int,

    @JsonProperty("star_rating")
    override var starRating: Float,

    @JsonProperty("aim_difficulty")
    var aimDifficulty: Float,

    @JsonProperty("approach_rate")
    var approachRate: Float,

    @JsonProperty("flashlight_difficulty")
    var flashlightDifficulty: Float,

    @JsonProperty("overall_difficulty")
    var overallDifficulty: Float,

    @JsonProperty("slider_factor")
    var sliderFactor: Float,

    @JsonProperty("speed_difficulty")
    var speedDifficulty: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)

data class BeatmapDifficultyAttributesTaiko(
    @JsonProperty("max_combo")
    override var maxCombo: Int,

    @JsonProperty("star_rating")
    override var starRating: Float,

    @JsonProperty("stamina_difficulty")
    var staminaDifficulty: Float,

    @JsonProperty("rhythm_difficulty")
    var rhythmDifficulty: Float,

    @JsonProperty("colour_difficulty")
    var colourDifficulty: Float,

    @JsonProperty("approach_rate")
    var approachRate: Float,

    @JsonProperty("great_hit_window")
    var greatHitWindow: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)

data class BeatmapDifficultyAttributesFruits(
    @JsonProperty("max_combo")
    override var maxCombo: Int,

    @JsonProperty("star_rating")
    override var starRating: Float,

    @JsonProperty("approach_rate")
    var approachRate: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)

data class BeatmapDifficultyAttributesMania(
    @JsonProperty("max_combo")
    override var maxCombo: Int,

    @JsonProperty("star_rating")
    override var starRating: Float,

    @JsonProperty("great_hit_window")
    var greatHitWindow: Float,

    @JsonProperty("score_multiplier")
    var scoreMultiplier: Float,
) : BeatmapDifficultyAttributes(maxCombo, starRating)
