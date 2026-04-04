package com.example.dndhelper.ui.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.ui.graphics.vector.ImageVector

enum class RollType {
    AbilityCheck,
    SkillCheck,
    SavingThrow,
    AttackHit,
    Damage,
    Custom
}

enum class AdvantageMode {
    None,
    Advantage,
    Disadvantage
}

data class DiceRollData(
    val title: String,
    val rollType: RollType,
    val baseModifier: Int = 0,
    val proficiencyBonus: Int = 0,
    val isProficient: Boolean = false,
    val expertise: Boolean = false,
    val diceCount: Int = 1,
    val diceSides: Int = 20,
    val icon: ImageVector = Icons.Default.Casino
)

data class RollOutcome(
    val firstRoll: Int,
    val secondRoll: Int? = null, // For advantage/disadvantage
    val modifier: Int,
    val total: Int,
    val isCriticalSuccess: Boolean = false,
    val isCriticalFailure: Boolean = false,
    val mode: AdvantageMode = AdvantageMode.None
)
