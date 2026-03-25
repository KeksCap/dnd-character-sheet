package com.example.dndhelper.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

// 1. Добавляем классы-помощники, чтобы Gson понимал структуру твоего ClassSpells.json
data class ClassData(
    val title: TitleData?,
    val spells: List<String>?
)

data class TitleData(
    val en: String?,
    val ru: String?
)

class SpellRepository(private val context: Context) {
    private val gson = Gson()

    fun loadSpellsFromAssets(): List<Spell> {
        return try {
            val inputStream = context.assets.open("allSpells.json")
            val reader = InputStreamReader(inputStream)

            val listType = object : TypeToken<List<Spell>>() {}.type
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

    fun loadClassSpells(): Map<String, List<String>> {
        return try {
            val inputStream = context.assets.open("ClassSpells.json")
            val reader = InputStreamReader(inputStream)

            // 2. Читаем JSON в правильном формате (Словарь объектов ClassData)
            val typeToken = object : TypeToken<Map<String, ClassData>>() {}.type
            val parsedData: Map<String, ClassData> = gson.fromJson(reader, typeToken)
            reader.close()

            // 3. Превращаем это в простой словарь для интерфейса: "Имя на русском" -> "Список заклинаний"
            val result = mutableMapOf<String, List<String>>()
            for ((key, classData) in parsedData) {
                // Если есть русский перевод — берем его, иначе оставляем английский ключ
                val className = classData.title?.ru ?: key
                val spells = classData.spells ?: emptyList()
                result[className] = spells
            }

            result
        } catch (e: Exception) {
            Log.e("DND_LOG", "Ошибка загрузки классов: ${e.message}")
            emptyMap()
        }
    }
}