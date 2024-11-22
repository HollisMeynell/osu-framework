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
    private external fun initByBeatmapWithState(ptr: Long, state: JniScoreState)

    private external fun initByOsuDifficultyAttributes(ptr: Long)
    private external fun initByOsuDifficultyAttributesWithState(ptr: Long, state: JniScoreState)
    private external fun initByTaikoDifficultyAttributes(ptr: Long)
    private external fun initByTaikoDifficultyAttributesWithState(ptr: Long, state: JniScoreState)
    private external fun initByCatchDifficultyAttributes(ptr: Long)
    private external fun initByCatchDifficultyAttributesWithState(ptr: Long, state: JniScoreState)
    private external fun initByManiaDifficultyAttributes(ptr: Long)
    private external fun initByManiaDifficultyAttributesWithState(ptr: Long, state: JniScoreState)

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

    external fun setLazer(value: Boolean)
    external fun setHardrock(value: Boolean)
    external fun setClockRate(value: Double)
    external fun setHitResultPriority(value: Boolean)

    private external fun nativeSetState(state: JniScoreState)
    private external fun nativeSetDifficulty(ptr: Long)
    private external fun nativeSetMods(legacy: Int)
    private external fun nativeSetModsByStr(mode: Byte, lazer: String)
    private external fun nativeSetModsMix(mode: Byte, legacy: Int, lazer: String)

    external fun generateState(): JniScoreState
    private external fun nativeCalculate(): JniPerformanceAttributes

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
        nativeSetState(state)
    }

    fun setDifficulty(state: JniDifficulty) {
        nativeSetDifficulty(state.getPtr())
    }

    fun calculate(): JniPerformanceAttributes {
        if (ready().not()) throw IllegalStateException("performance can not be used")
        return nativeCalculate()
    }

    companion object {
        fun createByBeatmap(beatmap: JniBeatmap, state: JniScoreState? = null): JniPerformance {
            if (beatmap.ready().not()) throw IllegalArgumentException("beatmap can not be used")
            return JniPerformance(beatmap.mode).apply {
                if (state == null) {
                    initByBeatmap(beatmap.getPtr())
                } else {
                    initByBeatmapWithState(beatmap.getPtr(), state)
                }
            }
        }

        fun createByDifficultyAttributes(
            difficulty: JniDifficultyAttributes,
            state: JniScoreState? = null
        ): JniPerformance {
            val performance = JniPerformance(OsuMode.Default)
            if ((difficulty as NativeClass).ready().not()) throw IllegalArgumentException("difficulty can not be used")
            when (difficulty as JniDifficultyAttributes) {
                is OsuDifficultyAttributes -> {
                    performance.mode = OsuMode.Osu
                    if (state == null) {
                        performance.initByOsuDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByOsuDifficultyAttributesWithState(difficulty.getPtr(), state)
                    }
                }

                is TaikoDifficultyAttributes -> {
                    performance.mode = OsuMode.Taiko
                    if (state == null) {
                        performance.initByTaikoDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByTaikoDifficultyAttributesWithState(difficulty.getPtr(), state)
                    }
                }

                is CatchDifficultyAttributes -> {
                    performance.mode = OsuMode.Catch
                    if (state == null) {
                        performance.initByCatchDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByCatchDifficultyAttributesWithState(difficulty.getPtr(), state)
                    }
                }

                is ManiaDifficultyAttributes -> {
                    performance.mode = OsuMode.Mania
                    if (state == null) {
                        performance.initByManiaDifficultyAttributes(difficulty.getPtr())
                    } else {
                        performance.initByManiaDifficultyAttributesWithState(difficulty.getPtr(), state)
                    }
                }
            }
            return performance
        }

        @JvmStatic
        fun createState(
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