package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Country(
    @JsonProperty("code")
    val code:String,

    @JsonProperty("name")
    val name:String,
)
