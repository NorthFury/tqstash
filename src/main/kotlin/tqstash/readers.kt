package tqstash

import tqstash.dto.Item
import tqstash.dto.Position
import tqstash.dto.Stash
import tqstash.io.TQInputStream
import java.io.File

fun readStash(file: File): Stash = TQInputStream(file.inputStream().buffered()).use { stream ->
    stream.skip(4)
    stream.readConstantString("begin_block")
    stream.skip(4)
    stream.readConstantString("stashVersion")
    val stashVersion = stream.readInt()
    stream.readConstantString("fName")
    val name = stream.readRawString()
    stream.readConstantString("sackWidth")
    val width = stream.readInt()
    stream.readConstantString("sackHeight")
    val height = stream.readInt()

    stream.readConstantString("numItems")
    val sackSize = stream.readInt()

    val items = 0.until(sackSize).map {
        val item = stream.readItem()
        stream.readConstantString("xOffset")
        val x = stream.readFloat().toInt()
        stream.readConstantString("yOffset")
        val y = stream.readFloat().toInt()

        item to Position(x, y)
    }

    stream.readConstantString("end_block")
    stream.skip(4)

    Stash(
            stashVersion = stashVersion,
            name = name,
            width = width,
            height = height,
            items = items
    )
}

private fun TQInputStream.readItem(): Item {
    val stream = this
    stream.readConstantString("stackCount")
    val stackCount = stream.readInt()
    stream.readConstantString("begin_block")
    stream.skip(4)
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
    val seed = stream.readInt()
    stream.readConstantString("var1")
    val var1 = stream.readInt()
    stream.readConstantString("end_block")
    stream.skip(4)

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

private fun TQInputStream.readConstantString(value: String) {
    val read = this.readCString()
    if (read != value) throw Exception("read: $read expected: $value")
}
