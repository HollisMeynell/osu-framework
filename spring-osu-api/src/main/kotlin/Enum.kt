@file:Suppress("unused")

package org.spring.osu

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.spring.core.Json
import org.spring.core.jsonList
import java.util.*

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

enum class UserScoreType(val type:String) {
    Best("best"), First("firsts"), Recent("recent")
}

enum class UserBeatmapType(val type: String) {
    Favourite("favourite"),
    Graveyard("graveyard"),
    Guest("guest"),
    Loved("loved"),
    MostPlayed("most_played"),
    Nominated("nominated"),
    Pending("pending"),
    Ranked("ranked"),;
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
            if (value != null) generator.writeString(value.name)
        }
    }

    class RulesetDeserializer : JsonDeserializer<OsuMode?>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OsuMode {
            return OsuMode.getMode(p.text)
        }
    }
}

enum class OsuMod(val value: Int, val mod: String) {
    None(0, ""),

    NoFail(1, "NF"),

    Easy(1 shl 1, "EZ"),

    TouchDevice(1 shl 2, "TD"),

    Hidden(1 shl 3, "HD"),

    HardRock(1 shl 4, "HR"),

    SuddenDeath(1 shl 5, "SD"),

    DoubleTime(1 shl 6, "DT"),

    Relax(1 shl 7, "RX"),

    HalfTime(1 shl 8, "HT"),

    // with dt: 512 + 64 = 576
    Nightcore((1 shl 9) + (DoubleTime.value), "NC"),

    Flashlight(1 shl 10, "FL"),

    Autoplay(1 shl 11, "AT"),

    SpunOut(1 shl 12, "SO"),

    Autopilot(1 shl 13, "AP"),

    Perfect(1 shl 14, "PF"),

    Key4(1 shl 15, "4K"),

    Key5(1 shl 16, "5K"),

    Key6(1 shl 17, "6K"),

    Key7(1 shl 18, "7K"),

    Key8(1 shl 19, "8K"),

    FadeIn(1 shl 20, "FI"),

    Random(1 shl 21, "RD"),

    Cinema(1 shl 22, "CM"),

    // osu!cuttingedge
    TargetPractice(1 shl 23, "TP"),

    Key9(1 shl 24, "9K"),

    KeyCoop(1 shl 25, "CP"),

    Key1(1 shl 26, "1K"),

    Key3(1 shl 27, "3K"),

    Key2(1 shl 28, "2K"),

    ScoreV2(1 shl 29, "V2"),

    Mirror(1 shl 30, "MR"),

    // keyMod(Key1.value | Key2.value | Key3.value | Key4.value | Key5.value | Key6.value | Key7.value | Key8.value | Key9.value | KeyCoop.value),
    keyMod(521109504, "KEY"),

    // FreeModAllowed(NoFail.value | Easy.value | Hidden.value | HardRock.value | SuddenDeath.value | Flashlight.value | FadeIn.value | Relax.value | Autopilot.value | SpunOut.value | keyMod.value),
    FreeMod(522171579, "FM"),

    // ScoreIncreaseMods(Hidden.value | HardRock.value | Flashlight.value | DoubleTime.value | FadeIn.value)
    ScoreIncreaseMods(1049688, "IM"),

    Other(-1, "OTHER"),
    ;

    operator fun plus(mod: OsuMod): Int {
        return this.value or mod.value
    }

    operator fun plus(modValue: Int): Int {
        return this.value or modValue
    }

    companion object {
        private val changeRatingValue: Int = Easy.value or HalfTime.value or TouchDevice.value or
                HardRock.value or DoubleTime.value or Nightcore.value or Flashlight.value
        private val emptyReg by lazy { "\\s+".toRegex() }
        private val splitReg by lazy { "(?<=\\w)(?=(\\w{2})+$)".toRegex() }

        fun getMods(value: Int): List<OsuMod> {
            return entries.filter { it.value and value != it.value }
        }

        fun getMod(value: Int): OsuMod? {
            return entries.firstOrNull { it.value and value == it.value }
        }

        fun getMod(mod: String): OsuMod {
            val modName = mod.trim().uppercase()
            return when (modName) {
                None.mod -> None
                NoFail.mod -> NoFail
                Easy.mod -> Easy
                TouchDevice.mod -> TouchDevice
                Hidden.mod -> Hidden
                HardRock.mod -> HardRock
                SuddenDeath.mod -> SuddenDeath
                DoubleTime.mod -> DoubleTime
                Relax.mod -> Relax
                HalfTime.mod -> HalfTime
                Nightcore.mod -> Nightcore
                Flashlight.mod -> Flashlight
                Autoplay.mod -> Autoplay
                SpunOut.mod -> SpunOut
                Autopilot.mod -> Autopilot
                Perfect.mod -> Perfect
                Key4.mod -> Key4
                Key5.mod -> Key5
                Key6.mod -> Key6
                Key7.mod -> Key7
                Key8.mod -> Key8
                FadeIn.mod -> FadeIn
                Random.mod -> Random
                Cinema.mod -> Cinema
                TargetPractice.mod -> TargetPractice
                Key9.mod -> Key9
                KeyCoop.mod -> KeyCoop
                Key1.mod -> Key1
                Key3.mod -> Key3
                Key2.mod -> Key2
                ScoreV2.mod -> ScoreV2
                Mirror.mod -> Mirror
                keyMod.mod -> keyMod
                FreeMod.mod -> FreeMod
                ScoreIncreaseMods.mod -> ScoreIncreaseMods
                else -> Other
            }
        }

        fun getMods(mods: List<String>) = mods.map { getMod(it) }.filter { it != Other }

        fun getMods(mods: String): List<OsuMod> {
            if (mods.isBlank()) return emptyList()
            val modsStr = mods.uppercase(Locale.getDefault()).replace(emptyReg, "")
            if (modsStr.length % 2 != 0) throw IllegalArgumentException("Invalid mods input: $mods")
            val modStrList = modsStr.split(splitReg).filter { it.isEmpty().not() }
            return getMods(modStrList)
        }

        fun hasChangeRating(value: Int) = changeRatingValue and value != 0

        private val ModListType = Json.typeFactory.constructCollectionType(List::class.java, OsuMod::class.java)
    }

    internal class OsuModSerializer :
        StdSerializer<OsuMod>(ModListType) {
        override fun serialize(value: OsuMod?, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value?.mod)
        }
    }

    internal class OsuModDeserializer :
        StdDeserializer<OsuMod>(ModListType) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OsuMod {
            val mods = p.readValueAs(String::class.java)
            return getMod(mods)
        }
    }

    internal class OsuModsSerializer :
        StdSerializer<List<OsuMod>>(ModListType) {
        override fun serialize(value: List<OsuMod>?, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeStartArray()
            value?.forEach { gen.writeString(it.mod) }
            gen.writeEndArray()
        }
    }

    internal class OsuModsDeserializer :
        StdDeserializer<List<OsuMod>>(ModListType) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<OsuMod> {
            val mods = p.readValueAsTree<JsonNode>().jsonList<String>()
            return getMods(mods)
        }
    }
}

operator fun Int.plus(mod: OsuMod): Int {
    return this or mod.value
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