package com.example.dndhelper.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class CharacterStorage(context: Context) {
    // SharedPreferences - это встроенный в Android сейф для настроек и мелких данных
    private val prefs: SharedPreferences = context.getSharedPreferences("dnd_characters", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 1. Сохранить или обновить персонажа
    fun saveCharacter(character: CharacterSaveData) {
        val jsonString = gson.toJson(character)
        prefs.edit().putString(character.id, jsonString).apply()
    }

    // 2. Получить список всех сохраненных персонажей (для Таверны)
    fun getAllCharacters(): List<CharacterSaveData> {
        val allEntries = prefs.all
        val characters = mutableListOf<CharacterSaveData>()

        for ((_, value) in allEntries) {
            if (value is String) {
                try {
                    // Распаковываем JSON обратно в персонажа
                    val char = gson.fromJson(value, CharacterSaveData::class.java)
                    characters.add(char)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return characters
    }

    // 3. Удалить персонажа (если его сожрал дракон)
    fun deleteCharacter(characterId: String) {
        prefs.edit().remove(characterId).apply()
    }
}