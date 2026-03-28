package com.example.dndhelper.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class ClassRepository(private val context: Context) {
    fun loadClasses(version: String): List<DndClass> {
        return try {
            val fileName = "classes$version.json"
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val listType = object : TypeToken<List<DndClass>>() {}.type
            Gson().fromJson(reader, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
