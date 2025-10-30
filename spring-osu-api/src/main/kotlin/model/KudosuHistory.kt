package org.spring.osu.model

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
    @field:JsonProperty("id")
    var id: Int,

    @field:JsonProperty("action")
    @field:JsonSerialize(using = Action.Serializer::class)
    @field:JsonDeserialize(using = Action.Deserializer::class)
    var action: Action,

    @field:JsonProperty("amount")
    var amount: Int,

    @field:JsonProperty("model")
    var model: String,

    @field:JsonProperty("created_at")
    var createdAt: OffsetDateTime,

    @field:JsonProperty("giver")
    var giver: Giver?,

    @field:JsonProperty("post")
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
        @field:JsonProperty("url")
        var url: String,

        @field:JsonProperty("username")
        var username: String,
    )

    data class Post(
        @field:JsonProperty("url")
        var url: String?,

        @field:JsonProperty("title")
        var title: String,
    )
}
