package org.spring.web.databases

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.spring.osu.persistence.OsuDatabases
import java.time.LocalDateTime

class FileRecord(
    var id: Int? = null,
    var localName: String = "",
    var fileName: String = "",
    var createTime: LocalDateTime = LocalDateTime.now(),
    var updateTime: LocalDateTime = LocalDateTime.now(),
) {
    companion object : IdTable<Int>("files") {
        val fileId = integer("id")
            .autoIncrement("files_id_seq")

        val localName = text("local")
            .index("local_name")

        val fileName = text("name")

        val createTime = datetime("create_from")

        val updateTime = datetime("update_from")

        override val id = fileId.entityId()
        override val primaryKey = PrimaryKey(id)

        suspend fun getFileRecordByName(name: String) = OsuDatabases.suspendTransaction {
            selectAll()
                .where { fileName eq name }
                .map { row ->
                    FileRecord(
                        row[fileId],
                        row[localName],
                        row[fileName],
                        row[createTime],
                        row[updateTime],
                    )
                }.firstOrNull()
        }

        suspend fun saveFileRecord(name: String, local: String): FileRecord {
            val fileRecord = FileRecord(
                localName = local,
                fileName = name,
            )
            val id = OsuDatabases.suspendTransaction {
                insertAndGetId {
                    it[localName] = fileRecord.localName
                    it[fileName] = fileRecord.fileName
                    it[createTime] = fileRecord.createTime
                    it[updateTime] = fileRecord.updateTime
                }
            }
            fileRecord.id = id.value
            return fileRecord
        }

        suspend fun getFileRecordBeforeUpdate(time: LocalDateTime) = OsuDatabases.suspendTransaction {
            selectAll()
                .where { updateTime less time }
                .map {
                    FileRecord(
                        it[fileId],
                        it[localName],
                        it[fileName],
                        it[createTime],
                        it[updateTime],
                    )
                }
        }

        suspend fun deleteByLocalName(local: String) = OsuDatabases.suspendTransaction {
            deleteWhere { localName eq local }
        }

        suspend fun deleteByLocalName(local: List<String>) = OsuDatabases.suspendTransaction {
            deleteWhere { localName inList local }
        }
    }
}