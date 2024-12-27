package org.spring.osu.persistence

import org.spring.osu.OsuApi
import org.spring.osu.model.Beatmap
import org.spring.osu.model.Beatmapset
import org.spring.osu.model.UserAuth
import org.spring.osu.persistence.entity.OsuBeatmapRecord
import org.spring.osu.persistence.entity.OsuBeatmapsetRecord

suspend fun OsuApi.getBeatmapByDatabase(bid: Long, auth: UserAuth? = null) :Beatmap {
    val result = OsuBeatmapRecord.getByID(bid, false)
    if (result != null) return result

    val beatmap = getBeatmap(bid, auth)
    OsuBeatmapsetRecord.save(getBeatmapset(beatmap.beatmapsetID, auth))
    return beatmap
}
suspend fun OsuApi.getBeatmapWithBeammapsetByDatabase(bid: Long, auth: UserAuth? = null) :Beatmap {
    val result = OsuBeatmapRecord.getByID(bid, true)
    if (result != null) return result

    val beatmap = getBeatmap(bid, auth)
    OsuBeatmapsetRecord.save(getBeatmapset(beatmap.beatmapsetID, auth))
    return beatmap
}

suspend fun OsuApi.getBeatmapsetByDatabase(sid: Long, auth: UserAuth? = null): Beatmapset {
    val result = OsuBeatmapsetRecord.getByID(sid, false)
    if (result != null) return result

    val beatmapset = getBeatmapset(sid, auth)
    OsuBeatmapsetRecord.save(beatmapset)
    return beatmapset
}

suspend fun OsuApi.getBeatmapsetWithBeatmapsByDatabase(sid: Long, auth: UserAuth? = null) : Beatmapset{
    val result = OsuBeatmapsetRecord.getByID(sid, true)
    if (result != null) {
        return result
    }

    val beatmapset = getBeatmapset(sid, auth)
    OsuBeatmapsetRecord.save(beatmapset)
    return beatmapset
}