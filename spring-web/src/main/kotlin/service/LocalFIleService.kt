package org.spring.web.service

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

object LocalFIleService {
    private val UPLOAD_PATH by lazy {
        ""
    }

    suspend fun writeFile(name: String, data: ByteArray) {
        val key = UUID.randomUUID().toString()
        val path = Paths.get(UPLOAD_PATH, key)
        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
    }
}