package com.example.dndhelper.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MonsterDao {
    @Query("SELECT * FROM monsters ORDER BY name ASC")
    suspend fun getAllMonsters(): List<Monster> // Теперь тут обычный List!
}