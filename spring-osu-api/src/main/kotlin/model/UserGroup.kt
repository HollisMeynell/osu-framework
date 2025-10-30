package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.spring.osu.OsuMode

data class UserGroup(
    @field:JsonProperty("colour")
    var colour: String?,

    @field:JsonProperty("has_listing")
    var hasListing: Boolean?,

    @field:JsonProperty("has_playmodes")
    var hasPlaymodes: Boolean?,

    @field:JsonProperty("id")
    var id: Int,

    @field:JsonProperty("identifier")
    var identifier: String,

    @field:JsonProperty("is_probationary")
    var isProbationary: Boolean?,

    @field:JsonProperty("name")
    var name: String,

    @field:JsonProperty("short_name")
    var shortName: String,

    @field:JsonProperty("description")
    var description: String?,

    @field:JsonProperty("playmodes")
    private var _playModes: List<String>?,
) {
    @JsonIgnore
    val playModes: List<OsuMode>? = _playModes?.map { OsuMode.getMode(it) }

    data class Description(
        @field:JsonProperty("html")
        var html: String,

        @field:JsonProperty("markdown")
        var markdown: String
    )
}
