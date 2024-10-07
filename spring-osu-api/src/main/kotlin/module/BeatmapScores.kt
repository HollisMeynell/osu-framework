package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapScores(
    @JsonProperty("scores")
    var scores: List<Score>,

    @JsonAlias("user_score")
    @JsonProperty("userScore")
    var userScore: BeatmapUserScore?,
)
