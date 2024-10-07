package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import org.spring.osu.RankStatus
import java.time.OffsetDateTime

data class Beatmap(
    @JsonProperty("beatmapset_id")
    var beatmapsetID: Long,

    @JsonProperty("difficulty_rating")
    var difficultyRating: Float,

    @JsonProperty("id")
    var id: Long,

    @JsonProperty("mode")
    @JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var mode: OsuMode,

    @JsonProperty("status")
    @JsonSerialize(using = RankStatus.RankStatusSerializer::class)
    @JsonDeserialize(using = RankStatus.RankStatusDeserializer::class)
    var status: RankStatus,

    @JsonProperty("total_length")
    var totalLength: Int,

    @JsonProperty("user_id")
    var userID: Long,

    @JsonProperty("version")
    var version: String,

    // Optional attributes

    @JsonProperty("beatmapset")
    @JsonSerialize(converter = BeatmapsetConverter::class)
    var beatmapset: Beatmapset? = null,

    @JsonProperty("checksum")
    var checksum: String? = null,

    @JsonProperty("failtimes")
    var failtimes: FailTimes? = null,

    @JsonProperty("max_combo")
    var maxCombo: Int? = null,

    @JsonProperty("accuracy")
    var accuracy: Float,

    @JsonProperty("ar")
    var ar: Float,

    @JsonProperty("bpm")
    var bpm: Float?,

    // Extended attributes

    @JsonProperty("convert")
    var convert: Boolean?,

    @JsonProperty("count_circles")
    var countCircles: Int?,

    @JsonProperty("count_sliders")
    var countSliders: Int?,

    @JsonProperty("count_spinners")
    var countSpinners: Int?,

    @JsonProperty("cs")
    var cs: Float?,

    @JsonProperty("deleted_at")
    var deletedAt: OffsetDateTime?,

    @JsonProperty("drain")
    var drain: Float?,

    @JsonProperty("hit_length")
    var hitLength: Int?,

    @JsonProperty("is_scoreable")
    var isScoreable: Boolean?,

    @JsonProperty("last_updated")
    var lastUpdated: OffsetDateTime?,

    @JsonProperty("mode_int")
    var modeInt: Int?,

    @JsonProperty("passcount")
    var passCount: Int?,

    @JsonProperty("playcount")
    var playCount: Int?,

    @JsonProperty("ranked")
    @JsonSerialize(using = RankStatus.RankStatusSerializer::class)
    @JsonDeserialize(using = RankStatus.RankStatusDeserializer::class)
    var ranked: RankStatus?,

    @JsonProperty("url")
    var url: String?,
) {
    data class FailTimes(
        @JsonProperty("exit")
        var exit: List<Int>?,

        @JsonProperty("fail")
        var fail: List<Int>?,
    )
}