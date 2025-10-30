package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.ObjectNode
import org.spring.osu.RankStatus

data class Beatmapset(
    @field:JsonProperty("artist")
    var artist: String,

    @field:JsonProperty("artist_unicode")
    var artistUnicode: String,

    @field:JsonProperty("covers")
    var covers: Covers,

    @field:JsonProperty("creator")
    var creator: String,

    @field:JsonProperty("favourite_count")
    var favouriteCount: Int,

    @field:JsonProperty("id")
    var id: Long,

    @field:JsonProperty("nsfw")
    var nsfw: Boolean,

    @field:JsonProperty("offset")
    var offset: Int,

    @field:JsonProperty("play_count")
    var playCount: Int,

    @field:JsonProperty("preview_url")
    var previewUrl: String,

    @field:JsonProperty("source")
    var source: String,

    @field:JsonProperty("status")
    @field:JsonSerialize(using = RankStatus.RankStatusSerializer::class)
    @field:JsonDeserialize(using = RankStatus.RankStatusDeserializer::class)
    var status: RankStatus,

    @field:JsonProperty("spotlight")
    var spotlight: Boolean,

    @field:JsonProperty("title")
    var title: String,

    @field:JsonProperty("title_unicode")
    var titleUnicode: String,

    @field:JsonProperty("user_id")
    var userID: Long,

    @field:JsonProperty("video")
    var video: Boolean,

    // Optional fields
    @field:JsonProperty("beatmaps")
    var beatmaps: List<Beatmap>? = null,

    @field:JsonProperty("converts")
    var converts: List<Beatmap>? = null,

    @field:JsonProperty("current_nominations")
    var currentNominations: List<Nomination>? = null,

    @field:JsonProperty("current_user_attributes")
    var currentUserAttributes: Any? = null,

    @field:JsonProperty("description")
    var description: Description? = null,

    @field:JsonProperty("discussions")
    var discussions: Description? = null,

    @field:JsonProperty("genre")
    var genre: ShowInfo? = null,

    @field:JsonProperty("language")
    var language: ShowInfo? = null,

    @field:JsonProperty("nominations")
    var nominations: ObjectNode? = null,

    @field:JsonProperty("pack_tags")
    var packTags: List<String>? = null,

    @field:JsonProperty("ratings")
    var ratings: List<Int>? = null,

    @field:JsonProperty("recent_favourites")
    var recentFavourites: List<User>? = null,

    @field:JsonProperty("related_users")
    var relatedUsers: List<User>? = null,

    @field:JsonProperty("user")
    var user: User? = null,

    @field:JsonProperty("track_id")
    var trackID: Int? = null,
) {
    // why?
    data class Description(val description: String)
    data class ShowInfo(
        val id: Int,
        val name: String,
    )
}

