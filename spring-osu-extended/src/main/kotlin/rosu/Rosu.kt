package org.spring.osu.extended.rosu

import org.spring.osu.OsuMod
import org.spring.osu.OsuMode
import org.spring.osu.plus
import java.nio.ByteBuffer
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

object Rosu {
    private const val libName = "spring_jni"
    private const val libEnvKey = "ROSU_LIB"
    val load: Unit by lazy {
        val libPath = System.getenv(libEnvKey)
        if (libPath != null) {
            System.load(libPath)
            return@lazy
        }

        val os = System.getProperty("os.name")
        val target = when {
            os.contains("windows", ignoreCase = true) -> "$libName.dll"
            os.contains("linux", ignoreCase = true) -> "lib$libName.so"
            os.contains("mac", ignoreCase = true) -> "lib$libName.dylib"
            else -> throw Error("Unsupported OS")
        }
        val lib = Rosu::class.java.getResourceAsStream("/lib/$target") ?: throw Error("Cannot support native library")
        lib.use {
            val tmpDirPath = Path(System.getProperty("java.io.tmpdir"), "rosu-lib")
            tmpDirPath.createDirectories()
            val filePath = tmpDirPath.resolve(target)
            Files.copy(it, filePath)
            filePath.toFile().deleteOnExit()
            System.load(filePath.absolutePathString())
        }
        return@lazy
    }
}
