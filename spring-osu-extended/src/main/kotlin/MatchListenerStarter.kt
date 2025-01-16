package org.spring.osu.extended

import kotlinx.coroutines.*
import org.spring.core.CoroutineScope
import org.spring.core.ticker
import org.spring.osu.OsuApi
import org.spring.osu.model.Match
import org.spring.osu.model.MatchEvent
import org.spring.osu.model.MatchEventType
import org.spring.osu.model.User
import org.spring.osu.extended.model.MatchListener
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class MatchListenerStarter(
    private val matchID: Long,
    vararg listener: MatchListener,
) {
    lateinit var stopJob: () -> Unit
    private val kill: Job = CoroutineScope.async {
        delay(3.hours)
        stop(StopType.Timeout)
    }
    private var match: Match
    private var listeners = mutableListOf<MatchListener>()
    private var nowGame: Long? = null
    private var nowEvent: Long? = null
    private val usersIDSet = mutableSetOf<Long>()
    private val userMap = mutableMapOf<Long, User>()

    var beforeEvent: suspend MatchEvent.() -> Unit = {}

    init {
        runBlocking {
            match = OsuApi.getMatch(matchID)
            listeners.addAll(listener)
            listeners.forEach { it.match = match }

            parseUsers(match.events, match.users)

            if (match.currentGameID != null) {
                val gameEvent = match.events.last { it.game != null }
                nowGame = gameEvent.game!!.id
                nowEvent = gameEvent.ID - 1
                onGameEvent(gameEvent)
            } else {
                nowEvent = match.latestEventID
            }
        }
    }

    val isStart: Boolean
        get() = ::stopJob.isInitialized

    private suspend fun doAction() {
        val newMatch = try {
            OsuApi.getMatch(matchID)
        } catch (e: Exception) {
            onError(e)
            return
        }

        // no event
        if (nowEvent == newMatch.latestEventID) return

        // has game
        if (newMatch.currentGameID != null) {
            val gameEvent = newMatch.events.last { it.game != null }
            val isAbort = nowGame != newMatch.currentGameID

            if (isAbort) {
                nowGame = newMatch.currentGameID
            }

            if (nowEvent == gameEvent.ID - 1 && isAbort.not()) {
                return
            } else {
                nowEvent = gameEvent.ID - 1
            }
        } else {
            nowGame = null
            nowEvent = newMatch.latestEventID
        }
        onNewMatch(newMatch)
        if (newMatch.isMatchEnd) stop(StopType.End)
    }

    @Suppress("unused")
    suspend fun start() {
        if (isStart) throw IllegalStateException("MatchListenerStarter is already started")
        if (match.isMatchEnd) {
            stop(StopType.End)
            return
        }
        stopJob = CoroutineScope.ticker(10.seconds) { doAction() }
        coroutineScope { kill.start() }
        listeners.on { onListenStart() }
    }

    fun stop(type: StopType = StopType.Abort) {
        if (!isStart) return
        kill.cancel()
        stopJob()
        listeners.on { onListenEnd(type) }
    }

    private suspend fun onNewMatch(newMatch: Match) {
        match += newMatch
        parseUsers(newMatch.events, newMatch.users)
        val games = newMatch.events.filter { it.game != null }
        if (games.isEmpty()) {
            return
        }
        if (games.size > 1) {
            val abortGame = games.dropLast(1)
            abortGame.forEach {
                if (it.game?.endTime == null || it.game?.scores.isNullOrEmpty()) {
                    onGameAbort(it)
                } else {
                    onGameEvent(it)
                }
            }
        }
        onGameEvent(games.last())
    }

    private fun parseUsers(events: List<MatchEvent>, users: List<User>) {
        userMap.putAll(users.map { it.id to it })
        for (it in events) when (it.type) {
            MatchEventType.PlayerJoined, MatchEventType.HostChanged -> {
                usersIDSet + it.userID
            }

            MatchEventType.PlayerLeft, MatchEventType.PlayerKicked -> {
                usersIDSet - it.userID
            }

            MatchEventType.Other -> {
                if (it.game != null) {
                    it.game!!.scores?.forEach { g -> usersIDSet + g.userID }
                }
            }

            else -> {}

        }
        usersIDSet.addAll(users.map { it.id })
    }

    private fun onError(e: Exception) {
        listeners.forEach { it.onError(e) }
    }

    private fun onGameAbort(event: MatchEvent) {
        val beatmapID = event.game?.beatmapID ?: return
        listeners.on {
            onGameAbort(beatmapID)
        }
    }

    private suspend fun onGameEvent(event: MatchEvent) {
        val game = event.game ?: return
        event.beforeEvent()

        if (game.endTime != null) {
            // is game end
            val e = MatchListener.EndEvent(
                game,
                event.ID,
                userMap
            )
            listeners.on { onGameEnd(e) }
        } else {
            // is game start
            val e = MatchListener.StartEvent(
                event.ID,
                match.info.name,
                game.beatmapID,
                game.beatmap!!,
                game.startTime,
                game.mode,
                game.mods,
                game.isTeamVS,
                game.teamType,
                usersIDSet.mapNotNull { userMap[it] }
            )
            listeners.on { onGameStart(e) }
        }
    }

    private fun List<MatchListener>.on(action : MatchListener.() -> Unit) {
        forEach {
            try {
                it.action()
            } catch (e: Exception) {
                it.onError(e)
            }
        }
    }

    enum class StopType {
        End,
        Abort,
        Timeout
    }
}
