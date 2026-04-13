package org.spring.osu.extended.rosu

sealed interface JniDifficultyAttributes : AutoCloseable {
    fun getStarRating(): Double
    fun getMaxCombo(): Int

    fun createPerformance(state: JniScoreState? = null): JniPerformance {
        return JniPerformance.createByDifficultyAttributes(this, state)
    }

    fun createPerformance(): JniPerformance {
        return JniPerformance.createByDifficultyAttributes(this)
    }

    companion object {
        @JvmStatic
        fun createOsu(
            aim: Double,
            aimDifficultSliderCount: Double,
            speed: Double,
            flashlight: Double,
            sliderFactor: Double,
            aimTopWeightedSliderFactor: Double,
            speedTopWeightedSliderFactor: Double,
            speedNoteCount: Double,
            aimDifficultStrainCount: Double,
            speedDifficultStrainCount: Double,
            nestedScorePerObject: Double,
            legacyScoreBaseMultiplier: Double,
            maximumLegacyComboScore: Double,
            ar: Double,
            greatHitWindow: Double,
            okHitWindow: Double,
            mehHitWindow: Double,
            hp: Double,
            od: Double,
            nCircles: Int,
            nSliders: Int,
            nLargeTicks: Int,
            nSpinners: Int,
            stars: Double,
            nObjects: Int,
            maxCombo: Int,
        ): JniDifficultyAttributes =
            OsuDifficultyAttributes(
                aim,
                aimDifficultSliderCount,
                speed,
                flashlight,
                sliderFactor,
                aimTopWeightedSliderFactor,
                speedTopWeightedSliderFactor,
                speedNoteCount,
                aimDifficultStrainCount,
                speedDifficultStrainCount,
                nestedScorePerObject,
                legacyScoreBaseMultiplier,
                maximumLegacyComboScore,
                ar,
                greatHitWindow,
                okHitWindow,
                mehHitWindow,
                hp,
                od,
                nCircles,
                nSliders,
                nLargeTicks,
                nSpinners,
                stars,
                nObjects,
                maxCombo,
            )


        @JvmStatic
        fun createTaiko(
            stamina: Double,
            rhythm: Double,
            color: Double,
            reading: Double,
            greatHitWindow: Double,
            okHitWindow: Double,
            monoStaminaFactor: Double,
            consistencyFactor: Double,
            stars: Double,
            maxCombo: Int,
            isConvert: Boolean,
        ): JniDifficultyAttributes =
            TaikoDifficultyAttributes(
                stamina,
                rhythm,
                color,
                reading,
                greatHitWindow,
                okHitWindow,
                monoStaminaFactor,
                consistencyFactor,
                stars,
                maxCombo,
                isConvert,
            )


        @JvmStatic
        fun createCatch(
            stars: Double,
            preempt: Double,
            nFruits: Int,
            nDroplets: Int,
            nTinyDroplets: Int,
            isConvert: Boolean,
        ): JniDifficultyAttributes =
            CatchDifficultyAttributes(stars, preempt, nFruits, nDroplets, nTinyDroplets, isConvert)


        @JvmStatic
        fun createMania(
            stars: Double,
            nObjects: Int,
            nHoldNotes: Int,
            maxCombo: Int,
            isConvert: Boolean,
        ): JniDifficultyAttributes =
            ManiaDifficultyAttributes(stars, nObjects, nHoldNotes, maxCombo, isConvert)

    }
}

data class OsuDifficultyAttributes(
    val aim: Double = 0.0,
    val aimDifficultSliderCount: Double = 0.0,
    val speed: Double = 0.0,
    val flashlight: Double = 0.0,
    val sliderFactor: Double = 0.0,
    val aimTopWeightedSliderFactor: Double = 0.0,
    val speedTopWeightedSliderFactor: Double = 0.0,
    val speedNoteCount: Double = 0.0,
    val aimDifficultStrainCount: Double = 0.0,
    val speedDifficultStrainCount: Double = 0.0,
    val nestedScorePerObject: Double = 0.0,
    val legacyScoreBaseMultiplier: Double = 0.0,
    val maximumLegacyComboScore: Double = 0.0,
    val ar: Double = 0.0,
    val greatHitWindow: Double = 0.0,
    val okHitWindow: Double = 0.0,
    val mehHitWindow: Double = 0.0,
    val hp: Double = 0.0,
    val od: Double = 0.0,
    val nCircles: Int = 0,
    val nSliders: Int = 0,
    val nLargeTicks: Int = 0,
    val nSpinners: Int = 0,
    val stars: Double = 0.0,
    val nObjects: Int = 0,
    @get:JvmName("maxCombo")
    val maxCombo: Int = 0,
) : JniDifficultyAttributes, NativeClass(4) {
    override fun getStarRating() = stars
    override fun getMaxCombo() = maxCombo
}

data class TaikoDifficultyAttributes(
    val stamina: Double = 0.0,
    val rhythm: Double = 0.0,
    val color: Double = 0.0,
    val reading: Double = 0.0,
    val greatHitWindow: Double = 0.0,
    val okHitWindow: Double = 0.0,
    val monoStaminaFactor: Double = 0.0,
    val consistencyFactor: Double = 0.0,
    val stars: Double = 0.0,
    @get:JvmName("maxCombo")
    val maxCombo: Int = 0,
    val isConvert: Boolean = false,
) : JniDifficultyAttributes, NativeClass(5) {
    override fun getStarRating() = stars
    override fun getMaxCombo() = maxCombo
}

data class CatchDifficultyAttributes(
    val stars: Double = 0.0,
    val preempt: Double = 0.0,
    val nFruits: Int = 0,
    val nDroplets: Int = 0,
    val nTinyDroplets: Int = 0,
    val isConvert: Boolean = false,
) : JniDifficultyAttributes, NativeClass(6) {
    override fun getStarRating() = stars
    override fun getMaxCombo() = nFruits + nDroplets
}


data class ManiaDifficultyAttributes(
    val stars: Double = 0.0,
    val nObjects: Int = 0,
    val nHoldNotes: Int = 0,
    @get:JvmName("maxCombo")
    val maxCombo: Int = 0,
    val isConvert: Boolean = false,
) : JniDifficultyAttributes, NativeClass(7) {
    override fun getStarRating() = stars
    override fun getMaxCombo() = maxCombo
}