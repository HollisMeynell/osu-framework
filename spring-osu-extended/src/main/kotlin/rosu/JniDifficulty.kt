package org.spring.osu.extended.rosu

class JniDifficulty(
    withMods: Boolean = false,
    ar: Float? = null,
    od: Float? = null,
    cs: Float? = null,
    hp: Float? = null,
    clockRate: Double? = null,
    isLazer: Boolean = false,
) : NativeClass(1) {
    @set:JvmName("_setAr")
    var ar: Float? = ar
        private set

    @set:JvmName("_setOd")
    var od: Float? = od
        private set

    @set:JvmName("_setCs")
    var cs: Float? = cs
        private set

    @set:JvmName("_setHp")
    var hp: Float? = hp
        private set

    @set:JvmName("_setClockRate")
    var clockRate: Double? = clockRate
        private set

    @set:JvmName("_setIsLazer")
    var isLazer: Boolean? = isLazer
        private set

    /**
     * @param withMods `true` ignore mods, `false` modified based on the mods.
     */
    fun setAr(value: Float, withMods: Boolean) {
        nativeSetAr(value, withMods)
        ar = value
    }

    /**
     * @param withMods `true` ignore mods, `false` modified based on the mods.
     */
    fun setOd(value: Float, withMods: Boolean) {
        nativeSetOd(value, withMods)
        od = value
    }

    /**
     * @param withMods `true` ignore mods, `false` modified based on the mods.
     */
    fun setCs(value: Float, withMods: Boolean) {
        nativeSetCs(value, withMods)
        cs = value
    }

    /**
     * @param withMods `true` ignore mods, `false` modified based on the mods.
     */
    fun setHp(value: Float, withMods: Boolean) {
        nativeSetHp(value, withMods)
        hp = value
    }

    fun setClockRate(value: Double) {
        nativeSetClockRate(value)
        clockRate = value
    }

    fun isLazer(bool: Boolean) {
        setLazer(bool)
        isLazer = bool
    }

    fun isHardrock(bool: Boolean) {
        setHardrock(bool)
    }

    @JvmName("isAllNull")
    private fun isAllNull(): Boolean = ar == null && od == null && cs == null && hp == null && clockRate == null
    private external fun initDifficulty(
        withMods: Boolean,
        lazer: Boolean,
        isNull: Boolean,
        ar: Float = this.ar ?: -10f,
        od: Float = this.od ?: -10f,
        cs: Float = this.cs ?: -10f,
        hp: Float = this.hp ?: -10f,
        clockRate: Double = this.clockRate ?: -1.0,
    )

    private external fun nativeSetAr(value: Float, withMods: Boolean)
    private external fun nativeSetOd(value: Float, withMods: Boolean)
    private external fun nativeSetCs(value: Float, withMods: Boolean)
    private external fun nativeSetHp(value: Float, withMods: Boolean)
    private external fun setLazer(bool: Boolean)
    private external fun setHardrock(bool: Boolean)
    private external fun nativeSetClockRate(value: Double)
    external fun calculate(beatmap: JniBeatmap): JniDifficultyAttributes

    init {
        initDifficulty(withMods, isLazer, isAllNull())
    }
}
