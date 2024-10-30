package org.spring.core

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.ref.Cleaner

class IOCopyStream {
    class InputCopyStream(
        val input: InputStream,
        val bufferSize: Int = 1024*8,
    ) : InputStream() {
        private val cl = cleaner.register(this) {
            input.close()
        }

        private val buffer = ByteArrayOutputStream()
        override fun read(): Int {
            return input.read().apply {
                buffer.write(this)
            }
        }

        override fun read(b: ByteArray): Int {
            return input.read(b).apply {
                buffer.write(b, 0, this)
            }
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            return input.read(b, off, len).apply {
                buffer.write(b, off, this)
            }
        }

        override fun readNBytes(len: Int): ByteArray {
            return input.readNBytes(len).apply {
                buffer.write(this)
            }
        }

        override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
            return input.readNBytes(b, off, len)
        }

        override fun close() {
            cl.clean()
        }
    }

    companion object {
        private val cleaner = Cleaner.create()
//        fun getCopyInput(input: InputStream): Pair<InputStream, InputStream> {
//            val copy = InputCopyStream(input)
//
//        }
    }
}
