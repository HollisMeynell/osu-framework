package org.spring.osu.persistence

import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction

object OsuDatabases {
    fun initDataBase(db: Database) {
        osuDB = db
    }

    var osuDB: Database? = null
        private set

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