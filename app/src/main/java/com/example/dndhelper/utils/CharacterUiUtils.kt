package com.example.dndhelper.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Утилиты для интерфейса персонажа.
 */

fun trStat(name: String, isEn: Boolean): String {
    if (!isEn) return name

    return when(name) {
        "Сила", "Strength" -> "Strength"
        "Ловкость", "Dexterity" -> "Dexterity"
        "Тело", "Constitution" -> "Constitution"
        "Инт", "Intelligence" -> "Intelligence"
        "Мудр", "Wisdom" -> "Wisdom"
        "Хар", "Charisma" -> "Charisma"
        // Skills
        "Атлетика" -> "Athletics"
        "Акробатика" -> "Acrobatics"
        "Ловкость рук" -> "Sleight of Hand"
        "Скрытность" -> "Stealth"
        "Анализ" -> "Investigation"
        "История" -> "History"
        "Магия" -> "Arcana"
        "Природа" -> "Nature"
        "Восприятие" -> "Perception"
        "Выживание" -> "Survival"
        "Убеждение" -> "Persuasion"
        "Обман" -> "Deception"
        else -> name
    }
}

// --- ЦВЕТ ШКОЛЫ МАГИИ ---
fun schoolColor(school: String?): Color {
    return when (school?.lowercase()?.trim()) {
        "conjuration", "призыв" -> Color(0xFF7B1FA2)  // Фиолетовый
        "abjuration", "ограждение" -> Color(0xFF1565C0)          // Синий
        "necromancy", "некромантия" -> Color(0xFF37474F)            // Тёмно-серый
        "evocation", "проявление" -> Color(0xFFBF360C)             // Огненный
        "enchantment", "очарование" -> Color(0xFFAD1457)           // Розовый
        "transmutation", "преобразование" -> Color(0xFF2E7D32)     // Зелёный
        "illusion", "иллюзия" -> Color(0xFF00695C)                // Бирюзовый
        "divination", "прорицание" -> Color(0xFFF57F17)           // Янтарный
        else -> Color(0xFF546E7A)                                  // Серо-синий
    }
}

fun sourceName(source: String?): String {
    return when (source?.trim()?.uppercase()) {
        "PHB" -> "Player's Handbook"
        "XGTE" -> "Xanathar's Guide"
        "TCOE", "TASHA" -> "Tasha's Cauldron"
        "SCAG" -> "Sword Coast AG"
        "EGW" -> "Explorer's Guide"
        "FTD" -> "Fizban's Treasury"
        "SCC" -> "Strixhaven"
        "AI" -> "Acquisitions Inc."
        "TOEE" -> "Temple of Evil"
        "HB" -> "Homebrew"
        else -> source ?: ""
    }
}

fun getProficiencyBonus(level: String): Int {
    val lvl = level.toIntOrNull() ?: 1
    return when {
        lvl in 1..4 -> 2
        lvl in 5..8 -> 3
        lvl in 9..12 -> 4
        lvl in 13..16 -> 5
        lvl in 17..20 -> 6
        else -> 2
    }
}

fun getStatMod(score: Int): Int {
    return Math.floorDiv(score - 10, 2)
}
