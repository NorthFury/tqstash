package tqstash

import tqstash.dto.Item
import tqstash.util.MathExpressionParser
import java.io.File


fun main(args: Array<String>) {
    val dbFileName = """E:\Games\Steam\SteamApps\common\Titan Quest Anniversary Edition\Database\database.arz"""
    val textFileName = """e:\Games\Steam\SteamApps\common\Titan Quest Anniversary Edition\Text\Text_EN.arc"""
    val texturesFileName = """e:\Games\Steam\SteamApps\common\Titan Quest Anniversary Edition\Resources\Items.arc"""
    val xTexturesFileName = """e:\Games\Steam\SteamApps\common\Titan Quest Anniversary Edition\Resources\XPack\Items.arc"""

    val stashFile = "transfer-stash.dxb"

    val stash = readStash(File(stashFile))
    val translations = readTranslations(File(textFileName))
    val textures = TextureStore(File(texturesFileName), File(xTexturesFileName))

    val paramMatcher = Regex("""\{(.+?)\d}""")
    val s = translations["MeetsRequirement"]!!.replace(paramMatcher, "$1")
    println(s.format("Strength", 5f.toDouble()))

//    File("translations.txt").writeText(translations.map{"${it.key} = ${it.value}"}.joinToString("\n"))

    val itemDataStore = ItemDataStore(File(dbFileName))
    itemDataStore.print()

    stash.items.asSequence().forEach { (item, _) ->
        println("==============================================================")
        println(item.baseItemId)
        println(item)
        loadItem(item, itemDataStore)
    }
}

private fun getBitmapId(baseItemData: Map<String, DataEntry>): DataEntry? = baseItemData.values.find {
    it.key == "bitmap"
            || it.key == "relicBitmap"
            || it.key == "artifactFormulaBitmapName"
            || it.key == "artifactBitmap"
}

private fun loadItem(item: Item, itemDataStore: ItemDataStore) {
    val baseInfo = itemDataStore.get(item.baseItemId)
    val prefixInfo = itemDataStore.get(item.prefixId)
    val suffixInfo = itemDataStore.get(item.suffixId)
    val relicInfo = itemDataStore.get(item.relicId)
    val relicBonusInfo = itemDataStore.get(item.relicBonusId)
    printDataEntries(item.baseItemId, baseInfo)
    printDataEntries(item.prefixId, prefixInfo)
    printDataEntries(item.suffixId, suffixInfo)
    printDataEntries(item.relicId, relicInfo)
    printDataEntries(item.relicBonusId, relicBonusInfo)
    println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

    val rarity = baseInfo["itemClassification"].stringValue()
    if (rarity == "Rare") {
        val dropsIn = getDropDifficulty(item.baseItemId)
    }
    val itemLevel = baseInfo["itemLevel"].intValue() ?: 0
    val itemNameTag = baseInfo["itemNameTag"].stringValue() // TODO: don't forget to fix MI translated name
    val bitmap = getBitmapId(baseInfo).stringValue()

    /* >>>>>>>>>> Requirements <<<<<<<<<<<<<<<<<<<<*/
    val requirements = getRequirements(baseInfo) +
            getRequirements(prefixInfo) +
            getRequirements(suffixInfo) +
            getRequirements(relicInfo)

    val itemClass = baseInfo["Class"].stringValue()
    val costPrefix = getRequirementPrefix(itemClass ?: "")
    val dynamicRequirement = if (costPrefix != null) {
        val attributesCount = ALL_ATTRIBUTE_GROUPS.count { attribute ->
            baseInfo.keys.any { it.startsWith(attribute, true) } ||
                    prefixInfo.keys.any { it.startsWith(attribute, true) } ||
                    suffixInfo.keys.any { it.startsWith(attribute, true) }
        }
        val itemCostName = baseInfo["itemCostName"].stringValue()
        val itemCostInfo = if (itemCostName != null) itemDataStore.get(itemCostName) else emptyMap()

        itemCostInfo.values.asSequence()
                .filter { it.key.startsWith(costPrefix) && !it.key.endsWith("CostEquation") }
                .mapNotNull { it as? SingleStringDataEntry }
                .map { (key, equation) ->
                    val computedValue = MathExpressionParser.PARSER.parse(equation).eval {
                        when (it) {
                            "itemLevel" -> itemLevel.toDouble()
                            "totalAttCount" -> attributesCount.toDouble()
                            else -> throw Exception("Unexpected symbol in equation: $it")
                        }
                    }
                    val value = Math.ceil(computedValue).toInt()
                    val stat = key.substring(costPrefix.length, key.length - "Equation".length)
                            .decapitalize() + "Requirement"
                    stat to value
                }
    } else {
        emptySequence()
    }

    val allRequirements = (requirements + dynamicRequirement)
            .groupingBy { it.first }
            .fold(0) { accumulator, element -> Math.max(accumulator, element.second) }

    allRequirements.forEach(::println)
    /* >>>>>>>>>> Requirements <<<<<<<<<<<<<<<<<<<<*/


    println()

    val rawAttributes = sequenceOf(baseInfo, prefixInfo, suffixInfo, relicInfo, relicBonusInfo)
            .flatMap { it.values.asSequence() }
            .toList()

    val characterAttributes = CharacterAttributes.ALL_GROUPS.asSequence().flatMap { field ->
        val value = singleValueItemAttribute(rawAttributes, field)
        val mod = singleValueItemAttribute(rawAttributes, field + "Modifier")

        sequenceOf(value, mod).filterNotNull()
    }

    val defensiveAttributes = DefensiveAttributes.ALL_GROUPS.asSequence().flatMap { field ->
        val value = singleValueItemAttribute(rawAttributes, field)
        val mod = singleValueItemAttribute(rawAttributes, field + "Modifier")
        val chance = singleValueItemAttribute(rawAttributes, field + "Chance")
        val chanceModifier = singleValueItemAttribute(rawAttributes, field + "ModifierChance")

        sequenceOf(value, mod, chance, chanceModifier).filterNotNull()
    }
    val defensiveTotalSpeed = sequenceOf(
            singleValueItemAttribute(rawAttributes, "defensiveTotalSpeedResistance")
    ).filterNotNull()

    (characterAttributes + defensiveAttributes + defensiveTotalSpeed).forEach(::println)
}

private fun singleValueItemAttribute(rawAttributes: List<DataEntry>, field: String): SingleValueItemAttribute? {
    val value = rawAttributes.asSequence()
            .filter { it.key == field }
            .mapNotNull { (it as? SingleFloatDataEntry)?.value }
            .sum()

    return if (value > 0.0f) SingleValueItemAttribute(field, value)
    else null
}

private fun printDataEntries(id: String, entries: Map<String, DataEntry>) {
    if (id.isNotBlank() && entries.isEmpty()) {
        println(">>>>>>>>>>>> Missing data set <<<<<<<<<<<<")
        println(id)
    }
    if (entries.isNotEmpty() && (
            id.equals("Records\\XPack\\Item\\Charms\\01_Act4_ErebanCrystal.dbr", true) ||
                    id.equals("Records\\item\\lootmagicalaffixes\\prefix\\Default\\rare_skillall_01.dbr", true)
            )
            ) {
        println("-------")
        println(id)
        entries.values
                .sortedWith(compareBy({ it.javaClass.canonicalName }, { it.key }))
                .forEach(::println)
    }
}

private fun getRequirementPrefix(itemClass: String): String? {
    val indexOfSplit = itemClass.indexOf('_')

    if (indexOfSplit != -1) {
        return itemClass.substring(indexOfSplit + 1).decapitalize()
    }

    return null
}

private val requirements = hashSetOf("levelRequirement", "intelligenceRequirement", "dexterityRequirement", "strengthRequirement")
private fun getRequirements(data: Map<String, DataEntry>) = data.values.asSequence()
        .filter { it.key in requirements }
        .mapNotNull { it as? SingleIntDataEntry }
        .map { it.key to it.value }

private fun getDropDifficulty(baseItemId: String): Difficulty? {
    // MI_E_GorgonArcher.dbr
    val index = baseItemId.indexOf('_')

    if (index > 0 && baseItemId[index + 2] == '_') {
        when (baseItemId[index + 1]) {
            'N' -> return Difficulty.Normal
            'E' -> return Difficulty.Epic
            'L' -> return Difficulty.Legendary
        }
    }

    return null
}

enum class Difficulty { Normal, Epic, Legendary }

sealed class ItemAttribute {
    abstract val key: String
}

data class SingleValueItemAttribute(override val key: String, val value: Float) : ItemAttribute()
