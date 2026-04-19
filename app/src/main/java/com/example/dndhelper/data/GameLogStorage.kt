package com.example.dndhelper.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Отдельное хранилище для игрового журнала.
 * Лог привязан к персонажу (по ID), но хранится ОТДЕЛЬНО от данных персонажа,
 * чтобы не переносить его при экспорте/импорте через QR.
 */
class GameLogStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dnd_game_log", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<GameLogEntry>>() {}.type

    fun getLog(characterId: String): List<GameLogEntry> {
        val json = prefs.getString(characterId, null) ?: return emptyList()
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveLog(characterId: String, entries: List<GameLogEntry>) {
        prefs.edit().putString(characterId, gson.toJson(entries)).apply()
    }

    fun clearLog(characterId: String) {
        prefs.edit().remove(characterId).apply()
    }

    fun deleteForCharacter(characterId: String) {
        prefs.edit().remove(characterId).apply()
    }
}
