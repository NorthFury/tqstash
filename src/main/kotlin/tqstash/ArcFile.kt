package tqstash

import jdk.internal.util.xml.impl.Input
import tqstash.io.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.channels.FileChannel

class ArcFile(private val file: File) {
    private val toc: Map<String, ArcRecord>

    init {
        val fileChannel = FileChannel.open(file.toPath())

        val header = readHeader(fileChannel)

        val tocSize = fileChannel.size().toInt() - header.tocOffset
        toc = readRecords(fileChannel, header, tocSize)
    }

    val fileNames by lazy { toc.keys }

    fun forEach(action: (fileName: String, inputStream: InputStream) -> Unit) {
        toc.values.forEach {
            action(it.fileName, it.inputStream())
        }
    }

    fun inputStream(name: String): InputStream? {
        val normalizedName = name.toLowerCase().replace('\\', '/')
        return toc[normalizedName]?.inputStream()
    }

    private fun ArcRecord.inputStream(): InputStream {
        val fileChannel = FileChannel.open(file.toPath())

        if (!compressed && compressedSize == realSize) {
            return fileChannel
                    .readOnlyMap(offset, realSize)
                    .inputStream()
        } else {
            val bytes = ByteArray(realSize)
            var offset = 0
            parts.forEach { part ->
                fileChannel.readOnlyMap(part.offset, part.compressedSize)
                        .inputStream()
                        .inflated()
                        .use {
                            var totalBytesRead = 0
                            do {
                                val bytesRead = it.read(bytes, offset, part.realSize - totalBytesRead)
                                offset += bytesRead
                                totalBytesRead += bytesRead
                            } while (totalBytesRead < part.realSize)
                        }
            }

            return ByteArrayInputStream(bytes)
        }
    }

    private fun readHeader(fileChannel: FileChannel): ArcHeader {
        return fileChannel
                .readOnlyMap(0, 28)
                .inputStream()
                .asTQStream()
                .use { input ->
                    input.skip(8)
                    val numEntries = input.readInt()
                    val numParts = input.readInt()
                    input.skip(8)
                    val tocOffset = input.readInt()

                    ArcHeader(
                            numEntries = numEntries,
                            numParts = numParts,
                            tocOffset = tocOffset
                    )
                }
    }

    private fun readRecords(fileChannel: FileChannel, header: ArcHeader, tocSize: Int): Map<String, ArcRecord> {
        return fileChannel.readOnlyMap(header.tocOffset, tocSize)
                .inputStream()
                .asTQStream()
                .use { input ->
                    val parts = Array(header.numParts) {
                        ArcPart(
                                offset = input.readInt(),
                                compressedSize = input.readInt(),
                                realSize = input.readInt()
                        )
                    }

                    val lastStringPosition = tocSize - 44 * header.numEntries
                    var position = header.numParts * 12
                    val fileNames = mutableListOf<String>()
                    while (position < lastStringPosition) {
                        val fileName = input.readNullTerminatedString()
                        position += fileName.length + 1
                        fileNames.add(fileName)
                    }

                    var fileNameIndex = 0
                    Array(header.numEntries) {
                        val compressed = input.readInt() > 1
                        val offset = input.readInt()
                        val compressedSize = input.readInt()
                        val realSize = input.readInt()
                        input.skip(12)
                        val numParts = input.readInt()
                        val firstPart = input.readInt()
                        input.skip(8)

                        val isActive = !compressed || numParts > 0
                        val recordParts = if (isActive) {
                            parts.sliceArray(firstPart until numParts + firstPart)
                        } else {
                            emptyArray<ArcPart>()
                        }

                        val fileName = if (isActive) fileNames[fileNameIndex++] else ""

                        fileName.toLowerCase() to ArcRecord(
                                fileName = fileName,
                                compressed = compressed,
                                offset = offset,
                                compressedSize = compressedSize,
                                realSize = realSize,
                                parts = recordParts
                        )
                    }.toMap()
                }
    }
}

private data class ArcHeader(val numEntries: Int, val numParts: Int, val tocOffset: Int)
private data class ArcPart(val offset: Int, val compressedSize: Int, val realSize: Int)
private data class ArcRecord(
        val fileName: String,
        val compressed: Boolean,
        val offset: Int,
        val compressedSize: Int,
        val realSize: Int,
        val parts: Array<ArcPart>
)
