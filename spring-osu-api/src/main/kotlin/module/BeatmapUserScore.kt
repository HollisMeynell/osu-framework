package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapUserScore(
    @JsonProperty("position")
    var position: Int,

    @JsonProperty("score")
    var score: Score,
)
