package org.spring.web.databases

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import org.spring.osu.persistence.OsuDatabases.osuDB
import org.spring.web.WebConfig

object WebDataBase {
    lateinit var db: Database
    fun initDataBase(config: WebConfig.DatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            driverClassName = config.driver
            username = config.username
            password = config.password
        }
        db = Database.connect(HikariDataSource(hikariConfig))

    }

    fun registerTable(table: Table) {
        transaction(osuDB) {
            SchemaUtils.create(table)
        }
    }

    suspend inline fun <T> suspendTransaction(crossinline block: Transaction.() -> T): T = coroutineScope {
        if (osuDB == null) {
            throw IllegalStateException("Database not initialized")
        }

        suspendedTransactionAsync(coroutineContext, osuDB){
            block()
        }.await()
    }
}