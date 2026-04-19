package com.example.dndhelper.data

data class Background(
    val nameEn: String,
    val nameRu: String,
    val source: String,
    val proficiencies: String,
    val description: String,
    val proficienciesEn: String = "",
    val descriptionEn: String = "",
    val url: String,
    val traits: List<String>? = null,
    val ideals: List<String>? = null,
    val bonds: List<String>? = null,
    val flaws: List<String>? = null,
    val traitsEn: List<String>? = null,
    val idealsEn: List<String>? = null,
    val bondsEn: List<String>? = null,
    val flawsEn: List<String>? = null
)
