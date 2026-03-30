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
import com.example.dndhelper.utils.getProficiencyBonus
import com.example.dndhelper.utils.getStatMod

@Composable
fun CharacterSheetTab(
    isEn: Boolean,
    character: CharacterSaveData,
    onCharacterChange: (CharacterSaveData) -> Unit,
    magicItems: List<MagicItem>
) {
    val language = if (isEn) "en" else "ru"
    
    var stats by remember { mutableStateOf(listOf(
        AbilityScore("Сила", 16, Icons.Default.FitnessCenter, listOf("Атлетика")),
        AbilityScore("Ловкость", 7, Icons.Default.DirectionsRun, listOf("Акробатика", "Ловкость рук", "Скрытность")),
        AbilityScore("Тело", 15, Icons.Default.Favorite),
        AbilityScore("Инт", 10, Icons.Default.MenuBook, listOf("Анализ", "История", "Магия", "Природа")),
        AbilityScore("Мудр", 12, Icons.Default.Visibility, listOf("Восприятие", "Выживание")),
        AbilityScore("Хар", 8, Icons.Default.SelfImprovement, listOf("Убеждение", "Обман"))
    ))}

    val strValue = stats.find { it.name == "Сила" || it.name == "Strength" }?.baseScore ?: 10
    val strMod = getStatMod(strValue)
    val dexValue = stats.find { it.name == "Ловкость" || it.name == "Dexterity" }?.baseScore ?: 10
    val dexMod = getStatMod(dexValue)
    val profBonus = getProficiencyBonus(character.level)
    val conValue = stats.find { it.name == "Тело" || it.name == "Constitution" }?.baseScore ?: 10
    val conMod = getStatMod(conValue)

    val initiativeText = if (dexMod >= 0) "+$dexMod" else "$dexMod"

    val standardArmorList = listOf(ArmorEntry("Без брони", "Unarmored", 10, 0)) +
        StandardEquipment.getArmor().map { 
            ArmorEntry(it.nameRu, it.nameEn, it.baseAc ?: 10, it.type ?: 0) 
        }

    val armorList = standardArmorList + character.customArmors.map {
        ArmorEntry(it.name, it.name, it.baseAc, it.type)
    }

    val selectedArmor = if (character.selectedArmorIndex < armorList.size) armorList[character.selectedArmorIndex] else armorList[0]
    val dexBonus = when (selectedArmor.type) {
        0 -> dexMod
        1 -> dexMod
        2 -> dexMod.coerceAtMost(2)
        else -> 0
    }
    val armorClass = selectedArmor.baseAc + dexBonus + (if (character.shieldEquipped) 2 else 0) + character.magicBonus

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        HeaderInfoBlock(
            character = character,
            onCharacterChange = onCharacterChange
        )
        Spacer(Modifier.height(16.dp))

        HealthRestBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            conMod = conMod
        )

        ConditionsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            isEn = isEn
        )

        CombatStatsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            armorClass = armorClass,
            initiativeText = initiativeText,
            isEn = isEn,
            armorList = armorList
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
            onStatsChange = { stats = it }
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
            hasDisadvantageOnAttacks = hasDisadvantageOnAttacks
        )

        MagicItemsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            magicItems = magicItems,
            isEn = isEn
        )

        SpellSlotsBlock(
            character = character,
            onCharacterChange = onCharacterChange
        )

        KnownSpellsBlock(
            character = character,
            onCharacterChange = onCharacterChange,
            isEn = isEn
        )

        Spacer(Modifier.height(24.dp))
        BiographyBlock(
            character = character,
            onCharacterChange = onCharacterChange
        )
    }
}
