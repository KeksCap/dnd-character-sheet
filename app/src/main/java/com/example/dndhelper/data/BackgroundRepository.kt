package com.example.dndhelper.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class BackgroundRepository(private val context: Context) {
    private val gson = Gson()

    fun loadBackgrounds(): List<Background> {
        return try {
            val inputStream = context.assets.open("backgrounds.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Background>>() {}.type
            val result: List<Background> = gson.fromJson(reader, type)
            reader.close()
            result
        } catch (e: Exception) {
            Log.e("DND_LOG", "Error loading backgrounds: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}
