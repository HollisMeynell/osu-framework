package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ScoreStatistics(

    @field:JsonProperty("count_300")
    var count300: Int,

    @field:JsonProperty("count_100")
    var count100: Int,

    @field:JsonProperty("count_50")
    var count50: Int,

    @field:JsonProperty("count_miss")
    var countMiss: Int,

    @field:JsonProperty("count_geki")
    var countGeki: Int,

    @field:JsonProperty("count_katu")
    var countKatu: Int,
)
