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
            speed: Double,
            flashlight: Double,
            sliderFactor: Double,
            speedNoteCount: Double,
            aimDifficultStrainCount: Double,
            speedDifficultStrainCount: Double,
            ar: Double,
            od: Double,
            hp: Double,
            nCircles: Int,
            nSliders: Int,
            nLargeTicks: Int,
            nSpinners: Int,
            stars: Double,
            maxCombo: Int,
        ): JniDifficultyAttributes =
            OsuDifficultyAttributes(
                aim,
                speed,
                flashlight,
                sliderFactor,
                speedNoteCount,
                aimDifficultStrainCount,
                speedDifficultStrainCount,
                ar,
                od,
                hp,
                nCircles,
                nSliders,
                nLargeTicks,
                nSpinners,
                stars,
                maxCombo
            )


        @JvmStatic
        fun createTaiko(
            stamina: Double,
            rhythm: Double,
            color: Double,
            peak: Double,
            greatHitWindow: Double,
            okHitWindow: Double,
            monoStaminaFactor: Double,
            stars: Double,
            maxCombo: Int,
            isConvert: Boolean,
        ): JniDifficultyAttributes =
            TaikoDifficultyAttributes(
                stamina,
                rhythm,
                color,
                peak,
                greatHitWindow,
                okHitWindow,
                monoStaminaFactor,
                stars,
                maxCombo,
                isConvert
            )


        @JvmStatic
        fun createCatch(
            stars: Double,
            ar: Double,
            nFruits: Int,
            nDroplets: Int,
            nTinyDroplets: Int,
            isConvert: Boolean,
        ): JniDifficultyAttributes =
            CatchDifficultyAttributes(stars, ar, nFruits, nDroplets, nTinyDroplets, isConvert)


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
    val speed: Double = 0.0,
    val flashlight: Double = 0.0,
    val sliderFactor: Double = 0.0,
    val speedNoteCount: Double = 0.0,
    val aimDifficultStrainCount: Double = 0.0,
    val speedDifficultStrainCount: Double = 0.0,
    val ar: Double = 0.0,
    val od: Double = 0.0,
    val hp: Double = 0.0,
    val nCircles: Int = 0,
    val nSliders: Int = 0,
    val nLargeTicks: Int = 0,
    val nSpinners: Int = 0,
    val stars: Double = 0.0,
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
    val peak: Double = 0.0,
    val greatHitWindow: Double = 0.0,
    val okHitWindow: Double = 0.0,
    val monoStaminaFactor: Double = 0.0,
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
    val ar: Double = 0.0,
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