package tqstash.io

import java.io.EOFException
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.Charset

class TQInputStream(input: InputStream) : FilterInputStream(input) {
    private val readBuffer = ByteArray(8)

    fun readShortInt(): Int {
        readFully(readBuffer, 2)
        val b3 = readBuffer[1].toInt()
        val b4 = readBuffer[0].toInt()
        return (b3 and 0xFF shl 8) or (b4 and 0xFF)
    }

    fun readInt(): Int {
        readFully(readBuffer, 4)
        val b1 = readBuffer[3].toInt()
        val b2 = readBuffer[2].toInt()
        val b3 = readBuffer[1].toInt()
        val b4 = readBuffer[0].toInt()
        return b1 shl 24 or (b2 and 0xFF shl 16) or (b3 and 0xFF shl 8) or (b4 and 0xFF)
    }

    fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    fun readCString(): String {
        return readRawString().toString(Charset.defaultCharset())
    }

    fun readRawString(): ByteArray {
        val length = this.readInt()
        val stringBytes = ByteArray(length)
        readFully(stringBytes, length)
        return stringBytes
    }

    private fun readFully(b: ByteArray, len: Int) {
        var total = 0
        while (total < len) {
            val result = `in`.read(b, total, len - total)
            if (result == -1) {
                break
            }
            total += result
        }

        if (total != len) throw EOFException()
    }
}
