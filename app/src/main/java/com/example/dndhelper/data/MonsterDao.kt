package com.example.dndhelper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

@Dao
@JvmSuppressWildcards
interface MonsterDao {
    // Получение данных через Flow — это уже асинхронно, suspend тут не нужен
    @Query("SELECT * FROM bestiary_table ORDER BY name ASC")
    fun getAllMonsters(): Flow<List<Monster>>

    // Добавляем suspend и ОБЯЗАТЕЛЬНО возвращаем List<Long> (список ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(monsters: List<Monster>): List<Long>

    // Добавляем suspend и ОБЯЗАТЕЛЬНО возвращаем Int (количество удаленных строк)
    @Query("DELETE FROM bestiary_table")
    suspend fun clearBestiary(): Int
}