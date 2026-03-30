package com.example.dndhelper.data

data class ArmorEntry(
    val nameRu: String,
    val nameEn: String,
    val baseAc: Int,
    val type: Int // 0=None, 1=Light, 2=Medium, 3=Heavy
)
