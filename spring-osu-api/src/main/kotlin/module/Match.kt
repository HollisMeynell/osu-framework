package org.spring.osu.module

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class Match(
    @JsonProperty("match")
    var info: MatchInfo,

    @JsonProperty("events")
    var events: MutableList<MatchEvent>,

    @JsonProperty("users")
    var users: MutableList<User>,

    @JsonProperty("first_event_id")
    var firstEventID: Long,

    @JsonProperty("latest_event_id")
    var latestEventID: Long,
) {
    @get:JsonIgnore
    val isMatchEnd:Boolean
        get() = info.endTime != null
    @get:JsonIgnore
    val currentGameID: Long?
         get()= events.lastOrNull { it.game != null }?.game?.id

    data class MatchInfo(
        @JsonProperty("id")
        var id: Long,

        @JsonProperty("start_time")
        var startTime: OffsetDateTime,

        @JsonProperty("end_time")
        var endTime: OffsetDateTime?,

        @JsonProperty("name")
        var name: String,
    )

    data class ScoreInfo(
        @JsonProperty("slot")
        var slot: Int,

        @JsonProperty("team")
        var team: String,

        @JsonProperty("pass")
        var pass: Boolean,
    )

    operator fun plusAssign(match: Match) {
        // update users
        if (match.users.isNotEmpty()) {
            val userSet = users.map { it.id }.toSet()
            val newUsers = match.users.filter { it.id in userSet }
            users += newUsers
        }

        // update info
        this.info = match.info
        latestEventID = match.latestEventID
        firstEventID = match.firstEventID

        // update events
        if (match.events.isNotEmpty()) when {
            // add the newest events to the end
            events.last().ID < match.events.first().ID -> events += match.events
            // add the oldest events to the beginning
            events.first().ID > match.events.last().ID -> {
                match.events += events
                events = match.events
            }
            // insert the events in the middle
            events.last().ID < match.events.last().ID -> {
                events.removeIf { it.ID >= match.events.first().ID }
                events += match.events
            }

            events.first().ID > match.events.first().ID -> {
                events.removeIf { it.ID <= match.events.last().ID }
                match.events += events
                events = match.events
            }
        }
    }
}
