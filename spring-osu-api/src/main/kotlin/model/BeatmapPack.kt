package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class BeatmapPack(
    @JsonProperty("author")
    var author: String,

    @JsonProperty("date")
    var date: LocalDate ,

    @JsonProperty("name")
    var name: String,

    @JsonProperty("no_diff_reduction")
    var noDiffReduction: Boolean,

    @JsonProperty("ruleset_id")
    var rulesetID: Int,

    @JsonProperty("tag")
    var tag: String,

    @JsonProperty("url")
    var url: String,

    @JsonProperty("beatmapsets")
    var beatmapsets: List<Beatmapset>?,

    @JsonProperty("user_completion_data")
    var userCompletionData: UserCompletionData?
) {
    data class UserCompletionData(
        @JsonProperty("beatmapset_ids")
        var beatmapsetIDs: List<Long>,

        @JsonProperty("completed")
        var completed: Boolean,
    )
}
