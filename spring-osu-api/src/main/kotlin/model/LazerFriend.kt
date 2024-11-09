package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LazerFriend(
    @JsonProperty("target_id")
    var targetId: Long,
    @JsonProperty("relation_type")
    var relationType: String,
    @JsonProperty("mutual")
    var isMutual: Boolean,
    @JsonProperty("target")
    var target: Friend,
)
