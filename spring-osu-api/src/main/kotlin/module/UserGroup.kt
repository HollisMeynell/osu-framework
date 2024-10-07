package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.spring.osu.OsuMode

data class UserGroup(
    @JsonProperty("colour")
    var colour: String?,

    @JsonProperty("has_listing")
    var hasListing: Boolean?,

    @JsonProperty("has_playmodes")
    var hasPlaymodes: Boolean?,

    @JsonProperty("id")
    var id: Int,

    @JsonProperty("identifier")
    var identifier: String,

    @JsonProperty("is_probationary")
    var isProbationary: Boolean?,

    @JsonProperty("name")
    var name: String,

    @JsonProperty("short_name")
    var shortName: String,

    @JsonProperty("description")
    var description: String?,

    @JsonProperty("playmodes")
    private var _playModes: List<String>?,
) {
    @JsonIgnore
    val playModes: List<OsuMode>? = _playModes?.map { OsuMode.getMode(it) }

    data class Description(
        @JsonProperty("html")
        var html: String,

        @JsonProperty("markdown")
        var markdown: String
    )
}
