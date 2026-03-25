package com.example.dndhelper.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class SpellRepository(private val context: Context) {
    private val gson = Gson()

    fun loadSpellsFromAssets(): List<Spell> {
        return try {
            val inputStream = context.assets.open("allSpells.json")
            val reader = InputStreamReader(inputStream)

            // 1. ВАЖНО: Теперь мы говорим Gson, что читаем СПИСОК (List), а не Map
            val listType = object : TypeToken<List<Spell>>() {}.type

            // 2. Читаем список напрямую
            val spellList: List<Spell> = gson.fromJson(reader, listType)

            reader.close()
            Log.d("DND_LOG", "✅ УСПЕХ! Загружено: ${spellList.size} заклинаний")

            spellList
        } catch (e: Exception) {
            Log.e("DND_LOG", "❌ ОШИБКА: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    // Добавим эту функцию внутрь класса SpellRepository
    fun loadClassSpells(): Map<String, List<String>> {
        return try {
            val inputStream = context.assets.open("ClassSpells.json")
            val reader = InputStreamReader(inputStream)
            // Читаем как словарь: Ключ (Название класса) -> Список строк (Названия заклинаний)
            val typeToken = object : TypeToken<Map<String, List<String>>>() {}.type
            val classSpells: Map<String, List<String>> = gson.fromJson(reader, typeToken)
            reader.close()
            classSpells
        } catch (e: Exception) {
            Log.e("DND_LOG", "Ошибка загрузки классов: ${e.message}")
            emptyMap()
        }
    }
}