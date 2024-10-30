package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MatchSearch(
    @JsonProperty("matches")
    var matches: List<Match.MatchInfo>,

    @JsonProperty("cursor_string")
    var cursorString: String?,

    @JsonProperty("params")
    var params: MatchSearchParams?,
) {
    data class MatchSearchParams(
        @JsonProperty("limit")
        var limit: Int,

        @JsonProperty("sort")
        var sort: SortType,
    )

    enum class SortType(val sort:String) {
        Asc("id_asc"), Desc("id_desc");
    }
}
