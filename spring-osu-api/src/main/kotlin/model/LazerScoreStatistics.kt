package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LazerScoreStatistics(
    @JsonProperty("perfect")
    var perfect: Int = 0,

    @JsonProperty("great")
    var great: Int = 0,

    @JsonProperty("good")
    var good: Int = 0,

    @JsonProperty("ok")
    var ok: Int = 0,

    @JsonProperty("meh")
    var meh: Int = 0,

    @JsonProperty("miss")
    var miss: Int = 0,

    @JsonProperty("ignore_hit")
    var ignoreHit: Int = 0,

    @JsonProperty("ignore_miss")
    var ignoreMiss: Int = 0,

    @JsonProperty("small_tick_hit")
    var smallTickHit: Int = 0,

    @JsonProperty("small_tick_miss")
    var smallTickMiss: Int = 0,

    @JsonProperty("large_tick_hit")
    var largeTickHit: Int = 0,

    @JsonProperty("large_tick_miss")
    var largeTickMiss: Int = 0,

    @JsonProperty("slider_tail_hit")
    var sliderTailHit: Int = 0,

    @JsonProperty("large_bonus")
    var largeBonus: Int = 0,

    @JsonProperty("small_bonus")
    var smallBonus: Int = 0,

    @JsonProperty("legacy_combo_increase")
    var legacyComboIncrease: Int = 0,
)
