@file:Suppress("unused")

package org.spring.osu.extended.rosu

sealed interface JniPerformanceAttributes {
    fun getStarRating(): Double
    fun getMaxCombo(): Int
    fun getPP(): Double

    companion object {
        @JvmStatic
        fun createOsu(
            pp: Double,
            ppAcc: Double,
            ppAim: Double,
            ppFlashlight: Double,
            ppSpeed: Double,
            effectiveMissCount: Double,
            speedDeviation: Double,
            comboBasedEstimatedMissCount: Double,
            scoreBasedEstimatedMissCount: Double,
            aimEstimatedSliderBreaks: Double,
            speedEstimatedSliderBreaks: Double,
            difficulty: OsuDifficultyAttributes,
        ): JniPerformanceAttributes =
            OsuPerformanceAttributes(
                pp,
                ppAcc,
                ppAim,
                ppFlashlight,
                ppSpeed,
                effectiveMissCount,
                speedDeviation,
                comboBasedEstimatedMissCount,
                scoreBasedEstimatedMissCount,
                aimEstimatedSliderBreaks,
                speedEstimatedSliderBreaks,
                difficulty,
            )

        @JvmStatic
        fun createTaiko(
            pp: Double,
            ppAcc: Double,
            ppDifficulty: Double,
            estimatedUnstableRate: Double,
            difficulty: TaikoDifficultyAttributes,
        ): JniPerformanceAttributes =
            TaikoPerformanceAttributes(
                pp,
                ppAcc,
                ppDifficulty,
                estimatedUnstableRate,
                difficulty
            )

        @JvmStatic
        fun createCatch(
            pp: Double,
            difficulty: CatchDifficultyAttributes,
        ): JniPerformanceAttributes =
            CatchPerformanceAttributes(
                pp,
                difficulty
            )

        @JvmStatic
        fun createMania(
            pp: Double,
            ppDifficulty: Double,
            difficulty: ManiaDifficultyAttributes,
        ): JniPerformanceAttributes =
            ManiaPerformanceAttributes(
                pp,
                ppDifficulty,
                difficulty
            )
    }
}

data class OsuPerformanceAttributes(
    val pp: Double,
    val ppAcc: Double,
    val ppAim: Double,
    val ppFlashlight: Double,
    val ppSpeed: Double,
    val effectiveMissCount: Double,
    val speedDeviation: Double,
    val comboBasedEstimatedMissCount: Double,
    val scoreBasedEstimatedMissCount: Double,
    val aimEstimatedSliderBreaks: Double,
    val speedEstimatedSliderBreaks: Double,
    val difficulty: OsuDifficultyAttributes,
) : JniPerformanceAttributes {
    override fun getStarRating() = difficulty.stars

    override fun getMaxCombo() = difficulty.maxCombo

    override fun getPP() = pp
}

data class TaikoPerformanceAttributes(
    val pp: Double,
    val ppAcc: Double,
    val ppDifficulty: Double,
    val estimatedUnstableRate: Double,
    val difficulty: TaikoDifficultyAttributes,
) : JniPerformanceAttributes {
    override fun getStarRating() = difficulty.stars

    override fun getMaxCombo() = difficulty.maxCombo

    override fun getPP() = pp
}

data class CatchPerformanceAttributes(
    val pp: Double,
    val difficulty: CatchDifficultyAttributes,
) : JniPerformanceAttributes {
    override fun getStarRating() = difficulty.stars

    override fun getMaxCombo() = difficulty.getMaxCombo()

    override fun getPP() = pp
}

data class ManiaPerformanceAttributes(
    val pp: Double,
    val ppDifficulty: Double,
    val difficulty: ManiaDifficultyAttributes,
) : JniPerformanceAttributes {
    override fun getStarRating() = difficulty.stars

    override fun getMaxCombo() = difficulty.maxCombo

    override fun getPP() = pp
}