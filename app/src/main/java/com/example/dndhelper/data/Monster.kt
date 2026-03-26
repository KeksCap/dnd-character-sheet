package com.example.dndhelper.data // <-- Убедись, что тут твой пакет!

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity говорит Андроиду, что это таблица в базе данных
@Entity(tableName = "bestiary_table")
data class Monster(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Уникальный номер монстра (генерируется сам)

    val name: String,        // Название (например, "Взрослый красный дракон")
    val type: String,        // Тип (например, "Дракон", "Нежить", "Зверь")
    val size: String,        // Размер ("Огромный", "Средний")
    val challengeRating: String, // Опасность (CR), например "1/4", "5", "17"
    val xp: Int,             // Опыт за убийство

    val imageUrl: String?,   // Ссылка на картинку (может быть null)

    // Сюда будем пихать весь остальной JSON (статы, атаки)
    val detailsJson: String
)