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
            assertEquals(beatmap.objects, 2126)
            val difficulty = JniDifficulty()
            difficulty.mode = OsuMode.Osu
            var attr = difficulty.calculate(beatmap)
            assert(attr.getStarRating() - 6.38 in -0.01..0.01)
            difficulty.setMods(LazerMod.DifficultyAdjust(circleSize = 6f))
            attr = difficulty.calculate(beatmap)
            assert(attr.getStarRating() - 7.12 in -0.01..0.01)

            val b = beatmap.createPerformance().apply {
                setLazer(true)
                setPassedObjects(2126)
            }

            assert(b.calculate().getPP() - 468 in -1.0..1.0)

            var performanceAttributes = beatmap.createPerformance().apply {
                setLazer(false)
                setAcc(97.39)
                setCombo(3054)
                setN300(2045)
                setN100(76)
                setN50(1)
                setMisses(4)
            }.calculate()
            assert(performanceAttributes is OsuPerformanceAttributes)
            if (performanceAttributes is OsuPerformanceAttributes) {
                println(performanceAttributes.pp)
                assert(performanceAttributes.pp - 296.31 in -0.05..0.05)
            }
            performanceAttributes = beatmap.createPerformance().apply {
                setMods(
                    LazerMod.DoubleTime(speedChange = 1.1f),
                    LazerMod.Hidden(),
                )
                setCombo(3320)
                setLargeTick(507)
                setSliderEnds(587)
                setN300(2113)
                setN100(13)
                setN50(0)
                setLazer(true)
            }.calculate()
            if (performanceAttributes is OsuPerformanceAttributes) {
                assert(performanceAttributes.pp - 628.0 in -1.0..1.0)
            }
        }
    }
}