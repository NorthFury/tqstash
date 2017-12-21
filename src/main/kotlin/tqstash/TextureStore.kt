package tqstash

import net.npe.dds.DDSReader
import tqstash.io.asTQStream
import java.awt.image.BufferedImage
import java.io.File

class TextureStore(itemsFile: File, expansionItemsFile: File) {
    private val itemsArc = ArcFile(itemsFile)
    private val expansionItemsArc = ArcFile(expansionItemsFile)

    fun getImage(id: String): BufferedImage? {
        val (normalizedId, arcFile) = if (id.startsWith("XPack", true)) {
            id.substring(12) to expansionItemsArc
        } else {
            id.substring(6) to itemsArc
        }

        return arcFile.inputStream(normalizedId)?.asTQStream()?.use { input ->
            input.skip(4)
            val offset = input.readInt()
            val size = input.readInt()

            input.skip(offset.toLong())
            // first 4 bytes should be should be 0x52534444 or 0x20534444

            val ddsData = ByteArray(size)
            var bytesRead = 0
            do {
                bytesRead += input.read(ddsData, bytesRead, size - bytesRead)
            } while (bytesRead < size)

            fixDdsData(ddsData)

            ddsToBufferedImage(ddsData)
        }
    }
}

private fun ddsToBufferedImage(ddsData: ByteArray): BufferedImage {
    fixDdsData(ddsData)

    val pixels = DDSReader.read(ddsData, DDSReader.ARGB, 0)
    val width = DDSReader.getWidth(ddsData)
    val height = DDSReader.getHeight(ddsData)
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, width, height, pixels, 0, width)

    return image
}

private fun fixDdsData(ddsData: ByteArray) {
    // Change "DDSR" to "DDS "
    ddsData[3] = 0x20

    val bitDepth = intFromBytes(ddsData[88], ddsData[89], ddsData[90], ddsData[91])
    if (bitDepth >= 24) {
        // Set the Red pixel mask
        ddsData[92] = 0
        ddsData[93] = 0
        ddsData[94] = 0xff.toByte()
        ddsData[95] = 0

        // Set the Green pixel mask
        ddsData[96] = 0
        ddsData[97] = 0xff.toByte()
        ddsData[98] = 0
        ddsData[99] = 0

        // Set the Blue pixel mask
        ddsData[100] = 0xff.toByte()
        ddsData[101] = 0
        ddsData[102] = 0
        ddsData[103] = 0

        // HACK: Fix to make 32-bit DDS files use transparency.
        if (bitDepth == 32) {
            // Add the alpha bit to the pixel format.
            ddsData[80] = (ddsData[80].toInt() or 1).toByte()

            // Set the Alpha pixel mask
            ddsData[104] = 0
            ddsData[105] = 0
            ddsData[106] = 0
            ddsData[107] = 0xff.toByte()
        }
    }

    // Set the DDS caps flag
    ddsData[109] = (ddsData[109].toInt() or 0x10).toByte()
}

private fun intFromBytes(b4: Byte, b3: Byte, b2: Byte, b1: Byte) =
        b1.toInt() shl 24 or (b2.toInt() and 0xFF shl 16) or (b3.toInt() and 0xFF shl 8) or (b4.toInt() and 0xFF)
