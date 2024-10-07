package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapPlaycount(
    @JsonProperty("beatmap_id")
    var beatmapID: Long,

    @JsonProperty("beatmap")
    var beatmap: Beatmap?,

    @JsonProperty("beatmapset")
    var beatmapset: Beatmapset?,

    @JsonProperty("count")
    var count: Int
)
