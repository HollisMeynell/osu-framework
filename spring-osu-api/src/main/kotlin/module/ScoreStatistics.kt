package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty

data class ScoreStatistics(

    @JsonProperty("count_100")
    var count100: Int,

    @JsonProperty("count_300")
    var count300: Int,

    @JsonProperty("count_50")
    var count50: Int,

    @JsonProperty("count_miss")
    var countMiss: Int,

    @JsonProperty("count_geki")
    var countGeki: Int,

    @JsonProperty("count_katu")
    var countKatu: Int,
)
