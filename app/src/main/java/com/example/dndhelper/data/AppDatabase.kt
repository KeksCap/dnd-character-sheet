package com.example.dndhelper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Указываем наши таблицы (пока только Monster) и версию базы
@Database(entities = [Monster::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun monsterDao(): MonsterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monsters.db" // <--- МЕНЯЕМ ИМЯ ФАЙЛА НА НАШЕ
                )
                    .createFromAsset("monsters.db") // <--- МАГИЯ РАСПАКОВКИ ИЗ ASSETS!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}