package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class BeatmapPack(
    @field:JsonProperty("author")
    var author: String,

    @field:JsonProperty("date")
    var date: LocalDate ,

    @field:JsonProperty("name")
    var name: String,

    @field:JsonProperty("no_diff_reduction")
    var noDiffReduction: Boolean,

    @field:JsonProperty("ruleset_id")
    var rulesetID: Int,

    @field:JsonProperty("tag")
    var tag: String,

    @field:JsonProperty("url")
    var url: String,

    @field:JsonProperty("beatmapsets")
    var beatmapsets: List<Beatmapset>?,

    @field:JsonProperty("user_completion_data")
    var userCompletionData: UserCompletionData?
) {
    data class UserCompletionData(
        @field:JsonProperty("beatmapset_ids")
        var beatmapsetIDs: List<Long>,

        @field:JsonProperty("completed")
        var completed: Boolean,
    )
}
