package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapScores(
    @field:JsonProperty("scores")
    var scores: List<Score>,

    @field:JsonAlias("user_score")
    @field:JsonProperty("userScore")
    var userScore: BeatmapUserScore?,
)
