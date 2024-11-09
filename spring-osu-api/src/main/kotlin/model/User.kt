package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import java.time.LocalDate
import java.time.OffsetDateTime


data class User(
    @JsonProperty("avatar_url")
    override var avatarUrl: String,

    @JsonProperty("country_code")
    override var countryCode: String,

    @JsonProperty("default_group")
    override var defaultGroup: String?,

    @JsonProperty("id")
    override var id: Long,

    @JsonProperty("is_active")
    override var isActive: Boolean,

    @JsonProperty("is_bot")
    override var isBot: Boolean,

    @JsonProperty("is_deleted")
    override var isDeleted: Boolean,

    @JsonProperty("is_online")
    override var isOnline: Boolean,

    @JsonProperty("is_supporter")
    override var isSupporter: Boolean,

    @JsonProperty("last_visit")
    override var lastVisit: OffsetDateTime?,

    @JsonProperty("pm_friends_only")
    override var pmFriendsOnly: Boolean,

    @JsonProperty("profile_colour")
    override var profileColour: String?,

    @JsonProperty("username")
    override var username: String,

    // Optional fields
    @JsonProperty("account_history")
    var accountHistory: List<UserAccountHistory>?,

    @JsonProperty("active_tournament_banner")
    var activeTournamentBanner: ProfileBanner?,

    @JsonProperty("active_tournament_banners")
    var activeTournamentBanners: List<ProfileBanner>?,

    @JsonProperty("badges")
    var badges: List<UserBadge>?,

    @JsonProperty("beatmap_playcounts_count")
    var beatmapPlaycountsCount: Int?,

    @JsonProperty("country")
    override var country: Country?,

    @JsonProperty("cover")
    override var cover: UserCover?,

    @JsonProperty("favourite_beatmapset_count")
    var favouriteBeatmapsetCount: Int?,

    @JsonProperty("follow_user_mapping")
    var followUserMapping: List<Int>?,

    @JsonProperty("follower_count")
    var followerCount: Int?,

    @JsonProperty("graveyard_beatmapset_count")
    var graveyardBeatmapsetCount: Int?,

    @JsonProperty("groups")
    override var groups: List<UserGroup>?,

    @JsonProperty("guest_beatmapset_count")
    var guestBeatmapsetCount: Int?,

    @JsonProperty("is_restricted")
    var isRestricted: Boolean?,

    @JsonProperty("kudosu")
    var kudosu: Kudosu?,

    @JsonProperty("loved_beatmapset_count")
    var lovedBeatmapsetCount: Int?,

    @JsonProperty("mapping_follower_count")
    var mappingFollowerCount: Int?,

    @JsonProperty("monthly_playcounts")
    var monthlyPlaycounts: List<DateCount>?,

    @JsonProperty("page")
    var page: UserPage?,

    @JsonProperty("pending_beatmapset_count")
    var pendingBeatmapsetCount: Int?,

    @JsonProperty("previous_usernames")
    var previousUsernames: List<String>?,

    @JsonProperty("rank_highest")
    var rankHighest: RankHighest?,

    @JsonProperty("rank_history")
    var rankHistory: RankHistory?,

    @JsonProperty("ranked_beatmapset_count")
    var rankedBeatmapsetCount: Int?,

    @JsonProperty("replays_watched_counts")
    var replaysWatchedCounts: List<DateCount>?,

    @JsonProperty("scores_best_count")
    var scoresBestCount: Int?,

    @JsonProperty("scores_first_count")
    var scoresFirstCount: Int?,

    @JsonProperty("scores_recent_count")
    var scoresRecentCount: Int?,

    @JsonProperty("session_verified")
    var sessionVerified: Boolean?,

    @JsonProperty("statistics")
    override var statistics: UserStatistics?,

    @JsonProperty("statistics_rulesets")
    var statisticsRulesets: UserStatisticsRulesets?,

    @JsonProperty("support_level")
    override var supportLevel: Int?,

    @JsonProperty("unread_pm_count")
    var unreadPmCount: Int?,

    @JsonProperty("user_achievements")
    var userAchievements: List<UserAchievement>?,

    @JsonProperty("cover_url")
    var coverUrl: String?,

    @JsonProperty("discord")
    var discord: String?,

    @JsonProperty("has_supported")
    var hasSupported: Boolean?,

    @JsonProperty("interests")
    var interests: String?,

    @JsonProperty("join_date")
    var joinDate: OffsetDateTime?,

    @JsonProperty("location")
    var location: String?,

    @JsonProperty("max_blocks")
    var maxBlocks: Int?,

    @JsonProperty("max_friends")
    var maxFriends: Int?,

    @JsonProperty("occupation")
    var occupation: String?,

    @JsonProperty("playmode")
    @JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var playmode: OsuMode?,

    @JsonProperty("playstyle")
    var playstyle: List<String>?,

    @JsonProperty("post_count")
    var postCount: Int?,

    @JsonProperty("profile_hue")
    var profileHue: Int?,

    /**
     * include [`me` ,`recent_activity` ,`beatmaps` ,`historical` ,`kudosu` ,`top_ranks` ,`medals`]
     */
    @JsonProperty("profile_order")
    var profileOrder: List<String>?,

    @JsonProperty("title")
    var title: String?,

    @JsonProperty("title_url")
    var titleUrl: String?,

    @JsonProperty("twitter")
    var twitter: String?,

    @JsonProperty("website")
    var website: String?,
) :Friend{
    data class Kudosu(
        @JsonProperty("available")
        val available:Int,

        @JsonProperty("total")
        val total:Int,
    )

    data class ProfileBanner(
        @JsonProperty("id")
        var id: Int,

        @JsonProperty("tournament_id")
        var tournamentID: Int,

        @JsonProperty("image")
        var image: String?,

        @JsonProperty("image@2x")
        var image2x: String?
    )

    data class RankHighest (
        @JsonProperty("rank")
        var rank: Int,

        @JsonProperty("updated_at")
        var updatedAt: OffsetDateTime,
    )

    data class RankHistory (
        @JsonProperty("data")
        var data: List<Int>,

        @JsonProperty("mode")
        @JsonSerialize(using = OsuMode.RulesetSerializer::class)
        @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
        var mode: OsuMode,
    )

    data class UserBadge(
        @JsonProperty("awarded_at")
        var awardedAt: OffsetDateTime,

        @JsonProperty("description")
        var description: String,

        @JsonProperty("image@2x_url")
        var image2xUrl: String,

        @JsonProperty("image_url")
        var imageUrl: String,

        @JsonProperty("url")
        var url: String
    )

    data class UserAccountHistory(
        @JsonProperty("description")
        var description: String?,

        @JsonProperty("id")
        var id: Long,

        @JsonProperty("length")
        var length: Int,

        @JsonProperty("permanent")
        var permanent: Boolean,

        @JsonProperty("timestamp")
        var timestamp: OffsetDateTime,

        @JsonProperty("type")
        private var _type: String
    ) {
        @JsonIgnore
        val type: Type = when (_type) {
            "note" -> Type.Note
            "restriction" -> Type.Restriction
            "silence" -> Type.Silence
            else -> throw IllegalArgumentException("Unknown type: $_type")
        }
        enum class Type {
            Note,
            Restriction,
            Silence,
        }
    }

    data class UserAchievement(
        @JsonProperty("achieved_at")
        var achievedAt: OffsetDateTime,

        @JsonProperty("achievement_id")
        var achievementID: Int,
    )

    data class DateCount(
        @JsonProperty("count")
        var count: Int,

        @JsonProperty("start_date")
        var startDate: LocalDate,
    )

    data class UserPage(
        @JsonProperty("html")
        var html: String,

        @JsonProperty("raw")
        var raw: String,
    )

    data class UserCover(
        @JsonProperty("custom_url")
        var customUrl: String?,

        @JsonProperty("url")
        var url: String,

        @JsonProperty("id")
        var id: Long?,
    )
}

