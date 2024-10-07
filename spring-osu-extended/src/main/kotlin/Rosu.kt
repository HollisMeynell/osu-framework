package org.spring.osu.extended

import org.spring.osu.OsuMod
import org.spring.osu.OsuMode
import org.spring.osu.plus
import java.nio.ByteBuffer
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

object Rosu {
    private const val libName = ""
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

sealed class JniParameter {
    abstract fun size(): Int
    abstract fun bytes(): ByteArray
    protected fun buffer(action: ByteBuffer.() -> Unit): ByteArray {
        val buffer = ByteBuffer.allocate(size())
        buffer.action()
        return buffer.array()
    }
}

data class JniMapConfig(
    var mode: OsuMode = OsuMode.Default,
    var mods: Int = 0,
    var speed: Double = -1.0,
    var accuracy: Double = 0.0,
) : JniParameter() {
    fun setMods(mods: Iterable<OsuMod>) {
        mods.forEach {
            this.mods += it
        }
    }
    override fun size() = 1 + 4 + 8 + 8
    override fun bytes() = buffer {
        put(mode.value.toByte())
        putInt(mods)
        putDouble(speed)
        putDouble(accuracy)
    }
}