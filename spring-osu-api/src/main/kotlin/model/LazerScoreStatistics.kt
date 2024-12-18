package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import org.spring.osu.OsuMode
import org.spring.osu.OsuMode.*
import kotlin.math.max
import kotlin.math.roundToInt

data class LazerScoreStatistics(
    /**
     * mania MAX
     */
    @JsonProperty("perfect")
    @JsonAlias("count_geki")
    var perfect: Int = 0,

    /**
     * n300
     */
    @JsonProperty("great")
    @JsonAlias("count_300")
    var great: Int = 0,

    /**
     * mania n200
     */
    @JsonProperty("good")
    @JsonAlias("count_katu")
    var good: Int = 0,

    /**
     * n100
     */
    @JsonProperty("ok")
    @JsonAlias("count_100")
    var ok: Int = 0,

    /**
     * n50
     */
    @JsonProperty("meh")
    @JsonAlias("count_50")
    var meh: Int = 0,

    @JsonProperty("miss")
    @JsonAlias("count_miss")
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
) {
    fun totalHits(mode: OsuMode) = when (mode) {
        Osu -> great + ok + meh + miss
        Taiko -> great + ok + miss
        Catch -> great + largeTickHit + smallTickHit + miss
        Mania -> perfect + great + good + ok + meh + miss
        else -> 0
    }

    fun accuracy(mode: OsuMode): Float {
        val numerator: Int
        val denominator: Int
        when (mode) {
            Osu -> {
                numerator = great * 300 + ok * 100 + meh * 50
                denominator = totalHits(mode) * 300
            }

            Taiko -> {
                numerator = great * 2 + ok * 1
                denominator = totalHits(mode) * 2
            }

            Catch -> {
                numerator = great + largeTickHit + smallTickHit
                denominator = totalHits(mode)
            }

            Mania -> {
                numerator = (perfect + great) * 300 + good * 200 + ok * 100 + meh * 50
                denominator = totalHits(mode) * 300
            }

            else -> return 0f
        }

        return (10000f * numerator / denominator).roundToInt() / 100f
    }

    fun toScoreStatistics(mode: OsuMode): ScoreStatistics {
        var geki = 0
        var katu = 0
        val n300 = great
        var n100 = 0
        var n50 = 0
        val misses = miss
        when (mode) {
            Osu -> {
                n100 = ok
                n50 = meh
            }

            Taiko -> {
                n100 = ok
            }

            Catch -> {
                n100 = ok
                n50 = meh
                katu = good
            }

            Mania -> {
                geki = perfect
                katu = good
                n100 = ok
                n50 = meh
            }

            Default -> return ScoreStatistics(0, 0, 0, 0, 0, 0)
        }
        return ScoreStatistics(
            n300,
            n100,
            n50,
            misses,
            geki,
            katu,
        )
    }
}
