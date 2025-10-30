package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BeatmapSearch(
    @field:JsonProperty("rule")
    var rule: String?,

    @field:JsonProperty("error")
    var error: String?,

    @field:JsonProperty("result_count")
    var resultCount: Int?,

    @field:JsonProperty("beatmapsets")
    var beatmapsets: List<Beatmapset>,

    @field:JsonProperty("total")
    var total: Int,

    @field:JsonProperty("cursor_string")
    var cursorString: String?,

    @field:JsonProperty("cursor")
    @Deprecated("Use cursorString instead")
    var cursor: SearchCursor?,

    @field:JsonProperty("search")
    var search: String,
) {
    @Deprecated("Use cursorString instead")
    data class SearchCursor(
        @field:JsonProperty("queued_at")
        var queuedAt: String,

        @field:JsonProperty("approved_data")
        var approved: String,

        @field:JsonProperty("id")
        var id: Int,
    )

    data class Search(
        @field:JsonProperty("c")
        var general: String,

        @field:JsonProperty("sort")
        var sort: String,

        @field:JsonProperty("s")
        var status: String,

        @field:JsonProperty("nsfw")
        var nsfw: Boolean,

        @field:JsonProperty("g")
        var genre: Byte,

        @field:JsonProperty("l")
        var language: Byte,

        @field:JsonProperty("e")
        var others: String,

        @field:JsonProperty("r")
        var rank: String,

        @field:JsonProperty("played")
        var played: String,
    )
}
