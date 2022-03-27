package com.hootor.tmc_2.data.room.inventory.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "inventoryDocsItems",
    foreignKeys = [
        ForeignKey(
            entity = TMCDbInventory::class,
            parentColumns = ["uuid"],
            childColumns = ["inventoryDoc_uuid"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    primaryKeys = ["inventoryDoc_uuid", "tms_uuid", "number_string"],
    indices = [
        Index("inventoryDoc_uuid"),
        Index("inventoryDoc_uuid", "tms_uuid")
    ]
)
data class TMCDbInventoryItems(
    @ColumnInfo(name = "inventoryDoc_uuid") val doc: String,
    @ColumnInfo(name = "tms_uuid") val tms: String,
    @ColumnInfo(name = "tms_title") val tmsTitle: String,
    @ColumnInfo(name = "tms_parent") val tmsParent: String = "",
    @ColumnInfo(name = "number_string") val numberString: Int,
    @ColumnInfo(name = "accounting_quantity") val accountingQuantity: Double,
    @ColumnInfo(name = "actual_quantity") val actualQuantity: Double,
)
