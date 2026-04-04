package com.example.dndhelper.data

data class RaceTrait(
    val name: String,
    val desc: String
)

data class Ethnicity(
    val name: String,
    val description: String,
    val names: String? = null
)

data class RaceSubclass(
    val nameEn: String,
    val nameRu: String,
    val traitsEn: List<RaceTrait>,
    val traitsRu: List<RaceTrait>,
    val loreEn: String? = null,
    val loreRu: String? = null
)

data class Race(
    val nameEn: String,
    val nameRu: String,
    val traitsEn: List<RaceTrait>,
    val traitsRu: List<RaceTrait>,
    val subraces: List<RaceSubclass> = emptyList(),
    val loreEn: String? = null,
    val loreRu: String? = null,
    val namesEn: String? = null,
    val namesRu: String? = null,
    val ethnicitiesEn: List<Ethnicity>? = null,
    val ethnicitiesRu: List<Ethnicity>? = null
)
