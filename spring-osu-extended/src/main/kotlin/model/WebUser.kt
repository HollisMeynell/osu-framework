package org.spring.osu.extended.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import org.spring.osu.model.User
import java.time.OffsetDateTime

data class WebUser(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("username")
    val username: String,

    @JsonProperty("join_date")
    val joinDate: OffsetDateTime,

    @JsonProperty("is_supporter")
    val isSupporter: Boolean,

    @JsonProperty("follow_user_mapping")
    val followMapperID: List<Long>,

    @JsonProperty("friends")
    val friends: List<Friend>,

    @JsonProperty("cover")
    var cover: User.UserCover,

    @JsonProperty("kudosu")
    val kudosu: User.Kudosu,

    @JsonProperty("playmode")
    @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    val mode: OsuMode,

    @JsonProperty("unread_pm_count")
    val unreadPMCount: Int,
) {
    data class Friend(
        @JsonProperty("target_id")
        val targetId: Long,

        @JsonProperty("relation_type")
        val relationType: String,

        @JsonProperty("mutual")
        val mutual: Boolean
    )
}
