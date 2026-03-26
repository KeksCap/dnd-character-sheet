package com.example.dndhelper.data

import java.util.UUID

// Главный "чемодан" со всеми данными персонажа
data class CharacterSaveData(
    val id: String = UUID.randomUUID().toString(), // Уникальный номер персонажа
    val name: String,
    val race: String,
    val charClass: String,
    val level: String,
    val maxHp: Int,
    val currentHp: Int,
    val imageUri: String?, // Путь к картинке сохраняем просто как текст
    val stats: List<StatSaveData>, // Характеристики без иконок
    val knownSpells: List<SpellInfo> // Твои сохраненные заклинания
)

// Маленький чемоданчик для характеристик (Сила, Ловкость и т.д.)
data class StatSaveData(
    val name: String,
    val baseScore: Int,
    val skillProficiencies: Map<String, Int> // Сохраняем только галочки навыков
)