package tqstash.dto

data class Item(
        val stackCount: Int,
        val baseItemId: String,
        val prefixId: String,
        val suffixId: String,
        val relicId: String,
        val relicBonusId: String,
        val seed: Int,
        val var1: Int
)
