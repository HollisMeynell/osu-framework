@file:Suppress("unused")

package org.spring.osu.extended.rosu


import org.spring.osu.OsuMode
import java.nio.file.Path

class JniBeatmap private constructor() : NativeClass(0) {
    constructor(data: ByteArray) : this() {
        parseByBytes(data)
    }

    constructor(path: Path) : this() {
        parseByPath(path.toString())
    }

    private var modeValue: Int = 0

    val mode: OsuMode
        get() = OsuMode.getMode(modeValue)

    var ar: Float = 0.0f
        private set

    var od: Float = 0.0f
        private set

    var cs: Float = 0.0f
        private set

    var hp: Float = 0.0f
        private set

    var stackLeniency: Float = 0.0f
        private set

    var bpm: Double = 0.0
        private set

    var sliderMultiplier: Double = 0.0
        private set

    var sliderTickTate: Double = 0.0
        private set

    var objects: Int = 0
        private set

    fun convertInPlace(mode: OsuMode): Boolean {
        modeValue = mode.value
        return convertInPlace(modeValue.toByte())
    }

    fun setGameMode(mode: OsuMode) = convertInPlace(mode)

    fun createDifficulty(): JniDifficulty {
        return JniDifficulty(mode = mode)
    }

    @JvmOverloads
    fun createPerformance(state: JniScoreState? = null): JniPerformance {
        return JniPerformance.createByBeatmap(this, state)
    }


    private external fun convertInPlace(mode: Byte): Boolean
    private external fun parseByBytes(map: ByteArray)
    private external fun parseByPath(local: String)

    override fun toString(): String {
        return """
            |JniBeatmap(
            |   mode=$mode,
            |   ar=$ar,
            |   od=$od,
            |   cs=$cs,
            |   hp=$hp,
            |   stackLeniency=$stackLeniency,
            |   bpm=$bpm,
            |   sliderMultiplier=$sliderMultiplier,
            |   sliderTickTate=$sliderTickTate
            |)
        """.trimMargin()
    }
}
