package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LazerFriend(
    @field:JsonProperty("target_id")
    var targetId: Long,
    @field:JsonProperty("relation_type")
    var relationType: String,
    @field:JsonProperty("mutual")
    var isMutual: Boolean,
    @field:JsonProperty("target")
    var target: Friend,
)
