package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class LazerBeatmapScores(
    @JsonProperty("scores")
    var scores: List<LazerScore>,

    @JsonAlias("user_score")
    @JsonProperty("userScore")
    var userScore: LazerBeatmapUserScore?,
)
