package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LazerBeatmapUserScore(
    @field:JsonProperty("position")
    var position: Int,

    @field:JsonProperty("score")
    var score: LazerScore,
)
