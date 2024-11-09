package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.spring.osu.OsuMod
import org.spring.osu.OsuMode
import java.time.OffsetDateTime

data class Score(
    @JsonProperty("accuracy")
    var accuracy: Double,

    @JsonProperty("best_id")
    var bestID: Long,

    @JsonProperty("score")
    var score: Long,

    @JsonProperty("created_at")
    var createdAt: OffsetDateTime,

    @JsonProperty("id")
    var id: Long,

    @JsonProperty("mode")
    @JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    var mode: OsuMode,

    @JsonProperty("max_combo")
    var maxCombo: Int,

    @JsonProperty("mods")
    @JsonSerialize(using = OsuMod.OsuModsSerializer::class)
    @JsonDeserialize(using = OsuMod.OsuModsDeserializer::class)
    var mods: List<OsuMod>,

    @JsonProperty("passed")
    var passed: Boolean,

    @JsonProperty("perfect")
    var perfect: Boolean,

    @JsonProperty("pp")
    var pp: Double?,

    @JsonProperty("rank")
    var rank: String,

    @JsonProperty("statistics")
    var statistics: ScoreStatistics,

    /**
     * from https://github.com/ppy/osu-web/blob/master/app/Libraries/MorphMap.php#L50
     */
    @JsonProperty("type")
    @JsonSerialize(using = ScoreType.Serializer::class)
    @JsonDeserialize(using = ScoreType.Deserializer::class)
    var type: ScoreType,

    @JsonProperty("replay")
    var replay: Boolean,

    @JsonProperty("user_id")
    var userID: Int,

    //Optional attributes
    @JsonProperty("beatmap")
    @JsonSerialize(converter = BeatmapConverter::class)
    var beatmap: Beatmap?,

    @JsonProperty("beatmapset")
    @JsonSerialize(converter = BeatmapsetConverter::class)
    var beatmapset: Beatmapset?,

    @JsonProperty("user")
    var user: User?,

    @JsonProperty("weight")
    var weight: Weight?,
    
    @JsonProperty("match")
    var match: Match.ScoreInfo?,
) {
    data class Weight(
        @JsonProperty("percentage")
        var percentage: Float,

        @JsonProperty("pp")
        var pp: Float
    )
}
