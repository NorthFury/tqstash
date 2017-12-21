package tqstash.io

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class SimpleBufferedInputStream(`in`: InputStream, private val buf: ByteArray) : FilterInputStream(`in`) {

    private var count: Int = 0
    private var pos: Int = 0

    private inline val inIfOpen: InputStream
        get() = `in` ?: throw IOException("Stream closed")


    private fun fill() {
        pos = 0
        count = 0
        val n = inIfOpen.read(buf, pos, buf.size - pos)
        if (n > 0)
            count = n + pos
    }

    override fun read(): Int {
        if (pos >= count) {
            fill()
            if (pos >= count)
                return -1
        }
        return buf[pos++].toInt()
    }

    private fun read1(b: ByteArray, off: Int, len: Int): Int {
        var avail = count - pos
        if (avail <= 0) {
            if (len >= buf.size) {
                return inIfOpen.read(b, off, len)
            }
            fill()
            avail = count - pos
            if (avail <= 0) return -1
        }
        val cnt = if (avail < len) avail else len
        System.arraycopy(buf, pos, b, off, cnt)
        pos += cnt
        return cnt
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        // Check for closed stream
        if (off or len or off + len or b.size - (off + len) < 0) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }

        var n = 0
        while (true) {
            val nread = read1(b, off + n, len - n)
            if (nread <= 0)
                return if (n == 0) nread else n
            n += nread
            if (n >= len)
                return n
            // if not closed but no bytes available, return
            val input = `in`
            if (input != null && input.available() <= 0)
                return n
        }
    }

    override fun skip(n: Long): Long {
        // Check for closed stream
        if (n <= 0) {
            return 0
        }
        val avail = (count - pos).toLong()

        if (avail <= 0) {
            return inIfOpen.skip(n)
        }

        val skipped = if (avail < n) avail else n
        pos += skipped.toInt()
        return skipped
    }

    override fun available(): Int {
        val n = count - pos
        val avail = inIfOpen.available()
        return if (n > Integer.MAX_VALUE - avail)
            Integer.MAX_VALUE
        else
            n + avail
    }

    override fun mark(readlimit: Int) {
        throw Exception("Operation not supported.")
    }

    override fun reset() {
        throw Exception("Operation not supported.")
    }

    override fun markSupported(): Boolean {
        return false
    }

    override fun close() {
        val input = `in`
        `in` = null
        input?.close()
    }
}
