package org.spring.web.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.spring.core.FileUtils
import org.spring.web.WebConfig
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*

object LocalFIleService {
    private val UPLOAD_PATH by lazy {
        val localPathStr = WebConfig.Instance.server.fileDirectory
        FileUtils.createDirectory(localPathStr)
    }

    suspend fun writeFile(name: String, data: ByteArray) {
        val key = UUID.randomUUID().toString()
        val path = UPLOAD_PATH.resolve(key)
        withContext(Dispatchers.IO) {
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        }
    }
}