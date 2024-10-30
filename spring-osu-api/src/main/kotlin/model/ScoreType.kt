package org.spring.osu.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer


enum class ScoreType(val type: String){
    BeatmapsetDiscussion("beatmapset_discussion"),
    BeatmapsetDiscussionPost("beatmapset_discussion_post"),
    Beatmapset("beatmapset"),
    Build("build"),
    Channel("channel"),
    Comment("comment"),
    ForumPost("forum_post"),
    ForumTopic("forum_topic"),
    LegacyMatchScore("legacy_match_score"),
    Message("message"),
    MultiplayerScoreLink("multiplayer_score_link"),
    NewsPost("news_post"),
    ScoreBestFruits("score_best_fruits"),
    ScoreBestMania("score_best_mania"),
    ScoreBestOsu("score_best_osu"),
    ScoreBestTaiko("score_best_taiko"),
    ScoreFruits("score_fruits"),
    ScoreMania("score_mania"),
    ScoreOsu("score_osu"),
    ScoreTaiko("score_taiko"),
    SoloScore("solo_score"),
    User("user"),;

    companion object {
        fun fromString(type: String) = when(type) {
            BeatmapsetDiscussion.type -> BeatmapsetDiscussion
            BeatmapsetDiscussionPost.type -> BeatmapsetDiscussionPost
            Beatmapset.type -> Beatmapset
            Build.type -> Build
            Channel.type -> Channel
            Comment.type -> Comment
            ForumPost.type -> ForumPost
            ForumTopic.type -> ForumTopic
            LegacyMatchScore.type -> LegacyMatchScore
            Message.type -> Message
            MultiplayerScoreLink.type -> MultiplayerScoreLink
            NewsPost.type -> NewsPost
            ScoreBestFruits.type -> ScoreBestFruits
            ScoreBestMania.type -> ScoreBestMania
            ScoreBestOsu.type -> ScoreBestOsu
            ScoreBestTaiko.type -> ScoreBestTaiko
            ScoreFruits.type -> ScoreFruits
            ScoreMania.type -> ScoreMania
            ScoreOsu.type -> ScoreOsu
            ScoreTaiko.type -> ScoreTaiko
            SoloScore.type -> SoloScore
            User.type -> User
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
    }

    internal class Serializer : StdSerializer<ScoreType>(ScoreType::class.java) {
        override fun serialize(value: ScoreType, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.type)
        }
    }

    internal class Deserializer : StdDeserializer<ScoreType>(ScoreType::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ScoreType {
            val node = p.text
            return ScoreType.fromString(node)
        }
    }
}