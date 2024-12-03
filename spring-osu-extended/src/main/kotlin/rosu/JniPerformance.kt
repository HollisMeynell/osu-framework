@file:Suppress("unused")

package org.spring.osu.extended.rosu

import org.spring.core.toJson
import org.spring.osu.OsuMode
import org.spring.osu.model.LazerMod
import org.spring.osu.model.OsuMod

class JniPerformance private constructor(
    var mode: OsuMode,
) : NativeClass(2) {

    private external fun initByBeatmap(ptr: Long)
    private external fun initByBeatmapWithState(ptr: Long, state: ByteArray)

    private external fun initByOsuDifficultyAttributes(ptr: Long)
    private external fun initByOsuDifficultyAttributesWithState(ptr: Long, state: ByteArray)
    private external fun initByTaikoDifficultyAttributes(ptr: Long)
    private external fun initByTaikoDifficultyAttributesWithState(ptr: Long, state: ByteArray)
    private external fun initByCatchDifficultyAttributes(ptr: Long)
    private external fun initByCatchDifficultyAttributesWithState(ptr: Long, state: ByteArray)
    private external fun initByManiaDifficultyAttributes(ptr: Long)
    private external fun initByManiaDifficultyAttributesWithState(ptr: Long, state: ByteArray)

    external fun setCombo(value: Int)
    external fun setGeki(value: Int)
    external fun setKatu(value: Int)
    external fun setN300(value: Int)
    external fun setN100(value: Int)
    external fun setN50(value: Int)
    external fun setMisses(value: Int)
    external fun setLargeTick(value: Int)
    external fun setSliderEnds(value: Int)
    external fun setPassedObjects(value: Int)
    external fun setHardrock(value: Boolean)
    external fun setClockRate(value: Double)
    external fun setHitResultPriority(value: Boolean)

    private external fun nativeSetAr(ar: Float, withMods: Boolean)
    private external fun nativeSetOd(od: Float, withMods: Boolean)
    private external fun nativeSetCs(cs: Float, withMods: Boolean)
    private external fun nativeSetHp(hp: Float, withMods: Boolean)
    private external fun nativeSetLazer(lazer: Boolean)
    private external fun nativeSetState(state: ByteArray)
    private external fun nativeSetDifficulty(ptr: Long)
    private external fun nativeSetMods(legacy: Int)
    private external fun nativeSetAccuracy(acc: Double)
    private external fun nativeSetModsByStr(mode: Byte, lazer: String)
    private external fun nativeSetModsMix(mode: Byte, legacy: Int, lazer: String)
    private external fun convertInPlace(mode: Byte)
    external fun generateState(): JniScoreState
    private external fun nativeCalculate(): JniPerformanceAttributes


    fun setLazer(value: Boolean) = nativeSetLazer(value)

    fun isLazer(value: Boolean) = nativeSetLazer(value)

    fun convertInPlace(mode: OsuMode): Boolean {
        if (this.mode != OsuMode.Osu && this.mode != mode) {
            return false
        }
        if (this.mode == mode) return true
        this.mode = mode
        convertInPlace(mode.value.toByte())
        return true
    }

    fun setGameMode(mode: OsuMode) = convertInPlace(mode)

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
        nativeSetModsByStr(mode.value.toByte(), mods.toJson())
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
        nativeSetModsMix(mode.value.toByte(), legacy, mods.toJson())
    }

    fun setMods(legacy: Int, mods: Collection<LazerMod>, mode: OsuMode? = this.mode) {
        mode?.let {
            nativeSetModsMix(it.value.toByte(), legacy, mods.toJson())
        } ?: throw IllegalArgumentException("unknown mode")
    }

    fun setState(state: JniScoreState) {
        nativeSetState(state.serialize())
    }

    fun setDifficulty(state: JniDifficulty) {
        nativeSetDifficulty(state.getPtr())
    }

    fun setAcc(acc: Double) {
        if (acc < 0 || acc > 100) throw IllegalArgumentException("accuracy must be in range 0..100")
        val accuracy = if (acc > 0 && acc < 1) {
            acc * 100
        } else {
            acc
        }
        nativeSetAccuracy(accuracy)
    }

    fun calculate(): JniPerformanceAttributes {
        if (ready().not()) throw IllegalStateException("performance can not be used")
        return nativeCalculate()
    }

    companion object {
        @JvmOverloads
        fun createByBeatmap(beatmap: JniBeatmap, state: JniScoreState? = null): JniPerformance {
            if (beatmap.ready().not()) throw IllegalArgumentException("beatmap can not be used")
            return JniPerformance(beatmap.mode).apply {
                if (state == null) {
                    initByBeatmap(beatmap.getPtr())
                } else {
                    initByBeatmapWithState(beatmap.getPtr(), state.serialize())
                }
            }
        }

        @JvmOverloads
        fun createByDifficultyAttributes(
            difficulty: JniDifficultyAttributes,
            state: JniScoreState? = null
        ): JniPerformance {
            val performance = JniPerformance(OsuMode.Default)
            if (difficulty is NativeClass && difficulty.ready().not())
                throw IllegalArgumentException("difficulty can not be used")
            when (difficulty) {
                is OsuDifficultyAttributes -> {
                    performance.mode = OsuMode.Osu
                    if (state == null) {
                        performance.initByOsuDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByOsuDifficultyAttributesWithState(difficulty.getPtr(), state.serialize())
                    }
                }

                is TaikoDifficultyAttributes -> {
                    performance.mode = OsuMode.Taiko
                    if (state == null) {
                        performance.initByTaikoDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByTaikoDifficultyAttributesWithState(difficulty.getPtr(), state.serialize())
                    }
                }

                is CatchDifficultyAttributes -> {
                    performance.mode = OsuMode.Catch
                    if (state == null) {
                        performance.initByCatchDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByCatchDifficultyAttributesWithState(difficulty.getPtr(), state.serialize())
                    }
                }

                is ManiaDifficultyAttributes -> {
                    performance.mode = OsuMode.Mania
                    if (state == null) {
                        performance.initByManiaDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByManiaDifficultyAttributesWithState(difficulty.getPtr(), state.serialize())
                    }
                }
            }
            return performance
        }
    }
}