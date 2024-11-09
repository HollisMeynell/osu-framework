package org.spring.osu.extended.rosu

class JniScoreState private constructor(init:Boolean): NativeClass(3) {
    constructor(): this(true)

    var maxCombo: Int = 0
        external set
    var sliderTickHits: Int = 0
        external set
    var sliderEndHits: Int = 0
        external set
    var geki: Int = 0
        external set
    var katu: Int = 0
        external set
    var n300: Int = 0
        external set
    var n100: Int = 0
        external set
    var n50: Int = 0
        external set
    var misses: Int = 0
        external set

    init {
        if (init) initState()
    }
    private external fun initState()
    companion object {
        internal fun withoutInit(): JniScoreState {
            return JniScoreState(false)
        }
    }
}