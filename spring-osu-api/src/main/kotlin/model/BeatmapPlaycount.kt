package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapPlaycount(
    @field:JsonProperty("beatmap_id")
    var beatmapID: Long,

    @field:JsonProperty("beatmap")
    var beatmap: Beatmap?,

    @field:JsonProperty("beatmapset")
    var beatmapset: Beatmapset?,

    @field:JsonProperty("count")
    var count: Int
)
