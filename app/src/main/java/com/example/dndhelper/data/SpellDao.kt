package com.example.dndhelper.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SpellDao {
    @Query("SELECT * FROM spells ORDER BY CAST(level AS INTEGER) ASC, name_en ASC")
    suspend fun getAllSpells(): List<SpellEntity>

    @Query("SELECT * FROM spells WHERE name_en LIKE :query OR name_ru LIKE :query ORDER BY CAST(level AS INTEGER) ASC, name_en ASC")
    suspend fun searchSpells(query: String): List<SpellEntity>

    @Query("SELECT * FROM spells WHERE name_en IN (:names) OR name_ru IN (:names) ORDER BY CAST(level AS INTEGER) ASC, name_en ASC")
    suspend fun getSpellsByName(names: List<String>): List<SpellEntity>
}
