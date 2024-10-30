package org.spring.osu.extended

import org.spring.osu.OsuRuleset
import org.tukaani.xz.LZMAInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant

class Replay(input: InputStream) {
    private val cache = ByteBuffer.allocate(8)
    var mode: OsuRuleset
    var version: Int
    var beatmapMD5: String
    var player: String
    var replayMD5: String
    var n300: Short
    var n100: Short
    var n50: Short
    var nGeki: Short
    var nKatu: Short
    var nMiss: Short
    var score: Int
    var maxCombo: Short
    var perfect: Boolean
    var mods: Int
    var hp: List<Pair<Int, Float>>
    var date: Instant
    // fixme: 丢弃前面 0 -500 的数据, 第一个数据是基于红线的时间
    var hits: List<Hit>
    var scoreID: Long

    init {
        cache.order(ByteOrder.LITTLE_ENDIAN)
        mode = OsuRuleset.getMode(input.read())
        version = input.int()
        beatmapMD5 = input.string()
        player = input.string()
        replayMD5 = input.string()
        n300 = input.short()
        n100 = input.short()
        n50 = input.short()
        nGeki = input.short()
        nKatu = input.short()
        nMiss = input.short()
        score = input.int()
        maxCombo = input.short()
        perfect = input.read() == 1
        mods = input.int()
        hp = input.readHP()
        date = Instant.ofEpochMilli((input.long() - 621355968000000000) / 10000)
        hits = input.readHits()
        scoreID = input.long()
        input.close()
    }

    data class Hit(
        var per: Int,
        // 0 - 512
        var x: Float,
        // 0 - 384
        var y: Float,
        // M1 = 1, M2 = 2, K1 = 4, K2 = 8, c = 16
        var key: Int,
    ) {
        constructor(s: String) : this(0, 0f, 0f, 0) {
            val data = s.split("|")
            per = data[0].toInt()
            x = data[1].toFloat()
            y = data[2].toFloat()
            key = data[3].toInt()
        }

        fun check(): Boolean {
            if (x == 256f && y == -500f) return false
            else if (per == -12345) return false
            return true
        }
    }

    private fun InputStream.cache(n: Int): ByteBuffer {
        cache.clear()
        val data = readNBytes(n)
        cache.put(data)
        cache.position(0)
        return cache
    }

    private fun InputStream.short() = cache(2).short

    private fun InputStream.int() = cache(4).int

    private fun InputStream.long() = cache(8).long

    private fun InputStream.length(): Int {
        var result = 0
        var shift = 0
        var b: Int
        do {
            b = read()
            result = result or ((b and 0x7f) shl shift)
            shift += 7
        } while ((b and 0x80) != 0)
        return result
    }

    private fun InputStream.string(): String {
        if (read() != 11) return ""
        val length = length()
        if (length == 0) return ""
        return readNBytes(length).toString(Charsets.UTF_8)
    }

    private fun InputStream.readHP(): List<Pair<Int, Float>> {
        val str = string()
        val list = str.split(",").mapNotNull {
            if (it.isEmpty()) return@mapNotNull null
            val data = it.split("|")
            return@mapNotNull try {
                Pair(data[0].toInt(), data[1].toFloat())
            } catch (e: Exception) {
                null
            }
        }
        return list
    }

    private fun InputStream.readHits(): List<Hit> {
        val len = int()
        val subInput = object : InputStream() {
            var l = 0
            val i = this@readHits
            override fun read(): Int {
                return if (l < len) {
                    l++
                    i.read()
                } else {
                    -1
                }
            }
            override fun close() {}
        }
        val hits = mutableListOf<Hit>()
        val lzma = LZMAInputStream(subInput).bufferedReader(Charsets.UTF_8)
        val buffer = StringBuilder()
        var data: Int
        while (lzma.read().also { data = it } != -1) {
            if (data.toChar() == ',') {
                val s = buffer.toString()
                val hit = Hit(s)
                if (hit.check()) hits += hit
                buffer.clear()
            } else {
                buffer.append(data.toChar())
            }
        }
        lzma.close()
        return hits
    }
}