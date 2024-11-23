@file:Suppress("unused")

package org.spring.osu.extended.rosu

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
    @Suppress("LeakingThis")
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
        private const val libName = "spring_jni"
        private fun loadLib() {
            var isLibDir = false
            System.getenv("ROSU_LIB_PATH")?.let {
                val path = Path(it)
                if (Files.isRegularFile(path)) {
                    System.load(it)
                    return
                }
                if (Files.isDirectory(path)) {
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
