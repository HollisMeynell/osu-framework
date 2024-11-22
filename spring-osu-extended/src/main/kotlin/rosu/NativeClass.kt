@file:Suppress("unused")
package org.spring.osu.extended.rosu

import org.spring.osu.model.OsuMod
import java.lang.ref.Cleaner
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories


sealed class NativeClass(
    type: Byte
) : AutoCloseable {
    private val _type: Byte = type
    private val _c = cleaner.register(this, this::release)
    private var _ptr: Long = 0

    protected fun release() {
        if (_ptr != 0L) {
            release(_ptr, _type)
            _ptr = 0
        }
    }

    internal fun ready(): Boolean {
        return _ptr != 0L
    }

    override fun close() {
        _c.clean()
    }

    internal fun getPtr() = _ptr

    companion object {
        const val libName = "spring_jni"
        private fun loadLib() {
            var isLibDir = false
            System.getenv("ROSU_LIB_PATH")?.let {
                val path = Path(it)
                if (Files.isRegularFile(path)) {
                    System.load(it)
                    return
                }
                if (Files.isDirectory(path)){
                    isLibDir = true
                }
            }
            val os: String = System.getProperty("os.name")
            val name = when {
                os.contains("windows", ignoreCase = true) -> "$libName.dll"
                os.contains("mac", ignoreCase = true) -> "lib$libName.dylib"
                os.contains("linux", ignoreCase = true) -> "lib$libName.so"
                else -> throw Error("Unsupported OS")
            }
            val lib = NativeClass::class.java.getResourceAsStream("/lib/${name}")
            lib?.use {
                val tmpDirPath = if (isLibDir)
                    Path.of(System.getenv("ROSU_LIB_PATH"))
                else
                    Path.of(System.getProperty("java.io.tmpdir"), "/rosulib")
                tmpDirPath.createDirectories()
                val f = tmpDirPath.resolve(name)
                Files.copy(it, f, StandardCopyOption.REPLACE_EXISTING)
                f.toFile().deleteOnExit()
                System.load(f.absolutePathString())
            }
        }

        init {
            loadLib()
        }

        private val cleaner = Cleaner.create(Thread.ofVirtual().factory())

        @JvmStatic
        @JvmName("release")
        private external fun release(ptr: Long, type: Byte)
    }
}

fun main() {
    val beatmap = JniBeatmap(Files.readAllBytes(Path("/home/spring/Documents/match/osu/1456709/Kano - Stella-rium (Asterisk MAKINA Remix) (Vaporfly) [Starlight].osu")))
    val difficulty = beatmap.createDifficulty()
    difficulty.setMods(OsuMod.DoubleTime, OsuMod.HardRock)
    val attr = difficulty.calculate(beatmap)
    println(attr.getStarRating())

    val performance1 = beatmap.createPerformance()
    val performance2 = attr.createPerformance()

    performance1.setDifficulty(difficulty)
    val pp1 = performance1.calculate()

    performance2.setMods("[{\"acronym\": \"DT\"}]")
    val pp2 = performance2.calculate()

    println(pp1.getPP())
    println(pp2.getPP())

    try {

    }finally {
        performance1.close()
        performance2.close()
        difficulty.close()
        beatmap.close()
    }
}