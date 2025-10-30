package org.spring.osu.extended.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.spring.osu.OsuMode
import org.spring.osu.model.User
import java.time.OffsetDateTime

data class WebUser(
    @field:JsonProperty("id")
    val id: Long,

    @field:JsonProperty("username")
    val username: String,

    @field:JsonProperty("join_date")
    val joinDate: OffsetDateTime,

    @field:JsonProperty("is_supporter")
    val isSupporter: Boolean,

    @field:JsonProperty("follow_user_mapping")
    val followMapperID: List<Long>,

    @field:JsonProperty("friends")
    val friends: List<Friend>,

    @field:JsonProperty("cover")
    var cover: User.UserCover,

    @field:JsonProperty("kudosu")
    val kudosu: User.Kudosu,

    @field:JsonProperty("playmode")
    @field:JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    val mode: OsuMode,

    @field:JsonProperty("unread_pm_count")
    val unreadPMCount: Int,
) {
    data class Friend(
        @field:JsonProperty("target_id")
        val targetId: Long,

        @field:JsonProperty("relation_type")
        val relationType: String,

        @field:JsonProperty("mutual")
        val mutual: Boolean
    )
}
