package tqstash

import tqstash.io.*
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ItemDataStore(file: File) {
    private val strings: Array<String>
    private val dataMap: Map<String, Map<String, DataEntry>>

    init {
        val mappedByteBuffer = FileChannel.open(file.toPath()).readOnlyMap()

        val arzHeader = readArzHeader(mappedByteBuffer)

        strings = readStrings(mappedByteBuffer, arzHeader)
        dataMap = readDataSets(mappedByteBuffer, arzHeader)
    }

    fun print() {
//        dataMap.values.asSequence()
//                .flatMap { it.keys.asSequence().filter { it.startsWith("defensive") } }
//                .distinct()
//                .sorted()
//                .forEach(::println)
//        dataMap.values.asSequence()
//                .flatMap {
//                    it.asSequence()
//                            .filter { it.key == "itemClassification" }
//                            .mapNotNull { it.value as? SingleStringDataEntry }
//                            .map { it.value }
//                }
//                .distinct()
//                .sorted()
//                .forEach(::println)

//        dataMap
//                .filter { it.value.values.any { it.key == "Class" && it is SingleStringDataEntry && it.value == "ItemEquipment" } }
//                .toList()
//                .take(10)
//                .forEach { (key, entries) ->
//                    println(key)
//                    entries.values
//                            .sortedWith(compareBy({ it.javaClass.canonicalName }, { it.key }))
//                            .forEach(::println)
//                }
    }

    private fun isUseful(key: String): Boolean {
        val matches = listOf(
                // bitmapId
                "bitmap", "bitmapFemale", "relicBitmap", "shardBitmap", "artifactFormulaBitmapName", "artifactBitmap",
                // Name display properties
                "hidePrefixName", "hideSuffixName",
                // relic valid target
                "shield",
                "amulet", "ring",
                "armband", "bracelet", "bodyArmor", "greaves", "helmet",
                "axe", "bow", "mace", "rangedOneHand", "spear", "staff", "sword",
                // quest, accompanied by cannotPickUpMultiple
                "quest"
        )
        val prefixes = listOf("character", "defensive", "offensive", "retaliation", "racialBonus", "augment")

        return matches.contains(key) || prefixes.any { key.startsWith(it) }
    }

    fun get(id: String): Map<String, DataEntry> {
        return dataMap.getOrDefault(id.toLowerCase(), emptyMap())
    }

    private fun readDataSets(fullFileByteBuffer: MappedByteBuffer, arzHeader: ArzHeader): Map<String, Map<String, DataEntry>> {
        val archives = readArchives(fullFileByteBuffer, arzHeader)

        val result = hashMapOf<String, Map<String, DataEntry>>()
//        println(">>>>>>>>>>>>>>>>>>>>>")
//        archives.filter { it.id.equals("Records\\game\\itemcost_lightarmor.dbr", true) }.forEach(::println)
//        println(">>>>>>>>>>>>>>>>>>>>>")

        val buffer = ByteArray(64 * 1024)
        archives.filter { it.type.isBlank() || it.type in ITEM_DATA_TYPES_TO_PARSE }.map { archive ->
            fullFileByteBuffer.inputStream(archive.offset, archive.size)
                    .inflated()
                    .buffered(buffer)
                    .asTQStream()
                    .use { input ->
                        val entries = hashMapOf<String, DataEntry>()

                        while (true) {
                            val b1 = input.read()
                            if (b1 == -1) { // only way to detect EOF without exceptions
                                break
                            }
                            val b2 = input.read()
                            val dataType = (b2 and 0xFF shl 8) or (b1 and 0xFF)
                            val valuesCount = input.readShortInt()
                            val keyIndex = input.readInt()
                            val key = strings[keyIndex]

                            if (BLACKLISTED_VALUES.contains(key)) {
                                val toSkip = valuesCount * 4L
                                var skipped = 0L
                                do {
                                    skipped += input.skip(toSkip - skipped)
                                } while (skipped < toSkip)
                            } else {
                                val dataEntry = input.readDataEntry(dataType, valuesCount, key)
                                if (dataEntry != null) entries.put(key, dataEntry)
                            }
                        }

                        DataSet(
                                id = archive.id,
                                type = archive.type,
                                entries = entries
                        )

                        result.put(archive.id.toLowerCase(), entries)
                    }
        }

        return result
    }

    private fun readArchives(fullFileByteBuffer: MappedByteBuffer, arzHeader: ArzHeader): Array<Archive> {
        return fullFileByteBuffer
                .inputStream(arzHeader.archivesOffset, arzHeader.archivesSize)
                .asTQStream()
                .use { input ->
                    Array(arzHeader.archivesCount) {
                        val idIndex = input.readInt()
                        val type = input.readCString()
                        val offset = input.readInt()
                        val size = input.readInt()
                        input.skip(8)
                        Archive(
                                strings[idIndex],
                                type,
                                offset + 24,
                                size
                        )
                    }
                }
    }

    private fun readStrings(fullFileByteBuffer: MappedByteBuffer, arzHeader: ArzHeader): Array<String> {
        return fullFileByteBuffer
                .inputStream(arzHeader.stringsOffset, arzHeader.stringsSize)
                .asTQStream()
                .use { input ->
                    Array(input.readInt()) {
                        input.readCString()
                    }
                }
    }

    private fun readArzHeader(fullFileByteBuffer: MappedByteBuffer): ArzHeader {
        return fullFileByteBuffer
                .inputStream(0, 24)
                .asTQStream()
                .use { input ->
                    input.skip(4)
                    ArzHeader(
                            archivesOffset = input.readInt(),
                            archivesSize = input.readInt(),
                            archivesCount = input.readInt(),
                            stringsOffset = input.readInt(),
                            stringsSize = input.readInt()
                    )
                }
    }

    companion object {
        private val ITEM_DATA_TYPES_TO_PARSE = listOf(
                "ArmorJewelry_Amulet", "ArmorJewelry_Bracelet", "ArmorJewelry_Ring",
                "ArmorProtective_Forearm", "ArmorProtective_Head", "ArmorProtective_LowerBody", "ArmorProtective_UpperBody",
                "ItemArtifact", "ItemArtifactFormula", "ItemCharm", "ItemEquipment", "ItemRelic",
                "LootItemTable_DynWeight", "LootItemTable_FixedWeight", "LootMasterTable", "LootRandomizer", "LootRandomizerTable",
                "ProxyAccessoryPool",
                "OneShot_Dye", "OneShot_PotionHealth", "OneShot_PotionMana", "OneShot_Scroll",
                "QuestItem",
                "WeaponArmor_Shield",
                "WeaponHunting_Bow", "WeaponHunting_Spear",
                "WeaponMagical_Staff",
                "WeaponMelee_Axe", "WeaponMelee_Mace", "WeaponMelee_Sword"
        )


        val BLACKLISTED_VALUES = hashSetOf(
                "FileDescription", "ActorName", "templateName",
                "actorHeight", "actorRadius", "relicToRelicSound",
                "relicCompleteSound", "relicToItemSound",
                "hitSound", "swipeSound", "dropSound", "dropSound3D", "dropSoundWater", "useSound",
                "weaponTrail", "shadowBias", "maxTransparency", "castsShadows",
                "baseTexture", "bumpTexture", "mesh",
                "armorFemaleBaseTexture", "armorFemaleBumpTexture", "armorFemaleMesh",
                "armorJackalManBaseTextureName", "armorJackalManBumpTextureName", "armorJackalManMeshName",
                "armorMaleBaseTexture", "armorMaleBumpTexture", "armorMaleMesh",
                "armorSatyrBaseTextureName", "armorSatyrBumpTextureName", "armorSatyrMeshName",
                "armorTigermanBaseTextureName", "armorTigermanBumpTextureName", "armorTigermanMeshName"
        )
    }

    private fun TQInputStream.readDataEntry(dataType: Int, valuesCount: Int, key: String): DataEntry? = when (dataType) {
        0 -> {
            if (valuesCount == 1) {
                val value = this.readInt()
                if (value != 0) SingleIntDataEntry(key, value)
                else null
            } else {
                val values = IntArray(valuesCount) {
                    this.readInt()
                }
                IntDataEntry(key, values)
            }
        }
        1 -> {
            if (valuesCount == 1) {
                val value = this.readFloat()
                if (value != 0f) SingleFloatDataEntry(key, value)
                else null
            } else {
                val values = FloatArray(valuesCount) {
                    this.readFloat()
                }
                FloatDataEntry(key, values)
            }
        }
        2 -> {
            if (valuesCount == 1) {
                SingleStringDataEntry(key, strings[this.readInt()])
            } else {
                val values = Array(valuesCount) {
                    strings[this.readInt()]
                }
                StringDataEntry(key, values)
            }
        }
        3 -> {
            if (valuesCount == 1) {
                val value = this.readInt() > 0
                if (value) SingleBooleanDataEntry(key, value)
                else null
            } else {
                val values = BooleanArray(valuesCount) {
                    this.readInt() > 0
                }
                BooleanDataEntry(key, values)
            }
        }
        else -> throw Exception("Invalid dataType")
    }
}

private data class ArzHeader(
        val archivesOffset: Int, val archivesSize: Int, val archivesCount: Int,
        val stringsOffset: Int, val stringsSize: Int
)

private data class Archive(val id: String, val type: String, val offset: Int, val size: Int)

private data class DataSet(val id: String, val type: String, val entries: Map<String, DataEntry>)

sealed class DataEntry {
    abstract val key: String
}

fun DataEntry?.intValue(): Int? = (this as? SingleIntDataEntry)?.value
fun DataEntry?.floatValue(): Float? = (this as? SingleFloatDataEntry)?.value
fun DataEntry?.stringValue(): String? = (this as? SingleStringDataEntry)?.value
fun DataEntry?.booleanValue(): Boolean = (this as? SingleBooleanDataEntry)?.value ?: false

data class IntDataEntry(
        override val key: String,
        val values: IntArray
) : DataEntry()

data class BooleanDataEntry(
        override val key: String,
        val values: BooleanArray
) : DataEntry()

data class FloatDataEntry(
        override val key: String,
        val values: FloatArray
) : DataEntry()


data class StringDataEntry(
        override val key: String,
        val values: Array<String>
) : DataEntry()

data class SingleIntDataEntry(
        override val key: String,
        val value: Int
) : DataEntry()

private data class SingleBooleanDataEntry(
        override val key: String,
        val value: Boolean
) : DataEntry()

data class SingleFloatDataEntry(
        override val key: String,
        val value: Float
) : DataEntry()

data class SingleStringDataEntry(
        override val key: String,
        val value: String
) : DataEntry()
