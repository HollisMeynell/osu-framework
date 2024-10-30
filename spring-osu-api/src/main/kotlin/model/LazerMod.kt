package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.spring.osu.OsuMod

data class LazerMod(
    @JsonProperty("acronym")
    val type: String,
    @JsonProperty("settings")
    val settings: Map<String, Number>?,
){
    /**
     * @return maybe be Other if the mod is not found
     */
    @JsonIgnore
    fun tryGetOsuMod(): OsuMod {
        return OsuMod.getMod(type)
    }
}
