package tqstash

object CharacterAttributes {
    val ALL_GROUPS = hashSetOf(
            "characterArmorStrengthReqReduction",
            "characterArmorDexterityReqReduction",
            "characterArmorIntelligenceReqReduction",
            "characterAttackSpeedModifier",
            "characterDefensiveAbility",
            "characterDefensiveBlockRecoveryReduction",
            "characterDeflectProjectile",
            "characterDexterity",
            "characterDodgePercent",
            "characterEnergyAbsorptionPercent",
            "characterGlobalReqReduction",
            "characterHuntingStrengthReqReduction",
            "characterHuntingDexterityReqReduction",
            "characterHuntingIntelligenceReqReduction",
            "characterIncreasedExperience",
            "characterIntelligence",
            "characterJewelryStrengthReqReduction",
            "characterJewelryDexterityReqReduction",
            "characterJewelryIntelligenceReqReduction",
            "characterLevelReqReduction",
            "characterLife",
            "characterLifeRegen",
            "characterOffensiveAbility",
            "characterMana",
            "characterManaLimitReserve",
            "characterManaLimitReserveReduction",
            "characterManaRegen",
            "characterMeleeStrengthReqReduction",
            "characterMeleeDexterityReqReduction",
            "characterMeleeIntelligenceReqReduction",
            "characterRunSpeed",
            "characterShieldStrengthReqReduction",
            "characterShieldDexterityReqReduction",
            "characterShieldIntelligenceReqReduction",
            "characterSpellCastSpeed",
            "characterStaffStrengthReqReduction",
            "characterStaffDexterityReqReduction",
            "characterStaffIntelligenceReqReduction",
            "characterStrength",
            "characterTotalSpeed",
            "characterWeaponStrengthReqReduction",
            "characterWeaponDexterityReqReduction",
            "characterWeaponIntelligenceReqReduction"
    )
}

object DefensiveAttributes {
    val REGULAR = hashSetOf(
            "defensiveAbsorption",
            "defensiveBleeding",
            "defensiveBleedingDuration",
            "defensiveBlock",
            "defensiveConfusion",
            "defensiveConvert",
            "defensiveCold",
            "defensiveColdDuration",
            "defensiveDisruption",
            "defensiveElementalResistance",
            "defensiveFear",
            "defensiveFire",
            "defensiveFireDuration",
            "defensiveFreeze",
            "defensiveLife",
            "defensiveLifeDuration",
            "defensiveLightning",
            "defensiveLightningDuration",
            "defensiveManaBurnRatio",
            "defensivePercentCurrentLife",
            "defensivePetrify",
            "defensivePhysical",
            "defensivePhysicalDuration",
            "defensivePierce",
            "defensivePierceDuration",
            "defensivePoison",
            "defensivePoisonDuration",
            "defensiveProtection",
            "defensiveReflect",
            "defensiveSlowLifeLeach",
            "defensiveSlowLifeLeachDuration",
            "defensiveSlowManaLeach",
            "defensiveSlowManaLeachDuration",
            "defensiveSleep",
            "defensiveStun",
            "defensiveTaunt",
            "defensiveTrap"
    )

    val OTHER = "defensiveTotalSpeed"

    val ALL_GROUPS = REGULAR + OTHER
}

object OffensiveAttributes {
    val ABSOLUTE = hashSetOf(
            "offensiveBaseCold",
            "offensiveBaseFire",
            "offensiveBaseLife",
            "offensiveBaseLightning",
            "offensiveBasePoison",
            "offensiveBonusPhysical",
            "offensiveCold",
            "offensiveElemental",
            "offensiveFire",
            "offensiveLife",
            "offensiveLifeLeech",
            "offensiveLightning",
            "offensivePercentCurrentLife",
            "offensivePhysical",
            "offensivePierce",
            "offensivePierceRatio",
            "offensivePoison",
            "offensiveTotalDamage",
            "retaliationCold",
            "retaliationElemental",
            "retaliationFire",
            "retaliationLife",
            "retaliationLightning",
            "retaliationPercentCurrentLife",
            "retaliationPhysical",
            "retaliationPierce",
            "retaliationPierceRatio",
            "retaliationPoison",
            "retaliationStun"
    )

    val DOT = hashSetOf(
            "offensiveSlowBleeding",
            "offensiveSlowCold",
            "offensiveSlowFire",
            "offensiveSlowLife",
            "offensiveSlowLifeLeach",
            "offensiveSlowLightning",
            "offensiveSlowManaLeach",
            "offensiveSlowPhysical",
            "offensiveSlowPoison",
            "retaliationSlowBleeding",
            "retaliationSlowCold",
            "retaliationSlowFire",
            "retaliationSlowLife",
            "retaliationSlowLifeLeach",
            "retaliationSlowLightning",
            "retaliationSlowManaLeach",
            "retaliationSlowPhysical",
            "retaliationSlowPoison"
    )

    val EOT = hashSetOf(
            "offensiveConfusion",
            "offensiveConvert",
            "offensiveDisruption",
            "offensiveFear",
            "offensiveFreeze",
            "offensiveFumble",
            "offensivePetrify",
            "offensiveProjectileFumble",
            "offensiveSleep",
            "offensiveSlowAttackSpeed",
            "offensiveSlowDefensiveAbility",
            "offensiveSlowDefensiveReduction",
            "offensiveSlowOffensiveAbility",
            "offensiveSlowOffensiveReduction",
            "offensiveSlowRunSpeed",
            "offensiveSlowSpellCastSpeed",
            "offensiveSlowTotalSpeed",
            "offensiveStun",
            "offensiveTotalDamageReductionAbsolute",
            "offensiveTotalDamageReductionPercent",
            "offensiveTotalResistanceReductionAbsolute",
            "offensiveTotalResistanceReductionPercent",
            "offensiveTrap",
            "retaliationSlowAttackSpeed",
            "retaliationSlowDefensiveAbility",
            "retaliationSlowOffensiveAbility",
            "retaliationSlowOffensiveReduction",
            "retaliationSlowRunSpeed",
            "retaliationSlowSpellCastSpeed"
    )

    val MANA = hashSetOf("offensiveManaBurn")

    val ALL_GROUPS = ABSOLUTE + DOT + EOT + MANA
}

val ALL_ATTRIBUTE_GROUPS = CharacterAttributes.ALL_GROUPS +
        DefensiveAttributes.ALL_GROUPS +
        OffensiveAttributes.ALL_GROUPS
