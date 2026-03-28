package com.example.dndhelper.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class RaceRepository(private val context: Context) {
    private val gson = Gson()

    fun loadRaces(version: String): List<Race> {
        val fileName = if (version == "2024") "races2024.json" else "races2014.json"
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Race>>() {}.type
            val result: List<Race> = gson.fromJson(reader, type)
            reader.close()
            result
        } catch (e: Exception) {
            Log.e("DND_LOG", "Error loading races: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}
