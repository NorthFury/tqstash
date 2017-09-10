package tqstash

import tqstash.dto.Item
import tqstash.dto.Position
import tqstash.dto.Stash
import java.io.DataInput
import java.io.DataInputStream
import java.io.File
import java.nio.charset.Charset

fun readStash(file: File): Stash = DataInputStream(file.inputStream().buffered()).use { stream ->
    stream.skip(4)
    stream.readConstantString("begin_block")
    stream.skipBytes(4)
    stream.readConstantString("stashVersion")
    val stashVersion = stream.readIntLE()
    stream.readConstantString("fName")
    val name = stream.readRawString()
    stream.readConstantString("sackWidth")
    val width = stream.readIntLE()
    stream.readConstantString("sackHeight")
    val height = stream.readIntLE()

    stream.readConstantString("numItems")
    val sackSize = stream.readIntLE()

    val items = 0.until(sackSize).map {
        val item = stream.readItem()
        stream.readConstantString("xOffset")
        val x = stream.readFloatLE().toInt()
        stream.readConstantString("yOffset")
        val y = stream.readFloatLE().toInt()

        item to Position(x, y)
    }

    stream.readConstantString("end_block")
    stream.skipBytes(4)

    Stash(
            stashVersion = stashVersion,
            name = name,
            width = width,
            height = height,
            items = items
    )
}

private fun DataInput.readItem(): Item {
    val stream = this
    stream.readConstantString("stackCount")
    val stackCount = stream.readIntLE()
    stream.readConstantString("begin_block")
    stream.skipBytes(4)
    stream.readConstantString("baseName")
    val baseItemId = stream.readCString()
    stream.readConstantString("prefixName")
    val prefixId = stream.readCString()
    stream.readConstantString("suffixName")
    val suffixId = stream.readCString()
    stream.readConstantString("relicName")
    val relicId = stream.readCString()
    stream.readConstantString("relicBonus")
    val relicBonusId = stream.readCString()
    stream.readConstantString("seed")
    val seed = stream.readIntLE()
    stream.readConstantString("var1")
    val var1 = stream.readIntLE()
    stream.readConstantString("end_block")
    stream.skipBytes(4)

    return Item(
            stackCount = stackCount,
            baseItemId = baseItemId,
            prefixId = prefixId,
            suffixId = suffixId,
            relicId = relicId,
            relicBonusId = relicBonusId,
            seed = seed,
            var1 = var1
    )
}

private fun DataInput.readConstantString(value: String) {
    val read = this.readCString()
    if (read != value) throw Exception("read: $read expected: $value")
}

private fun DataInput.readCString(): String {
    return this.readRawString().toString(Charset.defaultCharset())
}

private fun DataInput.readRawString(): ByteArray {
    val length = this.readIntLE()
    val stringBytes = ByteArray(length)
    this.readFully(stringBytes)
    return stringBytes
}

private fun DataInput.readFloatLE(): Float {
    return java.lang.Float.intBitsToFloat(this.readIntLE())
}

private fun DataInput.readIntLE(): Int {
    return Integer.reverseBytes(this.readInt())
}
