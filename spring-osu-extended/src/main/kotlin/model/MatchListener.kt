package org.spring.osu.extended.model

import org.spring.osu.OsuMod
import org.spring.osu.OsuMode
import org.spring.osu.extended.MatchListenerStarter
import org.spring.osu.model.Beatmap
import org.spring.osu.model.GameTeamType
import org.spring.osu.model.Match
import org.spring.osu.model.MatchGame
import org.spring.osu.model.User
import java.time.OffsetDateTime

interface MatchListener {
    var match: Match
    fun onListenStart()
    fun onListenEnd(type: MatchListenerStarter.StopType)
    fun onGameStart(event: StartEvent)
    fun onGameEnd(event: EndEvent)
    fun onGameAbort(beatmapID: Long)
    fun onError(e: Exception)

    data class StartEvent(
        val id: Long,
        val matchName: String,
        val beatmapID: Long,
        var beatmap: Beatmap,
        val start: OffsetDateTime,
        val mode: OsuMode,
        val mods: List<OsuMod>,
        val isTeamVS: Boolean,
        val teamType: GameTeamType,
        val users: List<User>,
    )

    data class EndEvent(
        val gameEvent: MatchGame,
        val id: Long,
        val users: Map<Long, User>,
    )
}