package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.spring.osu.OsuMode

data class Nomination(
    @field:JsonProperty("beatmapset_id")
    var beatmapsetID: Long,

    @field:JsonProperty("rulesets")
    private var _rulesets: List<String>?,

    @field:JsonProperty("reset")
    var reset: Boolean,

    @field:JsonProperty("user_id")
    var userID: Long,
) {
    @JsonIgnore
    val rulesets: List<OsuMode>? = _rulesets?.map { OsuMode.getMode(it) }
}