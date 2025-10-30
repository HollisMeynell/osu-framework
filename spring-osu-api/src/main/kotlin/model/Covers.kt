package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Covers(
    @field:JsonProperty("cover")
    var cover: String,

    @field:JsonProperty("cover@2x")
    var cover2x: String,

    @field:JsonProperty("card")
    var card: String,

    @field:JsonProperty("card@2x")
    var card2x: String,

    @field:JsonProperty("list")
    var list: String,

    @field:JsonProperty("list@2x")
    var list2x: String,

    @field:JsonProperty("slimcover")
    var slimcover: String,

    @field:JsonProperty("slimcover@2x")
    var slimcover2x: String,
)