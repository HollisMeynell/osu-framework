package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class LazerBeatmapScores(
    @field:JsonProperty("scores")
    var scores: List<LazerScore>,

    @field:JsonAlias("user_score")
    @field:JsonProperty("userScore")
    var userScore: LazerBeatmapUserScore?,
)
