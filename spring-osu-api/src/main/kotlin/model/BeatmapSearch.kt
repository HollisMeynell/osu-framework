package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapSearch(
    @JsonProperty("rule")
    var rule: String?,

    @JsonProperty("error")
    var error: String?,

    @JsonProperty("result_count")
    var resultCount: Int?,

    @JsonProperty("beatmapsets")
    var beatmapsets: List<Beatmapset>,

    @JsonProperty("total")
    var total: Int,

    @JsonProperty("cursor_string")
    var cursorString: String?,

    @JsonProperty("cursor")
    @Deprecated("Use cursorString instead")
    var cursor: SearchCursor?,

    @JsonProperty("search")
    var search: String,
) {
    @Deprecated("Use cursorString instead")
    data class SearchCursor(
        @JsonProperty("queued_at")
        var queuedAt: String,

        @JsonProperty("approved_data")
        var approved: String,

        @JsonProperty("id")
        var id: Int,
    )

    data class Search(
        @JsonProperty("c")
        var general: String,

        @JsonProperty("sort")
        var sort: String,

        @JsonProperty("s")
        var status: String,

        @JsonProperty("nsfw")
        var nsfw: Boolean,

        @JsonProperty("g")
        var genre: Byte,

        @JsonProperty("l")
        var language: Byte,

        @JsonProperty("e")
        var others: String,

        @JsonProperty("r")
        var rank: String,

        @JsonProperty("played")
        var played: String,
    )
}
