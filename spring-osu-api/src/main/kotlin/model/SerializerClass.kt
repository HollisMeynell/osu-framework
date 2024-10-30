package org.spring.osu.model

import com.fasterxml.jackson.databind.util.StdConverter


internal class BeatmapsetConverter : StdConverter<Beatmapset, Beatmapset>() {
    override fun convert(value: Beatmapset): Beatmapset {
        value.beatmaps?.forEach { it.beatmapset = null }
        return value
    }
}

internal class BeatmapConverter : StdConverter<Beatmap, Beatmap>() {
    override fun convert(value: Beatmap): Beatmap {
        value.beatmapset = null
        return value
    }
}