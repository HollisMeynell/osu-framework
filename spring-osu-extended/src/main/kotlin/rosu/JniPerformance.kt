package org.spring.osu.extended.rosu

class JniPerformance private constructor() : NativeClass(2) {
    private external fun initByBeatmap(beatmap: JniBeatmap)
    var state = JniScoreState.withoutInit()
        set(value) {
            field.close()
            field = value
        }

    override fun close() {
        state.close()
        super.close()
    }

    fun calculate() {

    }

    companion object {
        fun createByBeatmap(beatmap: JniBeatmap): JniPerformance {
            return JniPerformance().apply {
                initByBeatmap(beatmap)
            }
        }
    }
}