package org.spring.osu.extended.rosu

data class JniScoreState (
    val maxCombo: Int = 0,
    val largeTickHits: Int = 0,
    val sliderEndHits: Int = 0,
    val geki: Int = 0,
    val katu: Int = 0,
    val n300: Int = 0,
    val n100: Int = 0,
    val n50: Int = 0,
    val misses: Int = 0,
)