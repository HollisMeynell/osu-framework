package org.spring.web.entity

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.spring.core.getContext
import org.spring.core.setContext
import org.spring.osu.OsuApi
import org.spring.osu.model.User
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
            setContext(CONTEXT_KEY, userInfo)
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
        @JvmStatic
        val CONTEXT_KEY = "osu_oauth_get_user_info"

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

        fun getContextUser(): User? = getContext(CONTEXT_KEY)
    }
}