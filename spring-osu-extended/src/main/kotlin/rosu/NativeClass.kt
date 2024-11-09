package org.spring.osu.extended.rosu

import org.spring.osu.OsuMode
import java.lang.ref.Cleaner
import kotlin.io.path.Path


abstract class NativeClass(
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

    protected fun ready(): Boolean {
        return _ptr != 0L
    }

    override fun close() {
        _c.clean()
    }

    companion object {
        private val cleaner = Cleaner.create(Thread.ofVirtual().factory())

        @JvmStatic
        @JvmName("release")
        private external fun release(ptr: Long, type: Byte)
    }
}

fun main() {
    System.load("/home/spring/IdeaProjects/osu-framework/spring-osu-extended/native/target/debug/libspring_jni.so")
    a1()
}

fun a1() {
    val b =
        JniBeatmap(Path("/home/spring/Documents/match/osu/1456709/Kano - Stella-rium (Asterisk MAKINA Remix) (Vaporfly) [Starlight].osu"))
    val b2 =
        JniBeatmap(Path("/home/spring/Documents/match/osu/1456709/Kano - Stella-rium (Asterisk MAKINA Remix) (Vaporfly) [Starlight].osu"))
    b2.close()

    b.use {
        b.convertInPlace(OsuMode.Taiko)
        val d = JniDifficulty()
        d.setAr(2f, false)

        val c = d.calculate(it)
        if (c is OsuDifficultyAttributes) {
            println(c.stars)
            println(c.speed)
        }

        if (c is ManiaDifficultyAttributes) {
            println(c.stars)
            println(c.isConvert)
        }
    }
}