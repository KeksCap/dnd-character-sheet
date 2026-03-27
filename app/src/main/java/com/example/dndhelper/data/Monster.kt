package com.example.dndhelper.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monsters")
data class Monster(
    // В логе: name = 'id', type = 'INTEGER', notNull = 'true', primaryKeyPosition = '1'
    @PrimaryKey val id: Int,
    val slug: String?,
    val name: String?,
    val size: String?,
    val type: String?,
    val alignment: String?,

    // В логе: name = 'armor_class', type = 'INTEGER', notNull = 'false'
    @ColumnInfo(name = "armor_class") val armorClass: Int?,

    // В логе: name = 'hit_points', type = 'INTEGER', notNull = 'false'
    @ColumnInfo(name = "hit_points") val hitPoints: Int?,

    // В логе: name = 'cr', type = 'REAL', notNull = 'false'
    val cr: Double?,

    // В логе: name = 'document', type = 'TEXT', notNull = 'false'
    val document: String?,

    // В логе: name = 'raw_data', type = 'TEXT', notNull = 'false'
    @ColumnInfo(name = "raw_data") val rawData: String?
)