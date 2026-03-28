package com.example.dndhelper.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

// Вспомогательные классы для ClassSpells.json (оставляем как есть)
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

    // Открываем БД заклинаний
    private val spellDb by lazy { SpellDatabase.getDatabase(context) }

    // Загружаем все заклинания из БД (возвращаем в старом формате List<Spell> для совместимости с UI)
    suspend fun loadSpellsFromDb(): List<Spell> {
        return try {
            val entities = spellDb.spellDao().getAllSpells()
            Log.d("DND_LOG", "Loaded ${entities.size} spells from DB")
            entities.map { it.toSpell() }
        } catch (e: Exception) {
            Log.e("DND_LOG", "Error loading spells from DB: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // Поиск заклинаний по имени (для фильтра по классу)
    suspend fun getSpellsByNames(names: List<String>): List<Spell> {
        return try {
            spellDb.spellDao().getSpellsByName(names).map { it.toSpell() }
        } catch (e: Exception) {
            Log.e("DND_LOG", "Error searching spells: ${e.message}")
            emptyList()
        }
    }

    // ClassSpells.json остаётся без изменений — он небольшой и не несёт ценных данных
    fun loadClassSpells(): Map<String, List<String>> {
        return try {
            val inputStream = context.assets.open("ClassSpells.json")
            val reader = InputStreamReader(inputStream)
            val typeToken = object : TypeToken<Map<String, ClassData>>() {}.type
            val parsedData: Map<String, ClassData> = gson.fromJson(reader, typeToken)
            reader.close()

            val result = mutableMapOf<String, List<String>>()
            for ((key, classData) in parsedData) {
                val className = classData.title?.ru ?: key
                val spells = classData.spells ?: emptyList()
                result[className] = spells
            }
            result
        } catch (e: Exception) {
            Log.e("DND_LOG", "Error loading class spells: ${e.message}")
            emptyMap()
        }
    }
}