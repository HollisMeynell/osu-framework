package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Country(
    @field:JsonProperty("code")
    val code:String,

    @field:JsonProperty("name")
    val name:String,
)
