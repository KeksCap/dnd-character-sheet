package com.example.dndhelper.data

data class RaceTrait(
    val name: String,
    val desc: String
)

data class RaceSubclass(
    val nameEn: String,
    val nameRu: String,
    val traitsEn: List<RaceTrait>,
    val traitsRu: List<RaceTrait>
)

data class Race(
    val nameEn: String,
    val nameRu: String,
    val traitsEn: List<RaceTrait>,
    val traitsRu: List<RaceTrait>,
    val subraces: List<RaceSubclass> = emptyList()
)
