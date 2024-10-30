package org.spring.osu.persistence.model

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.upsert
import org.spring.osu.OsuApi
import org.spring.osu.model.UserAuth
import org.spring.osu.persistence.OsuDatabases
import org.spring.osu.persistence.OsuDatabases.suspendTransaction

data class OsuAuthRecord(
    var uid: Long? = null,
    override var refreshToken: String? = null,
    override var accessToken: String? = null,
    override var expires: Long = 0,
) : UserAuth {
    override suspend fun update() {
        if (uid == null) {
            val userInfo = OsuApi.getOwnData(this)
            uid = userInfo.id
        }
        val r = this
        suspendTransaction {
            OsuAuthRecord.upsert {
                it[uid] = r.uid!!
                it[accessToken] = r.accessToken
                it[refreshToken] = r.refreshToken
                it[expires] = r.expires
            }
        }
    }

    companion object: IdTable<Long>("osu_user_auth") {
        val uid = long("uid")
        val refreshToken = text("refresh_token").nullable()
        val accessToken = text("access_token").nullable()
        val expires = long("expires_in")

        override val id = uid.entityId()
        override val primaryKey = PrimaryKey(uid)

        init {
            OsuDatabases.registerTable(this)
        }
    }
}