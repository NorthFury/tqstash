package tqstash.dto

import java.util.Arrays

data class Stash(
        val stashVersion: Int,
        val name: ByteArray,
        val width: Int,
        val height: Int,
        val items: List<Pair<Item, Position>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stash

        if (stashVersion != other.stashVersion) return false
        if (!Arrays.equals(name, other.name)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (items != other.items) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stashVersion
        result = 31 * result + Arrays.hashCode(name)
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + items.hashCode()
        return result
    }
}

fun Stash.createBackupStash(): Stash {
    val backupName = this.name.clone()
    backupName[backupName.lastIndex] = 'g'.toByte()
    return this.copy(name = backupName)
}
