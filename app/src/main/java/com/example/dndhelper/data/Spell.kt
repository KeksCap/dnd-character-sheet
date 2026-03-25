package com.example.dndhelper.data

data class Spell(
    val en: SpellInfo?,
    val ru: SpellInfo?
)

data class SpellInfo(
    val name: String? = "Без названия",
    val level: String? = "?",
    val school: String? = "",
    val castingTime: String? = "",
    val range: String? = "",
    val components: String? = "",
    val duration: String? = "",
    val text: String? = "",
    val materials: String? = "",
    val source: String? = ""
)