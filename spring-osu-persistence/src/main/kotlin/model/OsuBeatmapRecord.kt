package org.spring.osu.persistence.model

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.spring.osu.OsuMode
import org.spring.osu.RankStatus
import org.spring.osu.model.Beatmap
import org.spring.osu.persistence.OsuDatabases
import org.spring.osu.persistence.OsuDatabases.suspendTransaction
import org.spring.osu.persistence.model.OsuAuthRecord.Companion.uid
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object OsuBeatmapRecord : IdTable<Long>("osu_beatmap") {
    val beatmapID = long("id")
    val beatmapsetID = long("beatmapset_id")
    val mapperID = long("mapper_id")
    val version = text("version")
    val difficulty = float("difficulty")
    val mode = integer("mode")
    val status = integer("status")
    val chechsum = text("checksum")
    val maxCombo = integer("max_combo")
    val od = float("od")
    val ar = float("ar")
    val hp = float("hp")
    val cs = float("cs")
    val bpm = float("bpm")
    val totalLength = integer("total_length")
    val hitLength = integer("hit_length")
    val countCircles = integer("count_circles")
    val countSliders = integer("count_sliders")
    val countSpinners = integer("count_spinners")
    val isScoreable = bool("is_scoreable").default(false)
    val lastUpdate = timestamp("last_update")

    override val id = beatmapID.entityId()
    override val primaryKey = PrimaryKey(beatmapID)

    suspend fun save(data: Beatmap) {
        suspendTransaction {
            upsert {
                it[id] = data.id
                it[beatmapsetID] = data.beatmapsetID
                it[mapperID] = data.userID
                it[version] = data.version
                it[difficulty] = data.difficultyRating
                it[mode] = data.mode.value
                it[status] = data.status.value
                it[chechsum] = data.checksum ?: ""
                it[maxCombo] = data.maxCombo ?: 0
                it[od] = data.accuracy
                it[ar] = data.ar
                it[hp] = data.drain ?: 0f
                it[cs] = data.cs ?: 0f
                it[bpm] = data.bpm ?: 0f
                it[totalLength] = data.totalLength
                it[hitLength] = data.hitLength ?: 0
                it[countCircles] = data.countCircles ?: 0
                it[countSliders] = data.countSliders ?: 0
                it[countSpinners] = data.countSpinners ?: 0
                it[isScoreable] = data.isScoreable == true
                it[lastUpdate] = data.lastUpdated?.toInstant() ?: Instant.ofEpochMilli(0)
            }
        }
    }

    suspend fun getByID(BID: Long): Beatmap? {
        return suspendTransaction {
            selectAll()
                .where { beatmapID eq BID }
                .limit(1)
                .map(::toBeatmap)
                .firstOrNull()
        }
    }

    suspend fun getBySID(SID: Long): List<Beatmap> {
        return suspendTransaction {
            selectAll()
                .where { beatmapsetID eq SID }
                .map(::toBeatmap)
        }
    }

    private fun toBeatmap(it: ResultRow): Beatmap {
        val statusVal = RankStatus.getStatus(it[status])
        val modeVal = OsuMode.getMode(it[mode])
        return Beatmap(
            id = it[beatmapID],
            beatmapsetID = it[beatmapsetID],
            userID = it[mapperID],
            version = it[version],
            difficultyRating = it[difficulty],
            mode = modeVal,
            status = statusVal,
            checksum = it[chechsum],
            maxCombo = it[maxCombo],
            accuracy = it[od],
            ar = it[ar],
            drain = it[hp],
            cs = it[cs],
            bpm = it[bpm],
            totalLength = it[totalLength],
            hitLength = it[hitLength],
            countCircles = it[countCircles],
            countSliders = it[countSliders],
            countSpinners = it[countSpinners],
            isScoreable = it[isScoreable],
            lastUpdated = ZonedDateTime.ofInstant(it[lastUpdate], ZoneOffset.systemDefault()).toOffsetDateTime(),
            convert = null,
            deletedAt = null,
            modeInt = modeVal.value,
            passCount = null,
            playCount = null,
            ranked = statusVal,
            url = null,
        )
    }

    init {
        OsuDatabases.registerTable(this)
    }
}