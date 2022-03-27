package com.hootor.tmc_2.data.room.inventory.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventoryDocs",
    indices = [
        Index("uuid", unique = true),
        Index("uuid", "date", unique = true)
    ]
)
data class TMCDbInventory(
    @ColumnInfo(name="uuid") @PrimaryKey val uuid: String,
    @ColumnInfo(name="mol") val mol: String = "",
    @ColumnInfo(name="number") val number: String,
    @ColumnInfo(name="date")  val date: Long,
    @ColumnInfo(name="subdivision")  val subdivision: String = ""
)
