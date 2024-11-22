@file:Suppress("unused")
package org.spring.osu.extended.rosu

import org.spring.core.toJson
import org.spring.osu.OsuMode
import org.spring.osu.model.LazerMod
import org.spring.osu.model.OsuMod

class JniDifficulty(
    withMods: Boolean = false,
    ar: Float? = null,
    od: Float? = null,
    cs: Float? = null,
    hp: Float? = null,
    clockRate: Double? = null,
    isLazer: Boolean = true,
    var mode: OsuMode? = null,
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

    fun hardrockOffsets(bool: Boolean) {
        setHardrock(bool)
    }

    fun passObject(sum: Int) {
        setPassObject(sum)
    }

    fun setMods(legacy: Int) {
        nativeSetMods(legacy)
    }

    fun setMods(vararg legacy: OsuMod) {
        val int = legacy.map { it.value }.reduce { acc, mod -> acc or mod }
        nativeSetMods(int)
    }

    fun setMods(legacy: Collection<OsuMod>) {
        val int = legacy.map { it.value }.reduce { acc, mod -> acc or mod }
        nativeSetMods(int)
    }

    fun setMods(json: String, mode: OsuMode? = this.mode) {
        mode?.let {
            nativeSetModsByStr(it.value.toByte(), json)
        } ?: throw IllegalArgumentException("unknown mode")
    }

    fun setMods(vararg mods: LazerMod) {
        mode?.let {
            nativeSetModsByStr(it.value.toByte(), mods.toJson())
        } ?: throw IllegalArgumentException("unknown mode")
    }

    fun setMods(mods: Collection<LazerMod>, mode: OsuMode? = this.mode) {
        mode?.let {
            nativeSetModsByStr(it.value.toByte(), mods.toJson())
        } ?: throw IllegalArgumentException("unknown mode")
    }

    fun setMods(legacy: Int, json: String, mode: OsuMode? = this.mode) {
        mode?.let {
            nativeSetModsMix(it.value.toByte(), legacy, json)
        } ?: throw IllegalArgumentException("unknown mode")
    }

    fun setMods(legacy: Int, vararg mods: LazerMod) {
        mode?.let {
            nativeSetModsMix(it.value.toByte(), legacy, mods.toJson())
        } ?: throw IllegalArgumentException("unknown mode")
    }

    fun setMods(legacy: Int, mods: Collection<LazerMod>, mode: OsuMode? = this.mode) {
        mode?.let {
            nativeSetModsMix(it.value.toByte(), legacy, mods.toJson())
        } ?: throw IllegalArgumentException("unknown mode")
    }

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

    fun calculate(beatmap: JniBeatmap): JniDifficultyAttributes {
        return nativeCalculate(beatmap.getPtr())
    }

    private external fun nativeSetAr(value: Float, withMods: Boolean)
    private external fun nativeSetOd(value: Float, withMods: Boolean)
    private external fun nativeSetCs(value: Float, withMods: Boolean)
    private external fun nativeSetHp(value: Float, withMods: Boolean)
    private external fun setLazer(bool: Boolean)
    private external fun setHardrock(bool: Boolean)
    private external fun setPassObject(sum: Int)
    private external fun nativeSetMods(legacy: Int)
    private external fun nativeSetModsByStr(mode: Byte, lazer: String)
    private external fun nativeSetModsMix(mode: Byte, legacy: Int, lazer: String)
    private external fun nativeSetClockRate(value: Double)

    private external fun nativeCalculate(beatmap: Long): JniDifficultyAttributes

    init {
        initDifficulty(withMods, isLazer, isAllNull())
    }
}
