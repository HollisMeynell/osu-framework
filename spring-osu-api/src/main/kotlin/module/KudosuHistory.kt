package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.OffsetDateTime

data class KudosuHistory(
    @JsonProperty("id")
    var id: Int,

    @JsonProperty("action")
    @JsonSerialize(using = Action.Serializer::class)
    @JsonDeserialize(using = Action.Deserializer::class)
    var action: Action,

    @JsonProperty("amount")
    var amount: Int,

    @JsonProperty("model")
    var model: String,

    @JsonProperty("created_at")
    var createdAt: OffsetDateTime,

    @JsonProperty("giver")
    var giver: Giver?,

    @JsonProperty("post")
    var post: Post,
) {
    enum class Action(val action:String) {
        DenyKudosuReset("deny_kudosu.reset"),
        Give("give"),
        VoteGive("vote.give"),
        Reset("reset"),
        VoteReset("vote.reset"),
        Revoke("revoke"),
        VoteRevoke("vote.revoke"),;

        internal class Serializer : StdSerializer<Action>(Action::class.java) {
            override fun serialize(value: Action, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeString(value.action)
            }
        }

        internal class Deserializer : StdDeserializer<Action>(Action::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Action {
                return when (val action = p.text) {
                    DenyKudosuReset.action -> DenyKudosuReset
                    Give.action -> Give
                    VoteGive.action -> VoteGive
                    Reset.action -> Reset
                    VoteReset.action -> VoteReset
                    Revoke.action -> Revoke
                    VoteRevoke.action -> VoteRevoke
                    else -> throw IllegalArgumentException("Unknown action: $action")
                }
            }
        }
    }

    data class Giver(
        @JsonProperty("url")
        var url: String,

        @JsonProperty("username")
        var username: String,
    )

    data class Post(
        @JsonProperty("url")
        var url: String?,

        @JsonProperty("title")
        var title: String,
    )
}
