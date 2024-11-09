package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.OffsetDateTime

@JsonDeserialize(`as` = User::class)
interface Friend {
    @get:JsonProperty("avatar_url")
    var avatarUrl: String

    @get:JsonProperty("country_code")
    var countryCode: String

    @get:JsonProperty("default_group")
    var defaultGroup: String?

    @get:JsonProperty("id")
    var id: Long

    @get:JsonProperty("is_active")
    var isActive: Boolean

    @get:JsonProperty("is_bot")
    var isBot: Boolean

    @get:JsonProperty("is_deleted")
    var isDeleted: Boolean

    @get:JsonProperty("is_online")
    var isOnline: Boolean

    @get:JsonProperty("is_supporter")
    var isSupporter: Boolean

    @get:JsonProperty("last_visit")
    var lastVisit: OffsetDateTime?

    @get:JsonProperty("pm_friends_only")
    var pmFriendsOnly: Boolean

    @get:JsonProperty("profile_colour")
    var profileColour: String?

    @get:JsonProperty("username")
    var username: String

    @get:JsonProperty("country")
    var country: Country?

    @get:JsonProperty("cover")
    var cover: User.UserCover?

    @get:JsonProperty("groups")
    var groups: List<UserGroup>?

    @get:JsonProperty("statistics")
    var statistics: UserStatistics?

    @get:JsonProperty("support_level")
    var supportLevel: Int?
}