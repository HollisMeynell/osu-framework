@file:Suppress("unused")

package org.spring.osu.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import org.spring.core.json
import org.spring.core.toJson
import org.spring.osu.OsuMode

sealed interface Mod {
    val acronym: String
    val mode: Set<OsuMode>
    @get:JsonIgnore
    val incompatible: Set<Mod>
}

sealed interface ValueMod {
    val value: Int

    operator fun plus(other: ValueMod): Int {
        return value or other.value
    }

    operator fun Int.plus(other: ValueMod): Int {
        return this or other.value
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "acronym")
@JsonSubTypes(
    JsonSubTypes.Type(value = LazerMod.Easy::class, name = "EZ"),
    JsonSubTypes.Type(value = LazerMod.NoFail::class, name = "NF"),
    JsonSubTypes.Type(value = LazerMod.HalfTime::class, name = "HT"),
    JsonSubTypes.Type(value = LazerMod.Daycore::class, name = "DC"),
    JsonSubTypes.Type(value = LazerMod.HardRock::class, name = "HR"),
    JsonSubTypes.Type(value = LazerMod.SuddenDeath::class, name = "SD"),
    JsonSubTypes.Type(value = LazerMod.Perfect::class, name = "PF"),
    JsonSubTypes.Type(value = LazerMod.DoubleTime::class, name = "DT"),
    JsonSubTypes.Type(value = LazerMod.Nightcore::class, name = "NC"),
    JsonSubTypes.Type(value = LazerMod.Hidden::class, name = "HD"),
    JsonSubTypes.Type(value = LazerMod.Flashlight::class, name = "FL"),
    JsonSubTypes.Type(value = LazerMod.Blinds::class, name = "BL"),
    JsonSubTypes.Type(value = LazerMod.StrictTracking::class, name = "ST"),
    JsonSubTypes.Type(value = LazerMod.AccuracyChallenge::class, name = "AC"),
    JsonSubTypes.Type(value = LazerMod.TargetPractice::class, name = "TP"),
    JsonSubTypes.Type(value = LazerMod.DifficultyAdjust::class, name = "DA"),
    JsonSubTypes.Type(value = LazerMod.Classic::class, name = "CL"),
    JsonSubTypes.Type(value = LazerMod.Random::class, name = "RD"),
    JsonSubTypes.Type(value = LazerMod.Mirror::class, name = "MR"),
    JsonSubTypes.Type(value = LazerMod.Alternate::class, name = "AL"),
    JsonSubTypes.Type(value = LazerMod.SingleTap::class, name = "SG"),
    JsonSubTypes.Type(value = LazerMod.Autoplay::class, name = "AT"),
    JsonSubTypes.Type(value = LazerMod.Cinema::class, name = "CN"),
    JsonSubTypes.Type(value = LazerMod.Relax::class, name = "RX"),
    JsonSubTypes.Type(value = LazerMod.Autopilot::class, name = "AP"),
    JsonSubTypes.Type(value = LazerMod.SpunOut::class, name = "SO"),
    JsonSubTypes.Type(value = LazerMod.Transform::class, name = "TR"),
    JsonSubTypes.Type(value = LazerMod.Wiggle::class, name = "WG"),
    JsonSubTypes.Type(value = LazerMod.SpinIn::class, name = "SI"),
    JsonSubTypes.Type(value = LazerMod.Grow::class, name = "GR"),
    JsonSubTypes.Type(value = LazerMod.Deflate::class, name = "DF"),
    JsonSubTypes.Type(value = LazerMod.WindUp::class, name = "WU"),
    JsonSubTypes.Type(value = LazerMod.WindDown::class, name = "WD"),
    JsonSubTypes.Type(value = LazerMod.Traceable::class, name = "TC"),
    JsonSubTypes.Type(value = LazerMod.BarrelRoll::class, name = "BR"),
    JsonSubTypes.Type(value = LazerMod.ApproachDifferent::class, name = "AD"),
    JsonSubTypes.Type(value = LazerMod.Muted::class, name = "MU"),
    JsonSubTypes.Type(value = LazerMod.NoScope::class, name = "NS"),
    JsonSubTypes.Type(value = LazerMod.Magnetised::class, name = "MG"),
    JsonSubTypes.Type(value = LazerMod.Repel::class, name = "RP"),
    JsonSubTypes.Type(value = LazerMod.AdaptiveSpeed::class, name = "AS"),
    JsonSubTypes.Type(value = LazerMod.FreezeFrame::class, name = "FR"),
    JsonSubTypes.Type(value = LazerMod.Bubbles::class, name = "BU"),
    JsonSubTypes.Type(value = LazerMod.Synesthesia::class, name = "SY"),
    JsonSubTypes.Type(value = LazerMod.Depth::class, name = "DP"),
    JsonSubTypes.Type(value = LazerMod.TouchDevice::class, name = "TD"),
    JsonSubTypes.Type(value = LazerMod.ScoreV2::class, name = "SV2"),
    JsonSubTypes.Type(value = LazerMod.Swap::class, name = "SW"),
    JsonSubTypes.Type(value = LazerMod.ConstantSpeed::class, name = "CS"),
    JsonSubTypes.Type(value = LazerMod.FloatingFruits::class, name = "FF"),
    JsonSubTypes.Type(value = LazerMod.NoRelease::class, name = "NR"),
    JsonSubTypes.Type(value = LazerMod.FadeIn::class, name = "FI"),
    JsonSubTypes.Type(value = LazerMod.Cover::class, name = "CO"),
    JsonSubTypes.Type(value = LazerMod.DualStages::class, name = "DS"),
    JsonSubTypes.Type(value = LazerMod.Invert::class, name = "IN"),
    JsonSubTypes.Type(value = LazerMod.HoldOff::class, name = "HO"),
    JsonSubTypes.Type(value = LazerMod.Key1::class, name = "1K"),
    JsonSubTypes.Type(value = LazerMod.Key2::class, name = "2K"),
    JsonSubTypes.Type(value = LazerMod.Key3::class, name = "3K"),
    JsonSubTypes.Type(value = LazerMod.Key4::class, name = "4K"),
    JsonSubTypes.Type(value = LazerMod.Key5::class, name = "5K"),
    JsonSubTypes.Type(value = LazerMod.Key6::class, name = "6K"),
    JsonSubTypes.Type(value = LazerMod.Key7::class, name = "7K"),
    JsonSubTypes.Type(value = LazerMod.Key8::class, name = "8K"),
    JsonSubTypes.Type(value = LazerMod.Key9::class, name = "9K"),
    JsonSubTypes.Type(value = LazerMod.Key10::class, name = "10K"),
)
sealed class LazerMod {
    @get:JsonProperty("acronym")
    abstract val type: String

    @get:JsonProperty("settings")
    abstract val settings: Any?


    class Easy(
        retries: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(retries)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var retries: Float?
            get() = (settings as Value).retries
            set(v) {
                (settings as Value).retries = v
            }

        private data class Value(
            @JsonProperty("retries") var retries: Float? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "EZ"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(HardRock, AccuracyChallenge, DifficultyAdjust)
            override val value: Int = 2
        }
    }

    class NoFail : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "NF"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(SuddenDeath, Perfect, AccuracyChallenge, Cinema)
            override val value: Int = 1
        }
    }

    class HalfTime(
        speedChange: Float? = null,
        adjustPitch: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(speedChange, adjustPitch)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var speedChange: Float?
            get() = (settings as Value).speedChange
            set(v) {
                (settings as Value).speedChange = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var adjustPitch: Boolean?
            get() = (settings as Value).adjustPitch
            set(v) {
                (settings as Value).adjustPitch = v
            }

        private data class Value(
            @JsonProperty("speed_change") var speedChange: Float? = null,
            @JsonProperty("adjust_pitch") var adjustPitch: Boolean? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "HT"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> =
                setOf(HalfTime, Daycore, DoubleTime, Nightcore, WindUp, WindDown, AdaptiveSpeed)
            override val value: Int = 256
        }
    }

    class Daycore(
        speedChange: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(speedChange)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var speedChange: Float?
            get() = (settings as Value).speedChange
            set(v) {
                (settings as Value).speedChange = v
            }

        private data class Value(
            @JsonProperty("speed_change") var speedChange: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "DC"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> =
                setOf(HalfTime, Daycore, DoubleTime, Nightcore, WindUp, WindDown, AdaptiveSpeed)

        }
    }

    class HardRock : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "HR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Easy, DifficultyAdjust, Mirror)
            override val value: Int = 16
        }
    }

    class SuddenDeath(
        restart: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(restart)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var restart: Boolean?
            get() = (settings as Value).restart
            set(v) {
                (settings as Value).restart = v
            }

        private data class Value(
            @JsonProperty("restart") var restart: Boolean? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "SD"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(NoFail, Perfect, TargetPractice, Cinema)
            override val value: Int = 32
        }
    }

    class Perfect(
        restart: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(restart)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var restart: Boolean?
            get() = (settings as Value).restart
            set(v) {
                (settings as Value).restart = v
            }

        private data class Value(
            @JsonProperty("restart") var restart: Boolean? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "PF"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(NoFail, SuddenDeath, AccuracyChallenge, Cinema)
            override val value: Int = 16384
        }
    }

    class DoubleTime(
        speedChange: Float? = null,
        adjustPitch: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(speedChange, adjustPitch)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var speedChange: Float?
            get() = (settings as Value).speedChange
            set(v) {
                (settings as Value).speedChange = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var adjustPitch: Boolean?
            get() = (settings as Value).adjustPitch
            set(v) {
                (settings as Value).adjustPitch = v
            }

        private data class Value(
            @JsonProperty("speed_change") var speedChange: Float? = null,
            @JsonProperty("adjust_pitch") var adjustPitch: Boolean? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "DT"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> =
                setOf(HalfTime, Daycore, DoubleTime, Nightcore, WindUp, WindDown, AdaptiveSpeed)
            override val value: Int = 64
        }
    }

    class Nightcore(
        speedChange: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(speedChange)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var speedChange: Float?
            get() = (settings as Value).speedChange
            set(v) {
                (settings as Value).speedChange = v
            }

        private data class Value(
            @JsonProperty("speed_change") var speedChange: Float? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "NC"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> =
                setOf(HalfTime, Daycore, DoubleTime, Nightcore, WindUp, WindDown, AdaptiveSpeed)
            override val value: Int = 512
        }
    }

    class Hidden(
        onlyFadeApproachCircles: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(onlyFadeApproachCircles)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var onlyFadeApproachCircles: Boolean?
            get() = (settings as Value).onlyFadeApproachCircles
            set(v) {
                (settings as Value).onlyFadeApproachCircles = v
            }

        private data class Value(
            @JsonProperty("only_fade_approach_circles") var onlyFadeApproachCircles: Boolean? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "HD"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> =
                setOf(SpinIn, Traceable, ApproachDifferent, Depth, FadeIn, Cover, Flashlight)
            override val value: Int = 8
        }
    }

    class Flashlight(
        followDelay: Float? = null,
        sizeMultiplier: Float? = null,
        comboBasedSize: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(followDelay, sizeMultiplier, comboBasedSize)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var followDelay: Float?
            get() = (settings as Value).followDelay
            set(v) {
                (settings as Value).followDelay = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var sizeMultiplier: Float?
            get() = (settings as Value).sizeMultiplier
            set(v) {
                (settings as Value).sizeMultiplier = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var comboBasedSize: Boolean?
            get() = (settings as Value).comboBasedSize
            set(v) {
                (settings as Value).comboBasedSize = v
            }

        private data class Value(
            @JsonProperty("follow_delay") var followDelay: Float? = null,
            @JsonProperty("size_multiplier") var sizeMultiplier: Float? = null,
            @JsonProperty("combo_based_size") var comboBasedSize: Boolean? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "FL"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Blinds, FadeIn, Hidden, Cover)
            override val value: Int = 1024
        }
    }

    class Blinds : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "BL"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Flashlight)

        }
    }

    class StrictTracking : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "ST"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(TargetPractice, Classic)

        }
    }

    class AccuracyChallenge(
        minimumAccuracy: Float? = null,
        accuracyJudgeMode: String? = null,
        restart: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(minimumAccuracy, accuracyJudgeMode, restart)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var minimumAccuracy: Float?
            get() = (settings as Value).minimumAccuracy
            set(v) {
                (settings as Value).minimumAccuracy = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var accuracyJudgeMode: String?
            get() = (settings as Value).accuracyJudgeMode
            set(v) {
                (settings as Value).accuracyJudgeMode = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var restart: Boolean?
            get() = (settings as Value).restart
            set(v) {
                (settings as Value).restart = v
            }

        private data class Value(
            @JsonProperty("minimum_accuracy") var minimumAccuracy: Float? = null,
            @JsonProperty("accuracy_judge_mode") var accuracyJudgeMode: String? = null,
            @JsonProperty("restart") var restart: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "AC"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Easy, NoFail, Perfect, Cinema)

        }
    }

    class TargetPractice(
        seed: Float? = null,
        metronome: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(seed, metronome)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var seed: Float?
            get() = (settings as Value).seed
            set(v) {
                (settings as Value).seed = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var metronome: Boolean?
            get() = (settings as Value).metronome
            set(v) {
                (settings as Value).metronome = v
            }

        private data class Value(
            @JsonProperty("seed") var seed: Float? = null,
            @JsonProperty("metronome") var metronome: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "TP"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> =
                setOf(SuddenDeath, StrictTracking, Random, SpunOut, Traceable, ApproachDifferent, Depth)

        }
    }

    class DifficultyAdjust(
        circleSize: Float? = null,
        approachRate: Float? = null,
        drainRate: Float? = null,
        overallDifficulty: Float? = null,
        extendedLimits: Boolean? = null,
        scrollSpeed: Float? = null,
        hardRockOffsets: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any =
            Value(circleSize, approachRate, drainRate, overallDifficulty, extendedLimits, scrollSpeed, hardRockOffsets)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var circleSize: Float?
            get() = (settings as Value).circleSize
            set(v) {
                (settings as Value).circleSize = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var approachRate: Float?
            get() = (settings as Value).approachRate
            set(v) {
                (settings as Value).approachRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var drainRate: Float?
            get() = (settings as Value).drainRate
            set(v) {
                (settings as Value).drainRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var overallDifficulty: Float?
            get() = (settings as Value).overallDifficulty
            set(v) {
                (settings as Value).overallDifficulty = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var extendedLimits: Boolean?
            get() = (settings as Value).extendedLimits
            set(v) {
                (settings as Value).extendedLimits = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var scrollSpeed: Float?
            get() = (settings as Value).scrollSpeed
            set(v) {
                (settings as Value).scrollSpeed = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var hardRockOffsets: Boolean?
            get() = (settings as Value).hardRockOffsets
            set(v) {
                (settings as Value).hardRockOffsets = v
            }

        private data class Value(
            @JsonProperty("circle_size") var circleSize: Float? = null,
            @JsonProperty("approach_rate") var approachRate: Float? = null,
            @JsonProperty("drain_rate") var drainRate: Float? = null,
            @JsonProperty("overall_difficulty") var overallDifficulty: Float? = null,
            @JsonProperty("extended_limits") var extendedLimits: Boolean? = null,
            @JsonProperty("scroll_speed") var scrollSpeed: Float? = null,
            @JsonProperty("hard_rock_offsets") var hardRockOffsets: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "DA"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Easy, HardRock)

        }
    }

    class Classic(
        noSliderHeadAccuracy: Boolean? = null,
        classicNoteLock: Boolean? = null,
        alwaysPlayTailSample: Boolean? = null,
        fadeHitCircleEarly: Boolean? = null,
        classicHealth: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any =
            Value(noSliderHeadAccuracy, classicNoteLock, alwaysPlayTailSample, fadeHitCircleEarly, classicHealth)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var noSliderHeadAccuracy: Boolean?
            get() = (settings as Value).noSliderHeadAccuracy
            set(v) {
                (settings as Value).noSliderHeadAccuracy = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var classicNoteLock: Boolean?
            get() = (settings as Value).classicNoteLock
            set(v) {
                (settings as Value).classicNoteLock = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var alwaysPlayTailSample: Boolean?
            get() = (settings as Value).alwaysPlayTailSample
            set(v) {
                (settings as Value).alwaysPlayTailSample = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var fadeHitCircleEarly: Boolean?
            get() = (settings as Value).fadeHitCircleEarly
            set(v) {
                (settings as Value).fadeHitCircleEarly = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var classicHealth: Boolean?
            get() = (settings as Value).classicHealth
            set(v) {
                (settings as Value).classicHealth = v
            }

        private data class Value(
            @JsonProperty("no_slider_head_accuracy") var noSliderHeadAccuracy: Boolean? = null,
            @JsonProperty("classic_note_lock") var classicNoteLock: Boolean? = null,
            @JsonProperty("always_play_tail_sample") var alwaysPlayTailSample: Boolean? = null,
            @JsonProperty("fade_hit_circle_early") var fadeHitCircleEarly: Boolean? = null,
            @JsonProperty("classic_health") var classicHealth: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "CL"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(StrictTracking)

        }
    }

    class Random(
        angleSharpness: Float? = null,
        seed: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(angleSharpness, seed)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var angleSharpness: Float?
            get() = (settings as Value).angleSharpness
            set(v) {
                (settings as Value).angleSharpness = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var seed: Float?
            get() = (settings as Value).seed
            set(v) {
                (settings as Value).seed = v
            }

        private data class Value(
            @JsonProperty("angle_sharpness") var angleSharpness: Float? = null,
            @JsonProperty("seed") var seed: Float? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "RD"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(TargetPractice, Swap)
            override val value: Int = 2097152
        }
    }

    class Mirror(
        reflection: String? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(reflection)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var reflection: String?
            get() = (settings as Value).reflection
            set(v) {
                (settings as Value).reflection = v
            }

        private data class Value(
            @JsonProperty("reflection") var reflection: String? = null,
        )

        companion object : Mod, ValueMod {
            override val acronym: String = "MR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(HardRock)
            override val value: Int = 1073741824
        }
    }

    class Alternate : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "AL"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(SingleTap, Autoplay, Cinema, Relax)

        }
    }

    class SingleTap : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "SG"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko)
            override val incompatible: Set<Mod> = setOf(Alternate, Autoplay, Cinema, Relax)

        }
    }

    class Autoplay : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "AT"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(
                Alternate, SingleTap, Cinema, Relax, Autopilot, SpunOut, Magnetised, Repel, AdaptiveSpeed, TouchDevice
            )
            override val value: Int = 2048
        }
    }

    class Cinema : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "CN"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(
                NoFail,
                SuddenDeath,
                Perfect,
                AccuracyChallenge,
                Alternate,
                SingleTap,
                Autoplay,
                Cinema,
                Relax,
                Autopilot,
                SpunOut,
                Magnetised,
                Repel,
                AdaptiveSpeed,
                TouchDevice
            )
            override val value: Int = 4194304
        }
    }

    class Relax : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "RX"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch)
            override val incompatible: Set<Mod> = setOf(Alternate, SingleTap, Autoplay, Cinema, Autopilot, Magnetised)
            override val value: Int = 128
        }
    }

    class Autopilot : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "AP"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Autoplay, Cinema, Relax, SpunOut, Magnetised, Repel, TouchDevice)
            override val value: Int = 8192
        }
    }

    class SpunOut : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "SO"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(TargetPractice, Autoplay, Cinema, Autopilot)
            override val value: Int = 4096
        }
    }

    class Transform : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "TR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Wiggle, Magnetised, Repel, FreezeFrame, Depth)

        }
    }

    class Wiggle(
        strength: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(strength)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var strength: Float?
            get() = (settings as Value).strength
            set(v) {
                (settings as Value).strength = v
            }

        private data class Value(
            @JsonProperty("strength") var strength: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "WG"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Transform, Magnetised, Repel, Depth)

        }
    }

    class SpinIn : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "SI"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Hidden, Grow, Deflate, Traceable, ApproachDifferent, Depth)

        }
    }

    class Grow(
        startScale: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(startScale)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var startScale: Float?
            get() = (settings as Value).startScale
            set(v) {
                (settings as Value).startScale = v
            }

        private data class Value(
            @JsonProperty("start_scale") var startScale: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "GR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(SpinIn, Grow, Deflate, Traceable, ApproachDifferent, Depth)

        }
    }

    class Deflate(
        startScale: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(startScale)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var startScale: Float?
            get() = (settings as Value).startScale
            set(v) {
                (settings as Value).startScale = v
            }

        private data class Value(
            @JsonProperty("start_scale") var startScale: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "DF"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(SpinIn, Grow, Deflate, Traceable, ApproachDifferent, Depth)

        }
    }

    class WindUp(
        initialRate: Float? = null,
        finalRate: Float? = null,
        adjustPitch: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(initialRate, finalRate, adjustPitch)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var initialRate: Float?
            get() = (settings as Value).initialRate
            set(v) {
                (settings as Value).initialRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var finalRate: Float?
            get() = (settings as Value).finalRate
            set(v) {
                (settings as Value).finalRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var adjustPitch: Boolean?
            get() = (settings as Value).adjustPitch
            set(v) {
                (settings as Value).adjustPitch = v
            }

        private data class Value(
            @JsonProperty("initial_rate") var initialRate: Float? = null,
            @JsonProperty("final_rate") var finalRate: Float? = null,
            @JsonProperty("adjust_pitch") var adjustPitch: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "WU"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(HalfTime, Daycore, DoubleTime, Nightcore, WindDown, AdaptiveSpeed)

        }
    }

    class WindDown(
        initialRate: Float? = null,
        finalRate: Float? = null,
        adjustPitch: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(initialRate, finalRate, adjustPitch)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var initialRate: Float?
            get() = (settings as Value).initialRate
            set(v) {
                (settings as Value).initialRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var finalRate: Float?
            get() = (settings as Value).finalRate
            set(v) {
                (settings as Value).finalRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var adjustPitch: Boolean?
            get() = (settings as Value).adjustPitch
            set(v) {
                (settings as Value).adjustPitch = v
            }

        private data class Value(
            @JsonProperty("initial_rate") var initialRate: Float? = null,
            @JsonProperty("final_rate") var finalRate: Float? = null,
            @JsonProperty("adjust_pitch") var adjustPitch: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "WD"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(HalfTime, Daycore, DoubleTime, Nightcore, WindUp, AdaptiveSpeed)

        }
    }

    class Traceable : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "TC"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Hidden, TargetPractice, SpinIn, Grow, Deflate, Depth)

        }
    }

    class BarrelRoll(
        spinSpeed: Float? = null,
        direction: String? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(spinSpeed, direction)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var spinSpeed: Float?
            get() = (settings as Value).spinSpeed
            set(v) {
                (settings as Value).spinSpeed = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var direction: String?
            get() = (settings as Value).direction
            set(v) {
                (settings as Value).direction = v
            }

        private data class Value(
            @JsonProperty("spin_speed") var spinSpeed: Float? = null,
            @JsonProperty("direction") var direction: String? = null,
        )

        companion object : Mod {
            override val acronym: String = "BR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Bubbles)

        }
    }

    class ApproachDifferent(
        scale: Float? = null,
        style: String? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(scale, style)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var scale: Float?
            get() = (settings as Value).scale
            set(v) {
                (settings as Value).scale = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var style: String?
            get() = (settings as Value).style
            set(v) {
                (settings as Value).style = v
            }

        private data class Value(
            @JsonProperty("scale") var scale: Float? = null,
            @JsonProperty("style") var style: String? = null,
        )

        companion object : Mod {
            override val acronym: String = "AD"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Hidden, TargetPractice, SpinIn, Grow, Deflate, FreezeFrame)

        }
    }

    class Muted(
        inverseMuting: Boolean? = null,
        enableMetronome: Boolean? = null,
        muteComboCount: Float? = null,
        affectsHitSounds: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(inverseMuting, enableMetronome, muteComboCount, affectsHitSounds)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var inverseMuting: Boolean?
            get() = (settings as Value).inverseMuting
            set(v) {
                (settings as Value).inverseMuting = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var enableMetronome: Boolean?
            get() = (settings as Value).enableMetronome
            set(v) {
                (settings as Value).enableMetronome = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var muteComboCount: Float?
            get() = (settings as Value).muteComboCount
            set(v) {
                (settings as Value).muteComboCount = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var affectsHitSounds: Boolean?
            get() = (settings as Value).affectsHitSounds
            set(v) {
                (settings as Value).affectsHitSounds = v
            }

        private data class Value(
            @JsonProperty("inverse_muting") var inverseMuting: Boolean? = null,
            @JsonProperty("enable_metronome") var enableMetronome: Boolean? = null,
            @JsonProperty("mute_combo_count") var muteComboCount: Float? = null,
            @JsonProperty("affects_hit_sounds") var affectsHitSounds: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "MU"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf()

        }
    }

    class NoScope(
        hiddenComboCount: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(hiddenComboCount)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var hiddenComboCount: Float?
            get() = (settings as Value).hiddenComboCount
            set(v) {
                (settings as Value).hiddenComboCount = v
            }

        private data class Value(
            @JsonProperty("hidden_combo_count") var hiddenComboCount: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "NS"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Catch)
            override val incompatible: Set<Mod> = setOf()

        }
    }

    class Magnetised(
        attractionStrength: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(attractionStrength)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var attractionStrength: Float?
            get() = (settings as Value).attractionStrength
            set(v) {
                (settings as Value).attractionStrength = v
            }

        private data class Value(
            @JsonProperty("attraction_strength") var attractionStrength: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "MG"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> =
                setOf(Autoplay, Cinema, Relax, Autopilot, Transform, Wiggle, Repel, Bubbles, Depth)

        }
    }

    class Repel(
        repulsionStrength: Float? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(repulsionStrength)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var repulsionStrength: Float?
            get() = (settings as Value).repulsionStrength
            set(v) {
                (settings as Value).repulsionStrength = v
            }

        private data class Value(
            @JsonProperty("repulsion_strength") var repulsionStrength: Float? = null,
        )

        companion object : Mod {
            override val acronym: String = "RP"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> =
                setOf(Autoplay, Cinema, Autopilot, Transform, Wiggle, Magnetised, Bubbles, Depth)

        }
    }

    class AdaptiveSpeed(
        initialRate: Float? = null,
        adjustPitch: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(initialRate, adjustPitch)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var initialRate: Float?
            get() = (settings as Value).initialRate
            set(v) {
                (settings as Value).initialRate = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var adjustPitch: Boolean?
            get() = (settings as Value).adjustPitch
            set(v) {
                (settings as Value).adjustPitch = v
            }

        private data class Value(
            @JsonProperty("initial_rate") var initialRate: Float? = null,
            @JsonProperty("adjust_pitch") var adjustPitch: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "AS"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Mania)
            override val incompatible: Set<Mod> =
                setOf(HalfTime, Daycore, DoubleTime, Nightcore, Autoplay, Cinema, WindUp, WindDown)

        }
    }

    class FreezeFrame : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "FR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Transform, ApproachDifferent, Depth)

        }
    }

    class Bubbles : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "BU"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(BarrelRoll, Magnetised, Repel)

        }
    }

    class Synesthesia : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "SY"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf()

        }
    }

    class Depth(
        maxDepth: Float? = null,
        showApproachCircles: Boolean? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(maxDepth, showApproachCircles)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var maxDepth: Float?
            get() = (settings as Value).maxDepth
            set(v) {
                (settings as Value).maxDepth = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var showApproachCircles: Boolean?
            get() = (settings as Value).showApproachCircles
            set(v) {
                (settings as Value).showApproachCircles = v
            }

        private data class Value(
            @JsonProperty("max_depth") var maxDepth: Float? = null,
            @JsonProperty("show_approach_circles") var showApproachCircles: Boolean? = null,
        )

        companion object : Mod {
            override val acronym: String = "DP"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(
                Hidden,
                TargetPractice,
                Transform,
                Wiggle,
                SpinIn,
                Grow,
                Deflate,
                Traceable,
                Magnetised,
                Repel,
                FreezeFrame,
                Depth
            )

        }
    }

    class TouchDevice : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "TD"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu)
            override val incompatible: Set<Mod> = setOf(Autoplay, Cinema, Autopilot)
            override val value: Int = 4
        }
    }

    class ScoreV2 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "SV2"
            override val mode: Set<OsuMode> = setOf(OsuMode.Osu, OsuMode.Taiko, OsuMode.Catch, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf()
            override val value: Int = 536870912
        }
    }

    class Swap : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "SW"
            override val mode: Set<OsuMode> = setOf(OsuMode.Taiko)
            override val incompatible: Set<Mod> = setOf(Random)

        }
    }

    class ConstantSpeed : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "CS"
            override val mode: Set<OsuMode> = setOf(OsuMode.Taiko, OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf()

        }
    }

    class FloatingFruits : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "FF"
            override val mode: Set<OsuMode> = setOf(OsuMode.Catch)
            override val incompatible: Set<Mod> = setOf()

        }
    }

    class NoRelease : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "NR"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(HoldOff)

        }
    }

    class FadeIn : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "FI"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(FadeIn, Hidden, Cover, Flashlight)
            override val value: Int = 1048576
        }
    }

    class Cover(
        coverage: Float? = null,
        direction: String? = null,
    ) : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any = Value(coverage, direction)

        @JsonProperty("settings")
        private fun setSettings(node: JsonNode) {
            settings = node.json<Value>()
        }

        @get:JsonIgnore
        @set:JsonIgnore
        var coverage: Float?
            get() = (settings as Value).coverage
            set(v) {
                (settings as Value).coverage = v
            }

        @get:JsonIgnore
        @set:JsonIgnore
        var direction: String?
            get() = (settings as Value).direction
            set(v) {
                (settings as Value).direction = v
            }

        private data class Value(
            @JsonProperty("coverage") var coverage: Float? = null,
            @JsonProperty("direction") var direction: String? = null,
        )

        companion object : Mod {
            override val acronym: String = "CO"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(FadeIn, Hidden, Flashlight)

        }
    }

    class DualStages : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "DS"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf()

        }
    }

    class Invert : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "IN"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(HoldOff)

        }
    }

    class HoldOff : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "HO"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(NoRelease, Invert)

        }
    }

    class Key1 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "1K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key2, Key3, Key4, Key5, Key6, Key7, Key8, Key9, Key10)
            override val value: Int = 67108864
        }
    }

    class Key2 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "2K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key3, Key4, Key5, Key6, Key7, Key8, Key9, Key10)
            override val value: Int = 268435456
        }
    }

    class Key3 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "3K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key4, Key5, Key6, Key7, Key8, Key9, Key10)
            override val value: Int = 134217728
        }
    }

    class Key4 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "4K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key5, Key6, Key7, Key8, Key9, Key10)
            override val value: Int = 32768
        }
    }

    class Key5 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "5K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key4, Key6, Key7, Key8, Key9, Key10)
            override val value: Int = 65536
        }
    }

    class Key6 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "6K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key4, Key5, Key7, Key8, Key9, Key10)
            override val value: Int = 131072
        }
    }

    class Key7 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "7K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key4, Key5, Key6, Key8, Key9, Key10)
            override val value: Int = 262144
        }
    }

    class Key8 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "8K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key4, Key5, Key6, Key7, Key9, Key10)
            override val value: Int = 524288
        }
    }

    class Key9 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod, ValueMod {
            override val acronym: String = "9K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key4, Key5, Key6, Key7, Key8, Key10)
            override val value: Int = 16777216
        }
    }

    class Key10 : LazerMod() {
        @get:JsonProperty("acronym")
        override val type: String = acronym

        @get:JsonProperty("settings")
        override var settings: Any? = null

        companion object : Mod {
            override val acronym: String = "10K"
            override val mode: Set<OsuMode> = setOf(OsuMode.Mania)
            override val incompatible: Set<Mod> = setOf(Key1, Key2, Key3, Key4, Key5, Key6, Key7, Key8, Key9)
        }
    }
}

fun main() {
    val mods = listOf(LazerMod.Hidden(), LazerMod.DoubleTime())
    println(mods.toJson())
}