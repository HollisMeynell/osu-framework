package org.spring.osu.persistence.model

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.spring.osu.extended.api.OsuWebApi
import org.spring.osu.extended.model.OsuWebAccount
import org.spring.osu.persistence.OsuDatabases
import org.spring.osu.persistence.OsuDatabases.suspendTransaction

data class OsuWebUserRecord(
    override var session: String,
    override var csrf: String? = null,
    override var name: String? = null,
    override var userID: Long? = null,
) : OsuWebAccount {
    override suspend fun update() {
        val r = this
        if (userID == null) {
            val userData = OsuWebApi.visitHomePage(this) ?: throw IllegalStateException("Failed to get user data")
            userID = userData.id
        }
        suspendTransaction {
            OsuWebUserRecord.upsert {
                userID?.let { id -> it[uid] = id }
                it[session] = r.session
                it[csrf] = r.csrf
                it[name] = r.name
            }
        }
    }

    companion object : IdTable<Long>("osu_web_session") {
        val uid = long("uid").autoIncrement()
        val name = text("name").nullable()
        val csrf = text("csrf").nullable()
        val session = text("session")

        override val id = uid.entityId()
        override val primaryKey = PrimaryKey(uid)

        val random = object : Expression<OsuWebUserRecord>() {
            override fun toQueryBuilder(queryBuilder: QueryBuilder) {
                queryBuilder.append("random()")
            }
        }

        init {
            OsuDatabases.registerTable(this)
        }

        suspend fun getRandomRecord(): OsuWebAccount? {
            return suspendTransaction {
                val data = OsuWebUserRecord.selectAll()
                    .orderBy(random)
                    .limit(1)
                    .map {
                        OsuWebUserRecord(
                            it[session],
                            it[csrf],
                            it[name],
                            it[uid]
                        )
                    }
                return@suspendTransaction data.firstOrNull()
            }
        }
    }
}