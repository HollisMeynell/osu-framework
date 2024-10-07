package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty

data class Covers(
    @JsonProperty("cover")
    var cover: String,

    @JsonProperty("cover@2x")
    var cover2x: String,

    @JsonProperty("card")
    var card: String,

    @JsonProperty("card@2x")
    var card2x: String,

    @JsonProperty("list")
    var list: String,

    @JsonProperty("list@2x")
    var list2x: String,

    @JsonProperty("slimcover")
    var slimcover: String,

    @JsonProperty("slimcover@2x")
    var slimcover2x: String,
)