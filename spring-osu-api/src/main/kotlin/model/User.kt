package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import java.time.LocalDate
import java.time.OffsetDateTime


data class User(
    @field:JsonProperty("avatar_url")
    override var avatarUrl: String,

    @field:JsonProperty("country_code")
    override var countryCode: String,

    @field:JsonProperty("default_group")
    override var defaultGroup: String?,

    @field:JsonProperty("id")
    override var id: Long,

    @field:JsonProperty("is_active")
    override var isActive: Boolean,

    @field:JsonProperty("is_bot")
    override var isBot: Boolean,

    @field:JsonProperty("is_deleted")
    override var isDeleted: Boolean,

    @field:JsonProperty("is_online")
    override var isOnline: Boolean,

    @field:JsonProperty("is_supporter")
    override var isSupporter: Boolean,

    @field:JsonProperty("last_visit")
    override var lastVisit: OffsetDateTime?,

    @field:JsonProperty("pm_friends_only")
    override var pmFriendsOnly: Boolean,

    @field:JsonProperty("profile_colour")
    override var profileColour: String?,

    @field:JsonProperty("username")
    override var username: String,

    // Optional fields
    @field:JsonProperty("account_history")
    var accountHistory: List<UserAccountHistory>?,

    @field:JsonProperty("active_tournament_banner")
    var activeTournamentBanner: ProfileBanner?,

    @field:JsonProperty("active_tournament_banners")
    var activeTournamentBanners: List<ProfileBanner>?,

    @field:JsonProperty("badges")
    var badges: List<UserBadge>?,

    @field:JsonProperty("beatmap_playcounts_count")
    var beatmapPlaycountsCount: Int?,

    @field:JsonProperty("country")
    override var country: Country?,

    @field:JsonProperty("cover")
    override var cover: UserCover?,

    @field:JsonProperty("favourite_beatmapset_count")
    var favouriteBeatmapsetCount: Int?,

    @field:JsonProperty("follow_user_mapping")
    var followUserMapping: List<Int>?,

    @field:JsonProperty("follower_count")
    var followerCount: Int?,

    @field:JsonProperty("graveyard_beatmapset_count")
    var graveyardBeatmapsetCount: Int?,

    @field:JsonProperty("groups")
    override var groups: List<UserGroup>?,

    @field:JsonProperty("guest_beatmapset_count")
    var guestBeatmapsetCount: Int?,

    @field:JsonProperty("is_restricted")
    var isRestricted: Boolean?,

    @field:JsonProperty("kudosu")
    var kudosu: Kudosu?,

    @field:JsonProperty("loved_beatmapset_count")
    var lovedBeatmapsetCount: Int?,

    @field:JsonProperty("mapping_follower_count")
    var mappingFollowerCount: Int?,

    @field:JsonProperty("monthly_playcounts")
    var monthlyPlaycounts: List<DateCount>?,

    @field:JsonProperty("page")
    var page: UserPage?,

    @field:JsonProperty("pending_beatmapset_count")
    var pendingBeatmapsetCount: Int?,

    @field:JsonProperty("previous_usernames")
    var previousUsernames: List<String>?,

    @field:JsonProperty("rank_highest")
    var rankHighest: RankHighest?,

    @field:JsonProperty("rank_history")
    var rankHistory: RankHistory?,

    @field:JsonProperty("ranked_beatmapset_count")
    var rankedBeatmapsetCount: Int?,

    @field:JsonProperty("replays_watched_counts")
    var replaysWatchedCounts: List<DateCount>?,

    @field:JsonProperty("scores_best_count")
    var scoresBestCount: Int?,

    @field:JsonProperty("scores_first_count")
    var scoresFirstCount: Int?,

    @field:JsonProperty("scores_recent_count")
    var scoresRecentCount: Int?,

    @field:JsonProperty("session_verified")
    var sessionVerified: Boolean?,

    @field:JsonProperty("statistics")
    override var statistics: UserStatistics?,

    @field:JsonProperty("statistics_rulesets")
    var statisticsRulesets: UserStatisticsRulesets?,

    @field:JsonProperty("support_level")
    override var supportLevel: Int?,

    @field:JsonProperty("unread_pm_count")
    var unreadPmCount: Int?,

    @field:JsonProperty("user_achievements")
    var userAchievements: List<UserAchievement>?,

    @field:JsonProperty("cover_url")
    var coverUrl: String?,

    @field:JsonProperty("discord")
    var discord: String?,

    @field:JsonProperty("has_supported")
    var hasSupported: Boolean?,

    @field:JsonProperty("interests")
    var interests: String?,

    @field:JsonProperty("join_date")
    var joinDate: OffsetDateTime?,

    @field:JsonProperty("location")
    var location: String?,

    @field:JsonProperty("max_blocks")
    var maxBlocks: Int?,

    @field:JsonProperty("max_friends")
    var maxFriends: Int?,

    @field:JsonProperty("occupation")
    var occupation: String?,

    @field:JsonProperty("playmode")
    @field:JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @field:JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var playmode: OsuMode?,

    @field:JsonProperty("playstyle")
    var playstyle: List<String>?,

    @field:JsonProperty("post_count")
    var postCount: Int?,

    @field:JsonProperty("profile_hue")
    var profileHue: Int?,

    /**
     * include [`me` ,`recent_activity` ,`beatmaps` ,`historical` ,`kudosu` ,`top_ranks` ,`medals`]
     */
    @field:JsonProperty("profile_order")
    var profileOrder: List<String>?,

    @field:JsonProperty("title")
    var title: String?,

    @field:JsonProperty("title_url")
    var titleUrl: String?,

    @field:JsonProperty("twitter")
    var twitter: String?,

    @field:JsonProperty("website")
    var website: String?,

    @field:JsonProperty("team")
    var team: UserTeam?,
) :Friend{
    data class Kudosu(
        @field:JsonProperty("available")
        val available:Int,

        @field:JsonProperty("total")
        val total:Int,
    )

    data class ProfileBanner(
        @field:JsonProperty("id")
        var id: Int,

        @field:JsonProperty("tournament_id")
        var tournamentID: Int,

        @field:JsonProperty("image")
        var image: String?,

        @field:JsonProperty("image@2x")
        var image2x: String?
    )

    data class RankHighest (
        @field:JsonProperty("rank")
        var rank: Int,

        @field:JsonProperty("updated_at")
        var updatedAt: OffsetDateTime,
    )

    data class RankHistory (
        @field:JsonProperty("data")
        var data: List<Int>,

        @field:JsonProperty("mode")
        @field:JsonSerialize(using = OsuMode.RulesetSerializer::class)
        @field:JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
        var mode: OsuMode,
    )

    data class UserBadge(
        @field:JsonProperty("awarded_at")
        var awardedAt: OffsetDateTime,

        @field:JsonProperty("description")
        var description: String,

        @field:JsonProperty("image@2x_url")
        var image2xUrl: String,

        @field:JsonProperty("image_url")
        var imageUrl: String,

        @field:JsonProperty("url")
        var url: String
    )

    data class UserAccountHistory(
        @field:JsonProperty("description")
        var description: String?,

        @field:JsonProperty("id")
        var id: Long,

        @field:JsonProperty("length")
        var length: Int,

        @field:JsonProperty("permanent")
        var permanent: Boolean,

        @field:JsonProperty("timestamp")
        var timestamp: OffsetDateTime,

        @field:JsonProperty("type")
        private var _type: String
    ) {
        @JsonIgnore
        val type: Type = when (_type) {
            "note" -> Type.Note
            "restriction" -> Type.Restriction
            "silence" -> Type.Silence
            "tournament_ban" -> Type.TournamentBan
            else -> Type.Other
        }
        enum class Type {
            Note,
            Restriction,
            Silence,
            TournamentBan,
            Other,
        }
    }

    data class UserAchievement(
        @field:JsonProperty("achieved_at")
        var achievedAt: OffsetDateTime,

        @field:JsonProperty("achievement_id")
        var achievementID: Int,
    )

    data class DateCount(
        @field:JsonProperty("count")
        var count: Int,

        @field:JsonProperty("start_date")
        var startDate: LocalDate,
    )

    data class UserPage(
        @field:JsonProperty("html")
        var html: String,

        @field:JsonProperty("raw")
        var raw: String,
    )

    data class UserCover(
        @field:JsonProperty("custom_url")
        var customUrl: String?,

        @field:JsonProperty("url")
        var url: String,

        @field:JsonProperty("id")
        var id: Long?,
    )

    data class UserTeam(
        @field:JsonProperty("flag_url")
        var flagUrl: String,
        @field:JsonProperty("id")
        var id: Long,
        @field:JsonProperty("name")
        var name: String,
        @field:JsonProperty("short_name")
        var shortName: String,
    )
}

