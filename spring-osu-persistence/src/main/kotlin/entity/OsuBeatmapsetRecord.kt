package org.spring.osu.persistence.entity

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.spring.osu.RankStatus
import org.spring.osu.model.Beatmapset
import org.spring.osu.model.Covers
import org.spring.osu.persistence.OsuDatabases
import org.spring.osu.persistence.OsuDatabases.suspendTransaction

object OsuBeatmapsetRecord : IdTable<Long>("osu_beatmapset") {
    val beatmapsetID = long("id")
    val artist = text("artist")
    val artistUnicode = text("artist_unicode")
    val title = text("title")
    val titleUnicode = text("title_unicode")
    val creator = text("creator")
    val creatorID = long("creator_id")
    val nsfw = bool("nsfw")
    val offset = integer("offset")
    val previewUrl = text("preview_url")
    val sourceStr = text("source")
    val status = integer("status")
    val spotlight = bool("spotlight")
    val video = bool("video")
    val beatmaps = array<Long>("beatmaps").nullable()

    val cover = text("covers_cover")
    val cover2x = text("covers_cover2x")
    val coverCard = text("covers_card")
    val coverCard2x = text("covers_card2x")
    val coverList = text("covers_list")
    val coverList2x = text("covers_list2x")
    val coverSlim = text("covers_slim")
    val coverSlim2x = text("covers_slim2x")

    override val id = beatmapsetID.entityId()
    override val primaryKey = PrimaryKey(beatmapsetID)

    suspend fun save(data: Beatmapset) {
        val beatmapsList = if (data.beatmaps != null) {
            data.beatmaps?.forEach {
                OsuBeatmapRecord.save(it)
            }
            data.beatmaps!!.map { it.id }
        } else {
            null
        }
        suspendTransaction{
            upsert {
                it[beatmapsetID] = data.id
                it[artist] = data.artist
                it[artistUnicode] = data.artistUnicode
                it[title] = data.title
                it[titleUnicode] = data.titleUnicode
                it[creator] = data.creator
                it[creatorID] = data.userID
                it[nsfw] = data.nsfw
                it[offset] = data.offset
                it[previewUrl] = data.previewUrl
                it[sourceStr] = data.source
                it[status] = data.status.value
                it[spotlight] = data.spotlight
                it[video] = data.video
                it[cover] = data.covers.cover
                it[cover2x] = data.covers.cover2x
                it[coverCard] = data.covers.card
                it[coverCard2x] = data.covers.card2x
                it[coverList] = data.covers.list
                it[coverList2x] = data.covers.list2x
                it[coverSlim] = data.covers.slimcover
                it[coverSlim2x] = data.covers.slimcover2x
                beatmapsList?.apply {
                    it[beatmaps] = this
                }
            }
        }
    }

    suspend fun getByID(SID:Long, withBeatmaps:Boolean = false): Beatmapset? {
        val result: Beatmapset? = suspendTransaction {
            return@suspendTransaction selectAll()
                .where { beatmapsetID eq SID }
                .limit(1)
                .map(OsuBeatmapsetRecord::toBeatmapset)
                .firstOrNull()
        }
        if (withBeatmaps) result?.let {
            it.beatmaps = OsuBeatmapRecord.getBySID(result.id)
        }
        return result
    }

    private fun toBeatmapset(it: ResultRow): Beatmapset {
        val covers = Covers(
            cover = it[cover],
            cover2x = it[cover2x],
            card = it[coverCard],
            card2x = it[coverCard2x],
            list = it[coverList],
            list2x = it[coverList2x],
            slimcover = it[coverSlim],
            slimcover2x = it[coverSlim2x],
        )
        val statusVal = RankStatus.getStatus(it[status])
        return Beatmapset(
            artist = it[artist],
            artistUnicode = it[artistUnicode],
            title = it[title],
            titleUnicode = it[titleUnicode],
            creator = it[creator],
            userID = it[creatorID],
            nsfw = it[nsfw],
            offset = it[offset],
            previewUrl = it[previewUrl],
            source = it[sourceStr],
            status = statusVal,
            spotlight = it[spotlight],
            video = it[video],
            beatmaps = null,
            covers = covers,
            favouriteCount = 0,
            id = it[beatmapsetID],
            playCount = 0,
        )
    }

    init {
        OsuDatabases.registerTable(this)
    }
}