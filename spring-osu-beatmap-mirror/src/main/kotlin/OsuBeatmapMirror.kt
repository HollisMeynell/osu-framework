package org.spring.osu.beatmap.mirror

import io.ktor.util.cio.readChannel
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.jetbrains.exposed.sql.Column
import org.spring.osu.OsuApi
import org.spring.osu.RankStatus
import org.spring.osu.extended.api.OsuWebApi
import org.spring.osu.model.Beatmapset
import org.spring.osu.persistence.OsuDatabases.suspendTransaction
import org.spring.osu.persistence.model.OsuWebUserRecord
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.Path


object OsuBeatmapMirror {
    const val BUFFER_SIZE = 8192
    var config: OsuBeatmapMirrorConfig = OsuBeatmapMirrorConfig("")
    val basePath by lazy {
        Path(config.basePath)
    }

    private suspend fun shouldUpdate(bid: Long, channel: Array<Beatmapset?>? = null): Boolean {
        val record = OsuFileRecord.getByBid(bid) ?: return true
        val status = record.status
        if (status == RankStatus.Ranked.value
            || status == RankStatus.Approved.value
            || status == RankStatus.Loved.value
        ) {
            return false
        }
        val beatmapset = OsuApi.getBeatmapset(record.sid)
        channel?.let { it[0] = beatmapset }
        if (beatmapset.status.value != status && status == RankStatus.Graveyard.value) {
            // graveyard -> any: need update
            return true
        }
        val map = beatmapset.beatmaps?.firstOrNull { it.id == bid }
        // delete diff
        if (map == null) return true
        // update diff
        if (map.lastUpdated!!.toInstant() != record.last.toInstant()) {
            return true
        }
        return false
    }

    suspend fun upload(sid: Long, input: InputStream) {
        val beatmapset = OsuApi.getBeatmapset(sid)

        withContext(Dispatchers.IO + SupervisorJob()) {
            writeStream(sid, beatmapset, input)
        }
    }

    suspend fun download(sid: Long, beatmapset: Beatmapset? = null) {
        val beatmapset = beatmapset ?: OsuApi.getBeatmapset(sid)
        val byteChannel = ByteChannel(true)
        val account = OsuWebUserRecord.getRandomRecord() ?: throw IllegalStateException("No account found")

        coroutineScope {
            val job1 = launch {
                OsuWebApi.doDownloadOsz(account, byteChannel, sid)
            }
            withContext (Dispatchers.IO + SupervisorJob()) {
                writeStream(sid, beatmapset, byteChannel.toInputStream())
            }
            job1.join()
        }
    }

    private suspend fun writeStream(sid: Long, beatmapset: Beatmapset, input: InputStream) {
        val path = basePath.resolve(sid.toString())
        val zipIn = ZipArchiveInputStream(input)
        zipIn.use {
            val allRecord = it.read(beatmapset, path)
            OsuFileRecord.saveAll(allRecord)
        }
    }

    suspend fun remove(sid: Long) {
        OsuFileRecord.deleteBySid(sid)
        Files.walk(basePath.resolve(sid.toString())).use {
            it.sorted(Comparator.reverseOrder()).forEach(Files::delete)
        }
    }

    suspend fun zipAllBeatmapsets(sids: Iterable<Long>, output: OutputStream, hasVideo: Boolean = true) {
        val zipOutput = ZipArchiveOutputStream(output)
        try {
            for (sid in sids) {
                val subEntry = ZipArchiveEntry("$sid.osz")
                zipOutput.putArchiveEntry(subEntry)
                val subZip = ZipArchiveOutputStream(zipOutput)
                writeDirToZip(subZip, basePath.resolve(sid.toString()), hasVideo)
                subZip.finish()
            }
        } finally {
            zipOutput.closeArchiveEntry()
            zipOutput.finish()
        }
    }

    suspend fun zipBeatmapset(sid: Long, output: OutputStream, hasVideo: Boolean = true) {
        val zipOutput = ZipArchiveOutputStream(output)
        try {
            writeDirToZip(zipOutput, basePath.resolve(sid.toString()), hasVideo)
        } finally {
            zipOutput.finish()
        }
    }

    private suspend fun writeDirToZip(zip: ZipArchiveOutputStream, path: Path, hasVideo: Boolean, base: String = "") {
        Files.list(path).use { list ->
            for (it in list.toList()) {
                if (hasVideo.not()) {
                    val name = it.fileName.toString()
                    if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".flv")) {
                        continue
                    }
                }
                if (Files.isDirectory(it)) {
                    writeDirToZip(zip, it, hasVideo, "${base}${it.fileName}/")
                } else {
                    val entry = ZipArchiveEntry("${base}${it.fileName}")
                    zip.putArchiveEntry(entry)
                    it.readChannel().copyTo(zip)
                    zip.closeArchiveEntry()
                }
            }
        }
    }

    suspend fun getLocalPathByBid(bid: Long, type: OsuFileRecord.Type): Path? {
        val column: Column<String> = when (type) {
            OsuFileRecord.Type.Audio -> OsuFileRecord.audio
            OsuFileRecord.Type.Background -> OsuFileRecord.background
            OsuFileRecord.Type.OsuFile -> OsuFileRecord.name
        }

        val beatmapsetArray = Array<Beatmapset?>(1) { null }
        if (shouldUpdate(bid)) {
            val beatmapset = beatmapsetArray[0]
            if (beatmapset == null) {
                val sid = OsuApi.getBeatmaps(bid).first().beatmapsetID
                download(sid)
            } else {
                download(beatmapset.id, beatmapset)
            }
        }
        val path = suspendTransaction {
            val record = OsuFileRecord.select(OsuFileRecord.name)
                .where { OsuFileRecord.bid eq bid }
                .map { it[OsuFileRecord.sid] to it[column] }
                .firstOrNull()
            return@suspendTransaction record?.let {
                basePath.resolve(it.first.toString()).resolve(it.second)
            }
        }

        if (path != null) return path
        throw IllegalStateException("File not found")
    }

    private fun ZipArchiveInputStream.read(beatmapset: Beatmapset, basePath: Path): List<OsuFileRecord> {
        val beatmapHashMap = beatmapset.beatmaps!!.map {
            it.checksum to it
        }.toMap()
        val records = ArrayList<OsuFileRecord>(beatmapHashMap.size)
        var entry: ZipArchiveEntry
        var zipFilePath: Path
        Files.createDirectories(basePath)
        while (nextEntry.also { entry = it } != null) {
            zipFilePath = basePath.resolve(entry.name)
            if (entry.isDirectory) {
                continue
            }
            Files.createDirectories(zipFilePath.parent)
            if (entry.name.endsWith(".osu")) {
                val fileData = readNBytes(entry.size.toInt())
                val fileMd5 = md5(fileData)
                val beatmap = beatmapHashMap[fileMd5] ?: throw IllegalStateException("Beatmap not found")
                val (audio, background) = OsuFileRecord
                    .parseAudioAndBackground(BufferedReader(InputStreamReader(ByteArrayInputStream(fileData))))
                val record = OsuFileRecord(
                    sid = beatmapset.id,
                    bid = beatmap.id,
                    name = entry.name,
                    background = background,
                    audio = audio,
                    version = beatmap.version,
                    mode = beatmap.mode.value,
                    check = fileMd5,
                    status = beatmap.status.value,
                    last = beatmap.lastUpdated!!,
                )
                records += record
                Files.write(zipFilePath, fileData)
            } else if (entry.size > 1024 * 1024) {
                bufferWrite(zipFilePath, entry.size)
            } else {
                Files.write(zipFilePath, readNBytes(entry.size.toInt()))
            }
        }
        return records
    }

    private fun ZipArchiveInputStream.bufferWrite(path: Path, size: Long) {
        var n = 0
        var buffer = ByteArray(BUFFER_SIZE)
        var channel = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
        channel.use {
            while (n < size - BUFFER_SIZE) {
                readNBytes(buffer, 0, BUFFER_SIZE)
                it.write(buffer)
                n += BUFFER_SIZE
            }
            val need = (size - n).toInt()
            readNBytes(buffer, 0, need)
            it.write(buffer, 0, need)
        }
    }

    fun md5(data: ByteArray): String {
        val digestMD5 = MessageDigest.getInstance("MD5")
        digestMD5.update(data)
        val result = digestMD5.digest()
        return result.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    }
}