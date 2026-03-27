package com.example.dndhelper.data

// --- ЧИСТАЯ МОДЕЛЬ ДАННЫХ ДЛЯ JSON ---

data class MonsterRawData(
    val desc: String?, // Вот оно, полное описание!
    val strength: Int?,
    val dexterity: Int?,
    val constitution: Int?,
    val intelligence: Int?,
    val wisdom: Int?,
    val charisma: Int?,

    val strength_save: Int?,
    val dexterity_save: Int?,
    val constitution_save: Int?,
    val intelligence_save: Int?,
    val wisdom_save: Int?,
    val charisma_save: Int?,

    val perception: Int?,
    val speed: Map<String, String>?, // СКОРОСТЬ

    val senses: String?,
    val languages: String?,

    val special_abilities: List<AbilityAction>?, // Особенности (пассивки)
    val actions: List<AbilityAction>?,           // Действия (атаки)
    val legendary_actions: List<AbilityAction>?, // Легендарные действия
    val reactions: List<AbilityAction>?          // Реакции
)

data class AbilityAction(
    val name: String,
    val desc: String
)