package org.spring.web.service

import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.SupervisorJob
import org.spring.osu.beatmap.mirror.OsuBeatmapMirror
import org.spring.osu.beatmap.mirror.OsuFileRecord

object MirrorService {
    suspend fun getFileName(bid:Long, typeString: String) : String {
        val type = OsuFileRecord.getType(typeString) ?: throw IllegalArgumentException("Invalid type")
        val path = OsuBeatmapMirror.getLocalPathByBid(bid, type)

        return path.fileName.toString()
    }

    suspend fun updateFile(bid: Long, channel: ByteReadChannel) {
        OsuBeatmapMirror.upload(bid, channel.toInputStream(SupervisorJob()))
    }
}