package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import org.spring.osu.RankStatus
import java.time.OffsetDateTime

data class Beatmap(
    @field:JsonProperty("beatmapset_id")
    var beatmapsetID: Long,

    @field:JsonProperty("difficulty_rating")
    var difficultyRating: Float,

    @field:JsonProperty("id")
    var id: Long,

    @field:JsonProperty("mode")
    @field:JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @field:JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var mode: OsuMode,

    @field:JsonProperty("status")
    @field:JsonSerialize(using = RankStatus.RankStatusSerializer::class)
    @field:JsonDeserialize(using = RankStatus.RankStatusDeserializer::class)
    var status: RankStatus,

    @field:JsonProperty("total_length")
    var totalLength: Int,

    @field:JsonProperty("user_id")
    var userID: Long,

    @field:JsonProperty("version")
    var version: String,

    // Optional attributes

    @field:JsonProperty("beatmapset")
    @field:JsonSerialize(converter = BeatmapsetConverter::class)
    var beatmapset: Beatmapset? = null,

    @field:JsonProperty("checksum")
    var checksum: String? = null,

    @field:JsonProperty("failtimes")
    var failtimes: FailTimes? = null,

    @field:JsonProperty("max_combo")
    var maxCombo: Int? = null,

    @field:JsonProperty("accuracy")
    var accuracy: Float?,

    @field:JsonProperty("ar")
    var ar: Float?,

    @field:JsonProperty("bpm")
    var bpm: Float?,

    // Extended attributes

    @field:JsonProperty("convert")
    var convert: Boolean?,

    @field:JsonProperty("count_circles")
    var countCircles: Int?,

    @field:JsonProperty("count_sliders")
    var countSliders: Int?,

    @field:JsonProperty("count_spinners")
    var countSpinners: Int?,

    @field:JsonProperty("cs")
    var cs: Float?,

    @field:JsonProperty("deleted_at")
    var deletedAt: OffsetDateTime?,

    @field:JsonProperty("drain")
    var drain: Float?,

    @field:JsonProperty("hit_length")
    var hitLength: Int?,

    @field:JsonProperty("is_scoreable")
    var isScoreable: Boolean?,

    @field:JsonProperty("last_updated")
    var lastUpdated: OffsetDateTime?,

    @field:JsonProperty("mode_int")
    var modeInt: Int?,

    @field:JsonProperty("passcount")
    var passCount: Int?,

    @field:JsonProperty("playcount")
    var playCount: Int?,

    @field:JsonProperty("ranked")
    @field:JsonSerialize(using = RankStatus.RankStatusSerializer::class)
    @field:JsonDeserialize(using = RankStatus.RankStatusDeserializer::class)
    var ranked: RankStatus?,

    @field:JsonProperty("url")
    var url: String?,

    @field:JsonProperty("owners")
    var owners: List<Owner>?,
) {
    data class FailTimes(
        @field:JsonProperty("exit")
        var exit: List<Int>?,

        @field:JsonProperty("fail")
        var fail: List<Int>?,
    )

    data class Owner (
        @field:JsonProperty("id")
        var id: Long,

        @field:JsonProperty("username")
        var username: String,
    )
}