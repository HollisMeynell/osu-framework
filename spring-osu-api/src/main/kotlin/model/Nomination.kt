package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.spring.osu.OsuMode

data class Nomination(
    @JsonProperty("beatmapset_id")
    var beatmapsetID: Long,

    @JsonProperty("rulesets")
    private var _rulesets: List<String>?,

    @JsonProperty("reset")
    var reset: Boolean,

    @JsonProperty("user_id")
    var userID: Long,
) {
    @JsonIgnore
    val rulesets: List<OsuMode>? = _rulesets?.map { OsuMode.valueOf(it) }
}