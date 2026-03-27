package com.example.dndhelper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Monster::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monsterDao(): MonsterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private var currentDbName: String = ""

        fun getDatabase(context: Context, dbName: String): AppDatabase {
            // Если имя базы изменилось — закрываем старую и открываем новую
            if (INSTANCE != null && currentDbName != dbName) {
                INSTANCE?.close()
                INSTANCE = null
            }

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .createFromAsset(dbName)
                    .fallbackToDestructiveMigration()
                    .build()
                currentDbName = dbName
                INSTANCE = instance
                instance
            }
        }
    }
}