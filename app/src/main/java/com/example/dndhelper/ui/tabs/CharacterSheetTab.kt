package com.example.dndhelper.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dndhelper.data.*
import com.example.dndhelper.ui.components.*
import com.example.dndhelper.ui.models.AbilityScore
import com.example.dndhelper.ui.models.DiceRollData
import com.example.dndhelper.ui.models.RollOutcome
import com.example.dndhelper.utils.GameLogManager
import com.example.dndhelper.utils.getProficiencyBonus
import com.example.dndhelper.utils.getStatMod
@Composable
fun CharacterSheetTab(
    isEn: Boolean,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    magicItems: List<MagicItem>,
    onAddLog: (GameLogEntry) -> Unit
) {
    val language = if (isEn) "en" else "ru"
    
    val stats = remember(character.stats, isEn) {
        val defaultStats = listOf(
            AbilityScore("Сила", 10, Icons.Default.FitnessCenter, if(isEn) listOf("Athletics") else listOf("Атлетика")),
            AbilityScore("Ловкость", 10, Icons.Default.DirectionsRun, if(isEn) listOf("Acrobatics", "Sleight of Hand", "Stealth") else listOf("Акробатика", "Ловкость рук", "Скрытность")),
            AbilityScore("Тело", 10, Icons.Default.Favorite),
            AbilityScore("Инт", 10, Icons.Default.MenuBook, if(isEn) listOf("Arcana", "History", "Investigation", "Nature", "Religion") else listOf("Анализ", "История", "Магия", "Природа", "Религия")),
            AbilityScore("Мудр", 10, Icons.Default.Visibility, if(isEn) listOf("Animal Handling", "Insight", "Medicine", "Perception", "Survival") else listOf("Восприятие", "Выживание", "Проницательность", "Уход за животными", "Медицина")),
            AbilityScore("Хар", 10, Icons.Default.SelfImprovement, if(isEn) listOf("Deception", "Intimidation", "Performance", "Persuasion") else listOf("Убеждение", "Обман", "Выступление", "Запугивание"))
        )
        
        defaultStats.map { defaultStat ->
            val charStat = character.stats.find { 
                when (defaultStat.name) {
                    "Сила" -> it.name in listOf("Strength", "Сила")
                    "Ловкость" -> it.name in listOf("Dexterity", "Ловкость")
                    "Тело" -> it.name in listOf("Constitution", "Телосложение", "Тело")
                    "Инт" -> it.name in listOf("Intelligence", "Интеллект", "Инт")
                    "Мудр" -> it.name in listOf("Wisdom", "Мудрость", "Мудр")
                    "Хар" -> it.name in listOf("Charisma", "Харизма", "Хар")
                    else -> false
                }
            }
            if (charStat != null) {
                defaultStat.copy(baseScore = charStat.baseScore, skillProficiencies = charStat.skillProficiencies)
            } else {
                defaultStat
            }
        }
    }

    val strValue = stats.find { it.name == "Сила" || it.name == "Strength" }?.baseScore ?: 10
    val strMod = getStatMod(strValue)
    val dexValue = stats.find { it.name == "Ловкость" || it.name == "Dexterity" }?.baseScore ?: 10
    val dexMod = getStatMod(dexValue)
    val profBonus = getProficiencyBonus(character.level)
    val conValue = stats.find { it.name == "Тело" || it.name == "Constitution" }?.baseScore ?: 10
    val conMod = getStatMod(conValue)

    val initiativeText = if (dexMod >= 0) "+$dexMod" else "$dexMod"

    val armorList = remember(character.customArmors, isEn, StandardEquipment.items) {
        val standard = listOf(ArmorEntry("Без брони", "Unarmored", 10, 0)) +
            StandardEquipment.getArmor().map { 
                ArmorEntry(it.nameRu, it.nameEn, it.baseAc ?: 10, it.type ?: 0) 
            }
        standard + character.customArmors.map {
            ArmorEntry(it.name, it.name, it.baseAc, it.type)
        }
    }

    val selectedArmor = if (character.selectedArmorIndex < armorList.size) armorList[character.selectedArmorIndex] else armorList[0]
    val dexBonus = when (selectedArmor.type) {
        0 -> dexMod
        1 -> dexMod
        2 -> dexMod.coerceAtMost(2)
        else -> 0
    }
    val armorClass = selectedArmor.baseAc + dexBonus + (if (character.shieldEquipped) 2 else 0) + character.magicBonus

    var activeRollData by remember { mutableStateOf<DiceRollData?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        HeaderInfoBlock(
            character = character,
            onCharacterChange = onCharacterChange
        )
        Spacer(Modifier.height(16.dp))

        HealthRestBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            conMod = conMod,
            onAddLog = onAddLog
        )

        ConditionsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            isEn = isEn,
            onAddLog = onAddLog
        )

        CombatStatsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            armorClass = armorClass,
            initiativeText = initiativeText,
            isEn = isEn,
            armorList = armorList,
            onRollRequest = { activeRollData = it }
        )

        val hasDisadvantageOnChecks = character.activeConditions.any { 
            it.contains("Отравлен") || it.contains("Poisoned") ||
            it.contains("Испуган") || it.contains("Frightened")
        } || character.exhaustionLevel >= 1

        StatsGrid(
            stats = stats,
            isEn = isEn,
            profBonus = profBonus,
            hasDisadvantageOnChecks = hasDisadvantageOnChecks,
            onStatsChange = { newAbilityScores ->
                val updatedStats = character.stats.toMutableList()
                newAbilityScores.forEach { ab ->
                    val index = updatedStats.indexOfFirst {
                        when (ab.name) {
                            "Сила" -> it.name in listOf("Strength", "Сила")
                            "Ловкость" -> it.name in listOf("Dexterity", "Ловкость")
                            "Тело" -> it.name in listOf("Constitution", "Телосложение", "Тело")
                            "Инт" -> it.name in listOf("Intelligence", "Интеллект", "Инт")
                            "Мудр" -> it.name in listOf("Wisdom", "Мудрость", "Мудр")
                            "Хар" -> it.name in listOf("Charisma", "Харизма", "Хар")
                            else -> false
                        }
                    }
                    if (index != -1) {
                        updatedStats[index] = updatedStats[index].copy(
                            baseScore = ab.baseScore,
                            skillProficiencies = ab.skillProficiencies
                        )
                    } else {
                        val saveName = when(ab.name) {
                            "Сила" -> if(isEn) "Strength" else "Сила"
                            "Ловкость" -> if(isEn) "Dexterity" else "Ловкость"
                            "Тело" -> if(isEn) "Constitution" else "Телосложение"
                            "Инт" -> if(isEn) "Intelligence" else "Интеллект"
                            "Мудр" -> if(isEn) "Wisdom" else "Мудрость"
                            "Хар" -> if(isEn) "Charisma" else "Харизма"
                            else -> ab.name
                        }
                        updatedStats.add(StatSaveData(saveName, ab.baseScore, ab.skillProficiencies))
                    }
                }
                onCharacterChange(character.copy(stats = updatedStats))
            },
            onRollRequest = { activeRollData = it }
        )

        val hasDisadvantageOnAttacks = character.activeConditions.any { 
            it.contains("Ослеплен") || it.contains("Blinded") ||
            it.contains("Отравлен") || it.contains("Poisoned") ||
            it.contains("Испуган") || it.contains("Frightened") ||
            it.contains("Сбит с ног") || it.contains("Prone") ||
            it.contains("Опутан") || it.contains("Restrained")
        } || character.exhaustionLevel >= 3

        AttacksBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            isEn = isEn,
            dexMod = dexMod,
            strMod = strMod,
            profBonus = profBonus,
            hasDisadvantageOnAttacks = hasDisadvantageOnAttacks,
            onRollRequest = { activeRollData = it }
        )

        MagicItemsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            magicItems = magicItems,
            isEn = isEn
        )

        SpellSlotsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            onAddLog = onAddLog
        )

        KnownSpellsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            isEn = isEn
        )

        Spacer(Modifier.height(24.dp))
        BiographyBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            isEn = isEn
        )
    }

    if (activeRollData != null) {
        DiceRollBottomSheet(
            rollData = activeRollData!!,
            onDismiss = { activeRollData = null },
            onRollComplete = { rollData, outcome ->
                val breakdown = buildLogBreakdown(outcome, rollData)
                val critRu = when {
                    outcome.isCriticalSuccess -> " КРИТ!"
                    outcome.isCriticalFailure -> " ПРОВАЛ!"
                    else -> ""
                }
                val critEn = when {
                    outcome.isCriticalSuccess -> " CRIT!"
                    outcome.isCriticalFailure -> " FAIL!"
                    else -> ""
                }
                onAddLog(
                    GameLogManager.logDiceRoll(
                        rollData.title,
                        rollData.title,
                        "$breakdown = ${outcome.total}$critRu",
                        "$breakdown = ${outcome.total}$critEn"
                    )
                )
            }
        )
    }
}

private fun buildLogBreakdown(outcome: RollOutcome, data: DiceRollData): String {
    val rollPart = if (outcome.secondRoll != null) {
        val modeStr = if (outcome.mode == com.example.dndhelper.ui.models.AdvantageMode.Advantage) "Adv" else "Dis"
        "(${outcome.firstRoll} vs ${outcome.secondRoll}) [$modeStr]"
    } else {
        "(${outcome.firstRoll})"
    }
    val modPart = if (outcome.modifier > 0) " + ${outcome.modifier}" else if (outcome.modifier < 0) " - ${-outcome.modifier}" else ""
    return "$rollPart$modPart"
}
