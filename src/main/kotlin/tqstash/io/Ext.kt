package tqstash.io

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.zip.InflaterInputStream


fun FileChannel.readOnlyMap(): MappedByteBuffer =
        this.readOnlyMap(0, this.size())

fun FileChannel.readOnlyMap(offset: Int, size: Int): MappedByteBuffer =
        this.readOnlyMap(offset.toLong(), size.toLong())

fun FileChannel.readOnlyMap(offset: Long, size: Long): MappedByteBuffer =
        this.map(FileChannel.MapMode.READ_ONLY, offset, size)

fun ByteBuffer.inputStream(): InputStream =
        ByteBufferInputStream(this)

fun ByteBuffer.inputStream(offset: Int, size: Int): InputStream =
        PartialByteBufferInputStream(this, offset, size)

fun InputStream.buffered(buffer: ByteArray): InputStream =
        SimpleBufferedInputStream(this, buffer)

fun InputStream.inflated(): InputStream =
        InflaterInputStream(this)

fun InputStream.asTQStream(): TQInputStream =
        TQInputStream(this)
