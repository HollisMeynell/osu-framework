package rosu

import org.junit.jupiter.api.Test
import org.spring.core.toJson
import org.spring.osu.OsuMode
import org.spring.osu.extended.rosu.JniBeatmap
import org.spring.osu.extended.rosu.JniDifficulty
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
            val difficulty = JniDifficulty(cs = 7f, withMods = false, isLazer = false)
            difficulty.setMods(x, beatmap.mode)
            val attr = difficulty.calculate(beatmap)
            assertEquals(attr.getStarRating(), 6.415588803667541)
            difficulty.setCs(beatmap.cs, false)
            assertEquals(difficulty.calculate(beatmap).getStarRating(), 5.053939235628658)
            val b = beatmap.createPerformance().apply { setLazer(true) }
            assertEquals(b.calculate().getPP(), 99.78179200276945)
            var performanceAttributes = beatmap.createPerformance().apply {
                generateState()
                setCombo(2264)
                setN300(1859)
                setN100(57)
                setN50(1)
                setMisses(6)
                setSliderEnds(999)
                setLargeTick(999)
            }.calculate()
            assert(performanceAttributes is OsuPerformanceAttributes)
            if (performanceAttributes is OsuPerformanceAttributes) {
                assertEquals(performanceAttributes.pp, 30.561301593138058)
            }
            performanceAttributes = beatmap.createPerformance().apply {
                generateState()
                setMods(LazerMod.DifficultyAdjust(circleSize = 7f))
                setCombo(2264)
                setN300(1859)
                setN100(57)
                setN50(1)
                setMisses(6)
                setSliderEnds(999)
                setLargeTick(999)
            }.calculate()
            if (performanceAttributes is OsuPerformanceAttributes) {
                assertEquals(performanceAttributes.pp, 72.88549841525916)
            }
        }
    }
}