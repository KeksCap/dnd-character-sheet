package com.example.dndhelper.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class MagicItemRepository(private val context: Context) {
    private val gson = Gson()

    fun loadMagicItems(): List<MagicItem> {
        return try {
            val inputStream = context.assets.open("magic_items2024.json")
            val reader = InputStreamReader(inputStream)
            val typeToken = object : TypeToken<List<MagicItem>>() {}.type
            val items: List<MagicItem> = gson.fromJson(reader, typeToken)
            reader.close()
            Log.d("DND_LOG", "Loaded ${items.size} magic items from JSON")
            items
        } catch (e: Exception) {
            Log.e("DND_LOG", "Error loading magic items: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}
