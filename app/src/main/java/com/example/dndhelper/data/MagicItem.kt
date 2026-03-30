package com.example.dndhelper.data

import com.google.gson.annotations.SerializedName

data class MagicItem(
    val slug: String,
    val nameRu: String,
    val nameEn: String,
    val typeRu: String,
    val typeEn: String,
    val subtypeRu: String?,
    val subtypeEn: String?,
    val rarity: String?,
    val attunement: Attunement?,
    val descriptionRu: String?,
    val descriptionEn: String?,
    val spells: List<String> = emptyList(),
    val document: String? = null,
    val isCustom: Boolean = false,
    val isAttuned: Boolean = false
)

data class Attunement(
    val required: Boolean,
    val condition: String?
)
