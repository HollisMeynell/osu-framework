package org.spring.web.databases

import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import org.spring.osu.persistence.OsuDatabases.osuDB

object WebDataBase {
    lateinit var db: Database
    fun initDataBase(database: Database) {
        db = database
    }

    fun registerTable(table: Table) {
        transaction(osuDB) {
            SchemaUtils.createMissingTablesAndColumns(table)
        }
    }

    fun registerTable(table: Table, init: Transaction.() -> Unit) {
        transaction(osuDB) {
            SchemaUtils.createMissingTablesAndColumns(table)
            init()
        }
    }

    suspend inline fun <T> suspendTransaction(crossinline block: Transaction.() -> T): T = coroutineScope {
        if (osuDB == null) {
            throw IllegalStateException("Database not initialized")
        }

        suspendedTransactionAsync(coroutineContext, osuDB) {
            block()
        }.await()
    }
}