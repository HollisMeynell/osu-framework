package org.spring.osu.extended.rosu

import org.spring.osu.model.LazerScoreStatistics

data class JniScoreState @JvmOverloads constructor(
    var maxCombo: Int = 0,
    var largeTickHits: Int = 0,
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
        writeInt(2, sliderEndHits)
        writeInt(3, geki)
        writeInt(4, katu)
        writeInt(5, n300)
        writeInt(6, n100)
        writeInt(7, n50)
        writeInt(8, misses)
        return data
    }
    companion object {
        @JvmStatic
        fun create(state: LazerScoreStatistics, maxCombo: Int) = JniScoreState(
            maxCombo,
            state.largeTickHit,
            state.sliderTailHit,
            state.perfect,
            state.good,
            state.great,
            state.ok,
            state.meh,
            state.miss,
        )

        @JvmStatic
        fun create(
            maxCombo: Int = 0,
            largeTickHits: Int = 0,
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