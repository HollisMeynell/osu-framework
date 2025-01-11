@file:Suppress("unused")

package org.spring.osu

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

enum class AuthScope(val scope: String) {
    /**
     * Allows read chat messages on a user's behalf.
     */
    ChatRead("chat.read"),

    /**
     * Allows sending chat messages on a user's behalf.
     */
    ChatWrite("chat.write"),

    /**
     * Allows joining and leaving chat channels on a user's behalf.
     */
    ChatWriteManage("chat.write_manage"),

    /**
     * Allows acting as the owner of a client; only available for Client Credentials Grant.
     * not used
     */
    Delegate("delegate"),

    /**
     * Allows creating and editing forum posts on a user's behalf.
     */
    ForumWrite("forum.write"),

    /**
     * Allows reading of the user's friend list.
     */
    FriendsRead("friends.read"),

    /**
     * Allows reading of the public profile of the user (/me).
     */
    Identify("identify"),

    /**
     * Allows reading of publicly available data on behalf of the user.
     */
    Public("public"),
    ;

    override fun toString(): String = scope
}

enum class UserScoreType(val type: String) {
    /**
     * The user's best scores.(bp)
     */
    Best("best"),

    /**
     * The user's first scores.
     */
    First("firsts"),

    /**
     * The user's recent scores.(24h)
     */
    Recent("recent"),
}

enum class UserBeatmapType(val type: String) {
    Favourite("favourite"),
    Graveyard("graveyard"),
    Guest("guest"),
    Loved("loved"),
    MostPlayed("most_played"),
    Nominated("nominated"),
    Pending("pending"),
    Ranked("ranked"), ;
}

enum class RankStatus(val value: Int, val status: String) {
    Graveyard(-2, "graveyard"),
    Wip(-1, "wip"),
    Pending(0, "pending"),
    Ranked(1, "ranked"),
    Approved(2, "approved"),
    Qualified(3, "qualified"),
    Loved(4, "loved"),
    Unknown(-3, "unknown"),
    ;

    companion object {
        fun getStatus(value: Int) = when (value) {
            -2 -> Graveyard
            -1 -> Wip
            0 -> Pending
            1 -> Ranked
            2 -> Approved
            3 -> Qualified
            4 -> Loved
            else -> Unknown
        }

        fun getStatus(status: String) = when (status.lowercase()) {
            "graveyard" -> Graveyard
            "wip" -> Wip
            "pending" -> Pending
            "ranked" -> Ranked
            "approved" -> Approved
            "qualified" -> Qualified
            "loved" -> Loved
            else -> Unknown
        }
    }

    internal class RankStatusSerializer : JsonSerializer<RankStatus?>() {
        private fun JsonGenerator.getKey(): String = outputContext.currentName
        override fun serialize(value: RankStatus?, generator: JsonGenerator, provider: SerializerProvider) {
            val key = generator.getKey()
            if (key == "status") generator.writeString(value!!.status)
            else generator.writeNumber(value!!.value)
        }
    }

    internal class RankStatusDeserializer : JsonDeserializer<RankStatus?>() {
        private fun JsonParser.getKey(): String = currentName()
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): RankStatus {
            return if (p.isNaN) RankStatus.getStatus(p.text)
            else RankStatus.getStatus(p.valueAsInt)
        }
    }
}

typealias OsuRuleset = OsuMode

enum class OsuMode(val value: Int, val describe: String) {
    Osu(0, "osu"),
    Taiko(1, "taiko"),
    Catch(2, "fruits"),
    Mania(3, "mania"),
    Default(-1, ""),
    ;

    fun isDefault() = this == Default

    companion object {
        fun getMode(value: Int) = when (value) {
            0 -> Osu
            1 -> Taiko
            2 -> Catch
            3 -> Mania
            else -> Default
        }

        fun getMode(name: String?): OsuMode {
            if (name == null) return Default
            return when (name.lowercase()) {
                "osu", "o", "0" -> Osu
                "taiko", "t", "1" -> Taiko
                "catch", "c", "fruits", "f", "2" -> Catch
                "mania", "m", "3" -> Mania
                else -> Default
            }
        }

    }

    override fun toString(): String = this.describe

    class RulesetSerializer : JsonSerializer<OsuMode?>() {
        override fun serialize(value: OsuMode?, generator: JsonGenerator, provider: SerializerProvider) {
            if (value != null) generator.writeString(value.describe)
        }
    }

    class RulesetDeserializer : JsonDeserializer<OsuMode?>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OsuMode {
            return OsuMode.getMode(p.text)
        }
    }
}

enum class BeatmapPackType(
    val type: String,
    val tag: Char,
    val describe: String,
) {
    Standard("standard", 'S', "Standard"),
    Featured("featured", 'F', "Featured Artist"),
    Tournament("tournament", 'P', "Tournament"),
    Loved("loved", 'L', "Project Loved"),
    Chart("chart", 'R', "Spotlights"),
    Theme("theme", 'T', "Theme"),
    Artist("artist", 'A', "Artist/Album"),
    ;

    internal class BeatmapPackTypeSerializer : JsonSerializer<BeatmapPackType?>() {
        override fun serialize(value: BeatmapPackType?, generator: JsonGenerator, provider: SerializerProvider) {
            if (value != null) generator.writeString(value.type)
        }
    }

    internal class BeatmapPackTypeDeserializer : JsonDeserializer<BeatmapPackType?>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BeatmapPackType {
            return Standard
        }
    }
}