package org.spring.web.service

import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import org.spring.osu.beatmap.mirror.OsuBeatmapMirror
import org.spring.osu.beatmap.mirror.OsuFileRecord
import org.spring.osu.model.Beatmapset
import java.io.OutputStream
import java.nio.file.Path

object OsuMirrorService {
    suspend fun getFilePath(bid: Long, typeString: String): Path {
        val type = OsuFileRecord.getType(typeString) ?: throw IllegalArgumentException("Invalid type")
        val path = OsuBeatmapMirror.getLocalPathByBid(bid, type)

        return path
    }

    suspend fun getZipOutput(sid: Long, output: OutputStream, hasVideo: Boolean = true) {
        OsuBeatmapMirror.zipBeatmapset(sid, output, hasVideo)
    }

    suspend fun getZipOutput(sidList: List<Long>, output: OutputStream, hasVideo: Boolean = true) {
        OsuBeatmapMirror.zipAllBeatmapsets(sidList, output, hasVideo)
    }

    suspend fun updateFile(bid: Long, channel: ByteReadChannel) {
        OsuBeatmapMirror.upload(bid, channel.toInputStream(SupervisorJob()))
    }

    suspend fun asyncDownload(bid: Long) {
        coroutineScope {
            val channel = Array<Beatmapset?>(1) { null }
            val needUpdate = OsuBeatmapMirror.shouldUpdate(bid, channel)
            if (needUpdate) {
                OsuBeatmapMirror.download(bid, channel[0])
            }
        }
    }

    suspend fun getAllCount() = OsuFileRecord.getAllCount()

}