package com.example.dndhelper.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class StandardItem(
    val nameRu: String,
    val nameEn: String,
    val costRu: String,
    val costEn: String,
    val weightRu: String,
    val weightEn: String,
    val category: String,
    val damage: String? = null,
    val damageEn: String? = null,
    val acRu: String? = null,
    val acEn: String? = null,
    val baseAc: Int? = null, // Число для расчетов
    val type: Int? = null    // 0=None, 1=Light, 2=Medium, 3=Heavy
)

data class EquipmentData(
    val weapons: List<StandardItem>,
    val armor: List<StandardItem>,
    val gear: List<StandardItem>
)

object StandardEquipment {
    var items by mutableStateOf<EquipmentData?>(null)

    fun initialize(context: Context) {
        if (items != null) return
        try {
            val jsonString = context.assets.open("equipment2024.json").bufferedReader().use { it.readText() }
            items = Gson().fromJson(jsonString, EquipmentData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getWeapons(): List<StandardItem> = items?.weapons ?: emptyList()
    fun getArmor(): List<StandardItem> = items?.armor ?: emptyList()
    fun getGear(): List<StandardItem> = items?.gear ?: emptyList()
}
