package org.spring.osu.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.spring.core.Json
import org.spring.core.jsonList
import java.util.*

enum class OsuMod(val value: Int, val acronym: String) {
    None(0, "NM"),

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
    KeyMod(521109504, "KEY"),

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
        private val changeSpeedValue: Int = DoubleTime.value or Nightcore.value or HalfTime.value
        private val emptyReg by lazy { "\\s+".toRegex() }
        private val splitReg by lazy { "(?<=\\w)(?=(\\w{2})+$)".toRegex() }

        fun getAllMod(value: Int): List<OsuMod> {
            return entries.subList(1, entries.size - 1).filter { it.value and value == it.value }
        }

        fun getMod(value: Int): OsuMod? {
            return entries.firstOrNull { it.value and value == it.value }
        }

        fun getMod(mod: String): OsuMod {
            val modName = mod.trim().uppercase()
            return when (modName) {
                None.acronym -> None
                NoFail.acronym -> NoFail
                Easy.acronym -> Easy
                TouchDevice.acronym -> TouchDevice
                Hidden.acronym -> Hidden
                HardRock.acronym -> HardRock
                SuddenDeath.acronym -> SuddenDeath
                DoubleTime.acronym -> DoubleTime
                Relax.acronym -> Relax
                HalfTime.acronym -> HalfTime
                Nightcore.acronym -> Nightcore
                Flashlight.acronym -> Flashlight
                Autoplay.acronym -> Autoplay
                SpunOut.acronym -> SpunOut
                Autopilot.acronym -> Autopilot
                Perfect.acronym -> Perfect
                Key4.acronym -> Key4
                Key5.acronym -> Key5
                Key6.acronym -> Key6
                Key7.acronym -> Key7
                Key8.acronym -> Key8
                FadeIn.acronym -> FadeIn
                Random.acronym -> Random
                Cinema.acronym -> Cinema
                TargetPractice.acronym -> TargetPractice
                Key9.acronym -> Key9
                KeyCoop.acronym -> KeyCoop
                Key1.acronym -> Key1
                Key3.acronym -> Key3
                Key2.acronym -> Key2
                ScoreV2.acronym -> ScoreV2
                Mirror.acronym -> Mirror
                KeyMod.acronym -> KeyMod
                FreeMod.acronym -> FreeMod
                ScoreIncreaseMods.acronym -> ScoreIncreaseMods
                else -> Other
            }
        }

        fun getModValue(mod: String): Int {
            return getMod(mod).value
        }

        fun getAllMod(mods: List<String>) = mods.map { getMod(it) }.filter { it != Other }

        fun getAllModValue(mods: List<String>): Int {
            return getAllMod(mods).sumOf { it.value }
        }

        fun getAllMod(mods: String): List<OsuMod> {
            if (mods.isBlank()) return emptyList()
            val modsStr = mods.uppercase(Locale.getDefault()).replace(emptyReg, "")
            if (modsStr.length % 2 != 0) throw IllegalArgumentException("Invalid mods input: $mods")
            val modStrList = modsStr.split(splitReg).filter { it.isEmpty().not() }
            return getAllMod(modStrList)
        }

        fun getAllModValue(mods: String): Int {
            return getAllMod(mods).sumOf { it.value }
        }

        fun hasChangeRating(value: Int) = changeRatingValue and value != 0

        fun hasChangeRating(mods: List<OsuMod>) = mods.any { it.value and changeRatingValue != 0 }

        fun changeAR(
            value: Float,
            dt: Boolean = false,
            hr: Boolean = false,
            ht: Boolean = false,
            ez: Boolean = false,
        ): Float {
            var ar: Float

            ar = when {
                hr -> (value * 1.4f).clamp()
                ez -> (value * 0.5f).clamp()
                else -> value
            }

            if (dt || ht) {
                var ms = when {
                    ar > 11f -> 300f
                    ar > 5f -> 1200 - (150 * (ar - 5))
                    ar > 0f -> 1800 - (120 * ar)
                    else -> 1800f
                }

                ms /= if (dt) 1.5f
                else 0.75f

                ar = when {
                    ms < 300 -> 11f
                    ms < 1200 -> 5 + (1200 - ms) / 150f
                    ms < 2400 -> (1800 - ms) / 120f
                    else -> -5f
                }
            }
            return ar
        }

        fun changeOD(
            value: Float,
            dt: Boolean = false,
            hr: Boolean = false,
            ht: Boolean = false,
            ez: Boolean = false,
        ): Float {
            var od: Float
            od = when {
                hr -> (value * 1.4f).clamp()
                ez -> (value * 0.5f).clamp()
                else -> value
            }

            if (dt || ht) {
                var ms = if (od < 11) {
                    80 - od * 6
                } else {
                    14f
                }

                ms /= if (dt) 1.5f
                else 0.75f

                od = if (ms < 14) {
                    (80 - ms) / 6
                } else {
                    11f
                }
            }
            return od
        }

        fun changeCS(value: Float, isHR: Boolean): Float =
            if (isHR) {
                value * 1.3f
            } else {
                value / 2
            }.clamp()

        fun changeHP(value: Float, isHR: Boolean): Float =
            if (isHR) {
                value * 1.4f
            } else {
                value / 2
            }.clamp()

        fun changeBPM(value: Float, isDT: Boolean): Float = if (isDT) value * 1.5f else value * 0.75f

        private fun Float.clamp() = if ((0f..10f).contains(this)) {
            this
        } else if (this > 10f) {
            10f
        } else {
            0f
        }

        private val ModListType = Json.typeFactory.constructCollectionType(List::class.java, OsuMod::class.java)
    }

    internal class OsuModSerializer :
        StdSerializer<OsuMod>(ModListType) {
        override fun serialize(value: OsuMod?, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value?.acronym)
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
            value?.forEach { gen.writeString(it.acronym) }
            gen.writeEndArray()
        }
    }

    internal class OsuModsDeserializer :
        StdDeserializer<List<OsuMod>>(ModListType) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<OsuMod> {
            val mods = p.readValueAsTree<JsonNode>().jsonList<String>()
            return getAllMod(mods)
        }
    }
}

operator fun Int.plus(mod: OsuMod): Int {
    return this or mod.value
}