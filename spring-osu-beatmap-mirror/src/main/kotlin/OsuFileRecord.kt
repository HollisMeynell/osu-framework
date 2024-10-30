package org.spring.osu.beatmap.mirror

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.spring.osu.persistence.OsuDatabases
import org.spring.osu.persistence.OsuDatabases.suspendTransaction
import java.io.BufferedReader
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class OsuFileRecord(
    var bid: Long,
    var sid: Long,
    var name: String,
    var background: String,
    var audio: String,
    var version: String,
    var mode: Int,
    var check: String,
    var status: Int,
    var last: OffsetDateTime,
) {
    enum class Type {
        OsuFile, Audio, Background
    }

    companion object : IdTable<Long>("beatmap_files") {
        val bid = long("bid")

        val sid = long("sid").index("local_dir", isUnique = false)

        val name = text("file_name")

        val background = text("background")

        val audio = text("audio")

        val version = text("version")

        val mode = integer("mode")

        val check = char("check", 32).index("check_str")

        val status = integer("status")

        val last = timestamp("last_time")


        override val id = bid.entityId()
        override val primaryKey = PrimaryKey(bid)

        init {
            OsuDatabases.registerTable(this)
        }

        fun parseAudioAndBackground(reader: BufferedReader): Pair<String, String> {
            var line: String
            var audio: String? = null
            var background: String? = null
            var inEvents = false
            while (reader.readLine().also { line = it } != null) {
                if (line.contains("AudioFilename: ")) {
                    audio = line.substringAfter("AudioFilename: ")
                } else if (line.contains("[Events]")) inEvents = true

                if (inEvents && line.startsWith("0,0,\"")) {
                    val end = line.lastIndexOf('"')
                    background = line.substring(5, end)
                    break
                }

                if (inEvents && line.contains("[TimingPoints]")) {
                    break
                }
            }
            reader.close()
            return (audio ?: "unknown") to (background ?: "unknown")
        }

        suspend fun getLastUpdateTimeBySid(sid: Long): Instant? {
            return suspendTransaction {
                return@suspendTransaction select(last)
                    .where { OsuFileRecord.sid eq sid }
                    .orderBy(last)
                    .limit(1)
                    .map { it[last] }
                    .firstOrNull()
            }
        }

        suspend fun getLastUpdateTimeByBid(bid: Long): Instant? {
            return suspendTransaction {
                return@suspendTransaction select(last)
                    .where { OsuFileRecord.bid eq bid }
                    .map { it[last] }
                    .firstOrNull()
            }
        }

        suspend fun deleteBySid(sid: Long) = suspendTransaction {
            OsuFileRecord.deleteWhere { with(it) { OsuFileRecord.sid eq sid } }
        }

        suspend fun getByBid(bid: Long): OsuFileRecord? {
            return suspendTransaction {
                return@suspendTransaction selectAll()
                    .where { OsuFileRecord.bid eq bid }
                    .map(::toEntity)
                    .firstOrNull()
            }
        }

        suspend fun saveAll(records: List<OsuFileRecord>) = suspendTransaction {
            OsuFileRecord.batchUpsert(records, shouldReturnGeneratedValues = false) {
                val r = it
                this[bid] = r.bid
                this[sid] = r.sid
                this[name] = r.name
                this[background] = r.background
                this[audio] = r.audio
                this[version] = r.version
                this[mode] = r.mode
                this[check] = r.check
                this[status] = r.status
                this[last] = r.last.toInstant()
            }
        }

        fun toEntity(it: ResultRow) = OsuFileRecord (
            bid = it[bid],
            sid = it[sid],
            name = it[name],
            background = it[background],
            audio = it[audio],
            version = it[version],
            mode = it[mode],
            check = it[check],
            status = it[status],
            last = OffsetDateTime.ofInstant(it[last], ZoneOffset.systemDefault())
        )
    }

    suspend fun save() {
        val r = this
        suspendTransaction {
            OsuFileRecord.upsert {
                it[bid] = r.bid
                it[sid] = r.sid
                it[name] = r.name
                it[background] = r.background
                it[audio] = r.audio
                it[version] = r.version
                it[mode] = r.mode
                it[check] = r.check
                it[status] = r.status
                it[last] = r.last.toInstant()
            }
        }
    }
}

