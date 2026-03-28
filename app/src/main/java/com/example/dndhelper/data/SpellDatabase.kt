package com.example.dndhelper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SpellEntity::class], version = 1, exportSchema = false)
abstract class SpellDatabase : RoomDatabase() {
    abstract fun spellDao(): SpellDao

    companion object {
        @Volatile private var INSTANCE: SpellDatabase? = null

        fun getDatabase(context: Context): SpellDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpellDatabase::class.java,
                    "spells.db"
                )
                .createFromAsset("spells.db")   // Копируем из assets при первом запуске
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
