package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.ObjectNode
import org.spring.osu.RankStatus

data class Beatmapset(
    @JsonProperty("artist")
    var artist: String,

    @JsonProperty("artist_unicode")
    var artistUnicode: String,

    @JsonProperty("covers")
    var covers: Covers,

    @JsonProperty("creator")
    var creator: String,

    @JsonProperty("favourite_count")
    var favouriteCount: Int,

    @JsonProperty("id")
    var id: Int,

    @JsonProperty("nsfw")
    var nsfw: Boolean,

    @JsonProperty("offset")
    var offset: Int,

    @JsonProperty("play_count")
    var playCount: Int,

    @JsonProperty("preview_url")
    var previewUrl: String,

    @JsonProperty("source")
    var source: String,

    @JsonProperty("status")
    @JsonSerialize(using = RankStatus.RankStatusSerializer::class)
    @JsonDeserialize(using = RankStatus.RankStatusDeserializer::class)
    var status: RankStatus,

    @JsonProperty("spotlight")
    var spotlight: Boolean,

    @JsonProperty("title")
    var title: String,

    @JsonProperty("title_unicode")
    var titleUnicode: String,

    @JsonProperty("user_id")
    var userID: Int,

    @JsonProperty("video")
    var video: Boolean,

    // Optional fields
    @JsonProperty("beatmaps")
    @JsonIgnoreProperties("beatmapset")
    var beatmaps: List<Beatmap>? = null,

    @JsonProperty("converts")
    @JsonIgnoreProperties("beatmapset")
    var converts: List<Beatmap>? = null,

    @JsonProperty("current_nominations")
    var currentNominations: List<Nomination>? = null,

    @JsonProperty("current_user_attributes")
    var currentUserAttributes: Any? = null,

    @JsonProperty("description")
    var description: Description? = null,

    @JsonProperty("discussions")
    var discussions: Description? = null,

    @JsonProperty("genre")
    var genre: ShowInfo? = null,

    @JsonProperty("language")
    var language: ShowInfo? = null,

    @JsonProperty("nominations")
    var nominations: ObjectNode? = null,

    @JsonProperty("pack_tags")
    var packTags: List<String>? = null,

    @JsonProperty("ratings")
    var ratings: List<Int>? = null,

    @JsonProperty("recent_favourites")
    var recentFavourites: List<User>? = null,

    @JsonProperty("related_users")
    var relatedUsers: List<User>? = null,

    @JsonProperty("user")
    var user: User? = null,

    @JsonProperty("track_id")
    var trackID: Int? = null,
) {
    // why?
    data class Description(val description: String)
    data class ShowInfo(
        val id: Int,
        val name: String,
    )

}

