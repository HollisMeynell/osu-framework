package org.spring.web.databases

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.spring.osu.OsuApi
import org.spring.osu.persistence.OsuDatabases

class OsuAuth(
    var id: Long? = null,
    var name: String = "",
    override var accessToken: String? = null,
    override var refreshToken: String? = null,
    override var expires: Long = 0,
) : org.spring.osu.model.UserAuth {

    override suspend fun update() {
        if (id == null) {
            val userInfo = OsuApi.getOwnData(this)
            id = userInfo.id
            name = userInfo.username
        }
        save()
    }

    suspend fun save() {
        if (id == null) return
        val data = this
        OsuDatabases.suspendTransaction {
            upsert {
                it[id] = data.id!!
                it[name] = data.name
                it[accessToken] = data.accessToken ?: ""
                it[refreshToken] = data.refreshToken ?: ""
                it[expires] = data.expires
            }
        }
    }

    companion object : IdTable<Long>("osu_oauth") {
        val uid = long("osu_id")
        val name = text("username")
        val accessToken = text("access_token")
        val refreshToken = text("refresh_token")
        val expires = long("time")

        override val id = uid.entityId()
        override val primaryKey = PrimaryKey(uid)

        suspend fun getByID(id: Long): OsuAuth? {
            return OsuDatabases.suspendTransaction {
                selectAll()
                    .where { OsuAuth.id eq id }
                    .map { row ->
                        OsuAuth(
                            row[OsuAuth.id].value,
                            row[name],
                            row[accessToken],
                            row[refreshToken],
                            row[expires],
                        )
                    }
                    .firstOrNull()
            }
        }

        init {
            OsuDatabases.registerTable(this)
        }
    }
}