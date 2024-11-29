package rosu

import org.junit.jupiter.api.Test
import org.spring.core.toJson
import org.spring.osu.OsuMode
import org.spring.osu.extended.rosu.JniBeatmap
import org.spring.osu.extended.rosu.OsuPerformanceAttributes
import org.spring.osu.model.LazerMod
import kotlin.test.assertEquals

class NativeClassTest {
    @Test
    fun test() {
        val x = listOf(
            LazerMod.DifficultyAdjust(approachRate = 7f),
            LazerMod.DoubleTime(speedChange = 1.3f),
            LazerMod.Hidden(),
        )
        assertEquals(x.toJson(), """[{"acronym":"DA","settings":{"approach_rate":7.0}},{"acronym":"DT","settings":{"speed_change":1.3}},{"acronym":"HD"}]""")

        val beatmapDate = this::class.java.getResource("/beatmap/map.osu")
            ?.openStream()?.readAllBytes() ?: error("beatmap not found")
        JniBeatmap(beatmapDate).use { beatmap ->
            assertEquals(beatmap.mode, OsuMode.Osu)
            assertEquals(beatmap.objects, 323)
            val b = beatmap.createPerformance().apply { setLazer(true) }
            assertEquals(b.calculate().getPP(), 99.78179200276945)
            val performanceAttributes = beatmap.createPerformance().apply {
                generateState()
                setMods(LazerMod.DifficultyAdjust(circleSize = 7f))
                setCombo(2264)
                setN300(1859)
                setN100(57)
                setN50(1)
                setMisses(6)
                setSliderEnds(0)
                setLargeTick(0)
            }.calculate()
            assert(performanceAttributes is OsuPerformanceAttributes)
            if (performanceAttributes is OsuPerformanceAttributes) {
                assertEquals(performanceAttributes.pp, 22.627379384290478)
            }
        }
    }
}