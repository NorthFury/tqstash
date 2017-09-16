package tqstash.io

import java.io.FilterOutputStream
import java.io.OutputStream

class TQOutputStream(out: OutputStream) : FilterOutputStream(out) {

    fun writeInt(v: Int) {
        out.write(0xFF and v)
        out.write(0xFF and (v shr 8))
        out.write(0xFF and (v shr 16))
        out.write(0xFF and (v shr 24))
    }

    fun writeFloat(v: Float) {
        writeInt(java.lang.Float.floatToIntBits(v))
    }

    fun writeCString(s: String) {
        val length = s.length
        writeInt(length)
        for (i in 0 until length) {
            out.write(s[i].toInt())
        }
    }

}
