package org.spring.osu.extended.rosu

import org.spring.osu.OsuMode
import org.spring.osu.model.LazerScoreStatistics
import org.spring.osu.model.ScoreStatistics

data class JniScoreState @JvmOverloads constructor(
    var maxCombo: Int = 0,
    var largeTickHits: Int = 0,
    var smallTickHits: Int = 0,
    var sliderEndHits: Int = 0,
    var geki: Int = 0,
    var katu: Int = 0,
    var n300: Int = 0,
    var n100: Int = 0,
    var n50: Int = 0,
    var misses: Int = 0,
) {

    fun serialize(): ByteArray {
        val data = ByteArray(36)
        fun writeInt(index: Int, value: Int) {
            val offset = index * 4
            data[offset] = (value ushr 24).toByte()
            data[offset + 1] = (value ushr 16).toByte()
            data[offset + 2] = (value ushr 8).toByte()
            data[offset + 3] = value.toByte()
        }
        writeInt(0, maxCombo)
        writeInt(1, largeTickHits)
        writeInt(2, smallTickHits)
        writeInt(3, sliderEndHits)
        writeInt(4, geki)
        writeInt(5, katu)
        writeInt(6, n300)
        writeInt(7, n100)
        writeInt(8, n50)
        writeInt(9, misses)
        return data
    }

    companion object {
        @JvmStatic
        fun create(state: ScoreStatistics, maxCombo: Int) = JniScoreState(
            maxCombo,
            0,
            0,
            0,
            state.countGeki,
            state.countKatu,
            state.count300,
            state.count100,
            state.count50,
            state.countMiss,
        )

        @JvmStatic
        fun create(state: LazerScoreStatistics, maxCombo: Int, mode: OsuMode):JniScoreState {
            val old = state.toScoreStatistics(mode)
            return JniScoreState(
                maxCombo,
                state.largeTickHit,
                state.smallTickHit,
                state.sliderTailHit,
                old.countGeki,
                old.countKatu,
                old.count300,
                old.count100,
                old.count50,
                old.countMiss,
            )
        }

        @JvmStatic
        fun create(
            maxCombo: Int = 0,
            largeTickHits: Int = 0,
            smallTickHits: Int = 0,
            sliderEndHits: Int = 0,
            geki: Int = 0,
            katu: Int = 0,
            n300: Int = 0,
            n100: Int = 0,
            n50: Int = 0,
            misses: Int = 0,
        ): JniScoreState {
            return JniScoreState(
                maxCombo,
                largeTickHits,
                smallTickHits,
                sliderEndHits,
                geki,
                katu,
                n300,
                n100,
                n50,
                misses
            )
        }
    }
}