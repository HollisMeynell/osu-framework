package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMode
import java.time.OffsetDateTime

data class Score(
    @field:JsonProperty("accuracy")
    var accuracy: Double,

    @field:JsonProperty("best_id")
    var bestID: Long,

    @field:JsonProperty("score")
    var score: Long,

    @field:JsonProperty("created_at")
    var createdAt: OffsetDateTime,

    @field:JsonProperty("id")
    var id: Long,

    @field:JsonProperty("mode")
    @field:JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @field:JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var mode: OsuMode,

    @field:JsonProperty("max_combo")
    var maxCombo: Int,

    @field:JsonProperty("mods")
    @field:JsonSerialize(using = OsuMod.OsuModsSerializer::class)
    @field:JsonDeserialize(using = OsuMod.OsuModsDeserializer::class)
    var mods: List<OsuMod>,

    @field:JsonProperty("passed")
    var passed: Boolean,

    @field:JsonProperty("perfect")
    var perfect: Boolean,

    @field:JsonProperty("pp")
    var pp: Double?,

    @field:JsonProperty("rank")
    var rank: String,

    @field:JsonProperty("statistics")
    var statistics: ScoreStatistics,

    /**
     * from https://github.com/ppy/osu-web/blob/master/app/Libraries/MorphMap.php#L50
     */
    @field:JsonProperty("type")
    @field:JsonSerialize(using = ScoreType.Serializer::class)
    @field:JsonDeserialize(using = ScoreType.Deserializer::class)
    var type: ScoreType,

    @field:JsonProperty("replay")
    var replay: Boolean,

    @field:JsonProperty("user_id")
    var userID: Int,

    //Optional attributes
    @field:JsonProperty("beatmap")
    @field:JsonSerialize(converter = BeatmapConverter::class)
    var beatmap: Beatmap?,

    @field:JsonProperty("beatmapset")
    @field:JsonSerialize(converter = BeatmapsetConverter::class)
    var beatmapset: Beatmapset?,

    @field:JsonProperty("user")
    var user: User?,

    @field:JsonProperty("weight")
    var weight: Weight?,
    
    @field:JsonProperty("match")
    var match: Match.ScoreInfo?,
) {
    data class Weight(
        @field:JsonProperty("percentage")
        var percentage: Float,

        @field:JsonProperty("pp")
        var pp: Float
    )
}
