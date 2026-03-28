package com.example.dndhelper.data

data class ClassFeature(
    val level: Int,
    val name: String,
    val desc: String
)

data class Subclass(
    val name: String,
    val desc: String,
    val features: List<ClassFeature>
)

data class DndClass(
    val nameEn: String,
    val nameRu: String,
    val hitDie: String,
    val traitsEn: Map<String, String>,
    val traitsRu: Map<String, String>,
    val featuresEn: List<ClassFeature>,
    val featuresRu: List<ClassFeature>,
    val subclassesEn: List<Subclass>,
    val subclassesRu: List<Subclass>
)
