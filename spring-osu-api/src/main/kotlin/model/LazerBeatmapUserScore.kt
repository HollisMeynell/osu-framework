package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LazerBeatmapUserScore(
    @JsonProperty("position")
    var position: Int,

    @JsonProperty("score")
    var score: LazerScore,
)
