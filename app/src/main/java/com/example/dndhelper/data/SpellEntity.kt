package com.example.dndhelper.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spells")
data class SpellEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name_en") val nameEn: String?,
    @ColumnInfo(name = "name_ru") val nameRu: String?,
    @ColumnInfo(name = "level") val level: String?,
    @ColumnInfo(name = "school_en") val schoolEn: String?,
    @ColumnInfo(name = "school_ru") val schoolRu: String?,
    @ColumnInfo(name = "casting_time_en") val castingTimeEn: String?,
    @ColumnInfo(name = "casting_time_ru") val castingTimeRu: String?,
    @ColumnInfo(name = "range_en") val rangeEn: String?,
    @ColumnInfo(name = "range_ru") val rangeRu: String?,
    @ColumnInfo(name = "components_en") val componentsEn: String?,
    @ColumnInfo(name = "components_ru") val componentsRu: String?,
    @ColumnInfo(name = "duration_en") val durationEn: String?,
    @ColumnInfo(name = "duration_ru") val durationRu: String?,
    @ColumnInfo(name = "materials_en") val materialsEn: String?,
    @ColumnInfo(name = "materials_ru") val materialsRu: String?,
    @ColumnInfo(name = "text_en") val textEn: String?,
    @ColumnInfo(name = "text_ru") val textRu: String?,
    @ColumnInfo(name = "source") val source: String?,
    @ColumnInfo(name = "ritual") val ritual: Int = 0
)

// Конвертируем SpellEntity в SpellInfo для использования в UI
fun SpellEntity.toSpellInfo(language: String): SpellInfo {
    val isEn = language == "en"
    return SpellInfo(
        name = if (isEn) nameEn else (nameRu ?: nameEn),
        level = level,
        school = if (isEn) schoolEn else (schoolRu ?: schoolEn),
        castingTime = if (isEn) castingTimeEn else (castingTimeRu ?: castingTimeEn),
        range = if (isEn) rangeEn else (rangeRu ?: rangeEn),
        components = if (isEn) componentsEn else (componentsRu ?: componentsEn),
        duration = if (isEn) durationEn else (durationRu ?: durationEn),
        materials = if (isEn) materialsEn else (materialsRu ?: materialsEn),
        text = if (isEn) textEn else (textRu ?: textEn),
        source = source
    )
}

// Конвертируем SpellEntity в старый формат Spell (en + ru) для совместимости
fun SpellEntity.toSpell(): Spell {
    return Spell(
        en = SpellInfo(
            name = nameEn, level = level, school = schoolEn,
            castingTime = castingTimeEn, range = rangeEn, components = componentsEn,
            duration = durationEn, materials = materialsEn, text = textEn, source = source
        ),
        ru = SpellInfo(
            name = nameRu, level = level, school = schoolRu,
            castingTime = castingTimeRu, range = rangeRu, components = componentsRu,
            duration = durationRu, materials = materialsRu, text = textRu, source = source
        )
    )
}
