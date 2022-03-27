package com.hootor.tmc_2.data.room.tmc.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tmc",
    indices = [
        Index("uuid", unique = true),
        Index("uuid", "parent", unique = true)
    ]
)
data class TMCDbEntity(
    @ColumnInfo(name="uuid") @PrimaryKey val uuid: String,
    @ColumnInfo(name="parent") val parent: String = "",
    @ColumnInfo(name="title") val title: String,
    @ColumnInfo(name="expanded") val expanded: Boolean = false,
    @ColumnInfo(name="expandable") val expandable: Boolean = false
)