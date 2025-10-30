package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MatchSearch(
    @field:JsonProperty("matches")
    var matches: List<Match.MatchInfo>,

    @field:JsonProperty("cursor_string")
    var cursorString: String?,

    @field:JsonProperty("params")
    var params: MatchSearchParams?,
) {
    data class MatchSearchParams(
        @field:JsonProperty("limit")
        var limit: Int,

        @field:JsonProperty("sort")
        var sort: SortType,
    )

    enum class SortType(val sort:String) {
        Asc("id_asc"), Desc("id_desc");
    }
}
