package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.spring.osu.OsuMod
import org.spring.osu.OsuMode
import java.time.OffsetDateTime

data class MatchEvent(
    @JsonProperty("id")
    val ID: Long,

    @JsonProperty("detail")
    val detail: MatchDetail,

    @JsonProperty("timestamp")
    val timestamp: OffsetDateTime,

    @JsonProperty("user_id")
    val userID: Long?,

    @JsonProperty("game")
    val game: MatchGame?,
) {
    @get:JsonIgnore
    val type: MatchEventType
        get() = detail.type

}

data class MatchGame(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("beatmap")
    var beatmap: Beatmap?,

    @JsonProperty("beatmap_id")
    val beatmapID: Long,

    @JsonProperty("start_time")
    val startTime: OffsetDateTime,

    @JsonProperty("end_time")
    val endTime: OffsetDateTime?,

    @JsonProperty("mode")
    @JsonSerialize(using = OsuMode.RulesetSerializer::class)
    @JsonDeserialize(using = OsuMode.RulesetDeserializer::class)
    val mode: OsuMode,

    @JsonProperty("mods")
    @JsonSerialize(using = OsuMod.OsuModsSerializer::class)
    @JsonDeserialize(using = OsuMod.OsuModsDeserializer::class)
    val mods: List<OsuMod>,

    @JsonProperty("scores")
    var scores: List<Score>?,

    @JsonProperty("scoring_type")
    @JsonSerialize(using = GameScoreType.Serializer::class)
    @JsonDeserialize(using = GameScoreType.Deserializer::class)
    val scoringType: GameScoreType,

    @JsonProperty("team_type")
    @JsonSerialize(using = GameTeamType.Serializer::class)
    @JsonDeserialize(using = GameTeamType.Deserializer::class)
    val teamType: GameTeamType,
) {
    @JsonIgnore
    val isTeamVS = teamType == GameTeamType.TeamVs || teamType == GameTeamType.TagTeamVs
}

data class MatchDetail(
    @JsonProperty("text")
    var text: String?,

    @JsonProperty("type")
    @JsonSerialize(using = MatchEventType.Serializer::class)
    @JsonDeserialize(using = MatchEventType.Deserializer::class)
    var type: MatchEventType,
)

enum class GameScoreType(val type: String) {
    Score("score"),
    Accuracy("accuracy"),
    Combo("combo"),
    ScoreV2("scorev2"),;

    internal class Serializer : StdSerializer<GameScoreType>(GameScoreType::class.java) {
        override fun serialize(value: GameScoreType, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.type)
        }
    }

    internal class Deserializer : StdDeserializer<GameScoreType>(GameScoreType::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GameScoreType {
            return when(p.text) {
                Score.type -> Score
                Accuracy.type -> Accuracy
                Combo.type -> Combo
                ScoreV2.type -> ScoreV2
                else -> throw IllegalArgumentException("Unknown GameScoreType: ${p.text}")
            }
        }
    }
}

enum class GameTeamType(val type: String) {
    HeadToHead("head-to-head"),
    TagCoop("tag-coop"),
    TeamVs("team-vs"),
    TagTeamVs("tag-team-vs"),;

    internal class Serializer : StdSerializer<GameTeamType>(GameTeamType::class.java) {
        override fun serialize(value: GameTeamType, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.type)
        }
    }

    internal class Deserializer : StdDeserializer<GameTeamType>(GameTeamType::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GameTeamType {
            return when(p.text) {
                HeadToHead.type -> HeadToHead
                TagCoop.type -> TagCoop
                TeamVs.type -> TeamVs
                TagTeamVs.type -> TagTeamVs
                else -> throw IllegalArgumentException("Unknown GameTeamType: ${p.text}")
            }
        }
    }
}

enum class MatchEventType(val type: String) {
    HostChanged("host-changed"),
    MatchCreated("match-created"),
    MatchDisbanded("match-disbanded"),
    PlayerJoined("player-joined"),
    PlayerKicked("player-kicked"),
    PlayerLeft("player-left"),
    Other("other"), ;

    companion object {
        fun fromType(type: String) = when (type) {
            HostChanged.type -> HostChanged
            MatchCreated.type -> MatchCreated
            MatchDisbanded.type -> MatchDisbanded
            PlayerJoined.type -> PlayerJoined
            PlayerKicked.type -> PlayerKicked
            PlayerLeft.type -> PlayerLeft
            else -> Other
        }
    }

    internal class Serializer : StdSerializer<MatchEventType>(MatchEventType::class.java) {
        override fun serialize(value: MatchEventType, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.type)
        }
    }

    internal class Deserializer : StdDeserializer<MatchEventType>(MatchEventType::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MatchEventType {
            return fromType(p.text)
        }
    }
}