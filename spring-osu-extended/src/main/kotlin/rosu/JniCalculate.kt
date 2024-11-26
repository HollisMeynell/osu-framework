package org.spring.osu.extended.rosu

import org.jetbrains.annotations.Nullable

class JniGradualPerformance private constructor(
    val state: JniScoreState
) : NativeClass(8) {
    var maxCombo by state::maxCombo
    var largeTickHits by state::largeTickHits
    var sliderEndHits by state::sliderEndHits
    var geki by state::geki
    var katu by state::katu
    var n300 by state::n300
    var n100 by state::n100
    var n50 by state::n50
    var misses by state::misses

    external fun remainingLength(): Int

    @Nullable fun next(): JniPerformanceAttributes? {
        return nativeNext(state.serialize())
    }

    @Nullable fun last(): JniPerformanceAttributes? {
        return nativeLast(state.serialize())
    }

    /**
     * Note that the count is zero-indexed, so n=0 will process 1 object, n=1 will process 2, and so on.
     */
    @Nullable fun next(n: Int): JniPerformanceAttributes? {
        return nativeNextSome(state.serialize(), n)
    }

    private external fun nativeNext(state: ByteArray): JniPerformanceAttributes?
    private external fun nativeLast(state: ByteArray): JniPerformanceAttributes?
    private external fun nativeNextSome(state: ByteArray, n:Int): JniPerformanceAttributes?

    companion object {
        @JvmStatic
        fun createByDifficulty(difficulty: JniDifficulty, beatmap: JniBeatmap) = difficulty.createGradualPerformance(beatmap)
    }
}