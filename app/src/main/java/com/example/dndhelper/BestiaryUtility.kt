package com.example.dndhelper

import com.example.dndhelper.data.MonsterRawData
import com.google.gson.Gson

// Глобальный парсер
private val gson = Gson()

// Функция парсинга JSON из БД
fun parseMonsterRawData(json: String?): MonsterRawData? {
    if (json == null) return null
    return try {
        gson.fromJson(json, MonsterRawData::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ПРАВИЛЬНЫЙ ГРАММАТИЧЕСКИЙ ПЕРЕВОД
// Берет размер И ТИП, чтобы согласовать роды (ОгромнОЕ чудовище, МаленькИЙ зверь)
fun translateSizeAndType(size: String?, type: String?, language: String = "ru"): String {
    val s = size?.lowercase() ?: "?"
    val t = type?.lowercase() ?: "?"

    if (language == "en") {
        return "${s.replaceFirstChar { it.uppercase() }} ${t.replaceFirstChar { it.uppercase() }}"
    }

    // Сначала переведем тип, чтобы понять род
    val typeRu = when (t) {
        "aberration" -> "аберрация"
        "beast" -> "зверь"
        "celestial" -> "небожитель"
        "construct" -> "конструкт"
        "dragon" -> "дракон"
        "elemental" -> "элементаль"
        "fey" -> "фея"
        "fiend" -> "исчадие"
        "giant" -> "великан"
        "humanoid" -> "гуманоид"
        "monstrosity" -> "чудовище"
        "ooze" -> "слизь"
        "plant" -> "растение"
        "undead" -> "нежить"
        else -> type ?: "?"
    }

    // Теперь переводим размер, согласуя с родом типа
    val sizeRu = when {
        // ЖЕНСКИЙ РОД (аберрация, фея, нежить)
        t in listOf("aberration", "fey", "ooze", "undead") -> when (s) {
            "tiny" -> "Крошечная"
            "small" -> "Маленькая"
            "medium" -> "Средняя"
            "large" -> "Большая"
            "huge" -> "Огромная"
            "gargantuan" -> "Колоссальная"
            else -> size ?: "?"
        }
        // СРЕДНИЙ РОД (чудовище, растение)
        t in listOf("monstrosity", "plant") -> when (s) {
            "tiny" -> "Крошечное"
            "small" -> "Маленькое"
            "medium" -> "Среднее"
            "large" -> "Большое"
            "huge" -> "Огромное"
            "gargantuan" -> "Колоссальное"
            else -> size ?: "?"
        }
        // МУЖСКОЙ РОД (зверь, гуманоид, дракон, элементаль и т.д.)
        else -> when (s) {
            "tiny" -> "Крошечный"
            "small" -> "Маленький"
            "medium" -> "Средний"
            "large" -> "Большой"
            "huge" -> "Огромный"
            "gargantuan" -> "Колоссальный"
            else -> size ?: "?"
        }
    }

    return "$sizeRu $typeRu"
}

// Перевод мировоззрения
fun translateAlignment(alignment: String?, language: String = "ru"): String {
    if (alignment == null) return "?"
    
    if (language == "en") return alignment.replaceFirstChar { it.uppercase() }

    return alignment.lowercase()
        .replace("chaotic evil", "хаотично-злой")
        .replace("chaotic good", "хаотично-добрый")
        .replace("chaotic neutral", "хаотично-нейтральный")
        .replace("lawful evil", "законно-злой")
        .replace("lawful good", "законно-добрый")
        .replace("lawful neutral", "законно-нейтральный")
        .replace("neutral evil", "нейтрально-злой")
        .replace("neutral good", "нейтрально-добрый")
        .replace("true neutral", "истинно нейтральный")
        .replace("neutral", "нейтральный")
        .replace("unaligned", "без мировоззрения")
        .replace("any alignment", "любое мировоззрение")
}