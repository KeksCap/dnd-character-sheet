package com.example.dndhelper.ui.models

import androidx.compose.ui.graphics.vector.ImageVector

data class AbilityScore(
    val name: String,
    val baseScore: Int,
    val icon: ImageVector,
    val skills: List<String> = emptyList(),
    val skillProficiencies: Map<String, Int> = emptyMap()
)
