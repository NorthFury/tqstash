package tqstash.io

import java.io.InputStream
import java.nio.ByteBuffer

class PartialByteBufferInputStream(private val buffer: ByteBuffer, private val offset: Int, private val length: Int) : InputStream() {
    private val limit = minOf(buffer.limit(), offset + length)
    init {
        buffer.position(offset)
    }

    private fun hasRemaining(): Boolean {
        return buffer.position() < limit
    }

    private fun remaining(): Int {
        return limit - buffer.position()
    }

    override fun read(): Int {
        if (!hasRemaining()) return -1

        return java.lang.Byte.toUnsignedInt(buffer.get())
    }

    override fun read(b: ByteArray): Int {
        return this.read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || b.size < off + len)
            throw IndexOutOfBoundsException()

        val remaining = remaining()
        if (remaining == 0) return -1

        val bytesToRead = minOf(remaining, len - off)

        for (i in off until bytesToRead)
            b[i] = buffer.get()

        return bytesToRead
    }

    override fun skip(n: Long): Long {
        if (n <= 0) return 0

        val validOffset = minOf(n.toInt(), remaining())
        val newPosition = buffer.position() + validOffset
        buffer.position(newPosition)

        return validOffset.toLong()
    }

    override fun available(): Int {
        return buffer.remaining()
    }

    override fun reset() {
        buffer.reset()
    }

    override fun mark(unused: Int) {
        buffer.mark()
    }

    override fun markSupported(): Boolean {
        return true
    }
}