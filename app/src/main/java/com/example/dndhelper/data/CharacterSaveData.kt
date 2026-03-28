package com.example.dndhelper.data

import java.util.UUID

// Главный "чемодан" со всеми данными персонажа
data class CharacterSaveData(
    val id: String = UUID.randomUUID().toString(), // Уникальный номер персонажа
    val name: String = "",
    val race: String = "",
    val charClass: String = "",
    val level: String = "1",
    val maxHp: Int = 10,
    val currentHp: Int = 10,
    val imageUri: String? = null, // Путь к картинке сохраняем просто как текст
    val stats: List<StatSaveData> = emptyList(), // Характеристики без иконок
    val knownSpells: List<SpellInfo> = emptyList(), // Твои сохраненные заклинания
    
    // --- СОСТОЯНИЕ КД ---
    val selectedArmorIndex: Int = 0,
    val shieldEquipped: Boolean = false,
    val magicBonus: Int = 0,

    // --- ВАЛЮТА ---

    val cp: Int = 0,
    val sp: Int = 0,
    val ep: Int = 0,
    val gp: Int = 0,
    val pp: Int = 0,

    // --- ИНВЕНТАРЬ И СОКРОВИЩА ---
    val inventoryItems: List<String> = emptyList(),
    val treasures: List<String> = emptyList(),
    val customWeapons: List<CustomWeapon> = emptyList(),
    val customArmors: List<CustomArmor> = emptyList(),
    val equippedStandardWeapons: List<String> = emptyList(), // Экипированное стандартное оружие
    val standardWeaponProficiencies: Map<String, Boolean> = emptyMap(), // Настройка владения для стандартного
    val standardWeaponAbilities: Map<String, String> = emptyMap(), // Настройка СИЛ/ЛОВ для стандартного

    // --- ЛИЧНОСТЬ И БИОГРАФИЯ ---
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val eyes: String = "",
    val skin: String = "",
    val hair: String = "",
    val background: String = "",
    val allies: String = "",
    val personalityTraits: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val flaws: String = ""
)

// Кастомное оружие
data class CustomWeapon(
    val name: String,
    val damage: String,
    val ability: String = "STR", // "STR" или "DEX"
    val isProficient: Boolean = true,
    val magicBonus: Int = 0
)

// Кастомная броня
data class CustomArmor(
    val name: String,
    val baseAc: Int,
    val type: Int // 0=None, 1=Light, 2=Medium, 3=Heavy
)

// Маленький чемоданчик для характеристик (Сила, Ловкость и т.д.)
data class StatSaveData(
    val name: String,
    val baseScore: Int,
    val skillProficiencies: Map<String, Int> = emptyMap() // Сохраняем только галочки навыков
)