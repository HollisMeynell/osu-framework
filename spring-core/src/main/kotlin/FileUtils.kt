package org.spring.core

import java.nio.file.Files
import java.nio.file.Path

object FileUtils {
    fun createDirectory(pathString: String): Path {
        if (pathString.isBlank()) {
            throw IllegalArgumentException("Base path not set")
        }
        val path = Path.of(pathString)
        if (!Files.isDirectory(path)) {
            if (Files.exists(path)) {
                throw IllegalArgumentException("Path is not a directory")
            }
            Files.createDirectories(path)
        }
        return path
    }
}