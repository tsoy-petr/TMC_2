package com.hootor.tmc_2.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hootor.tmc_2.data.room.inventory.TMCDbInventoryDao
import com.hootor.tmc_2.data.room.inventory.TMCDbInventoryItemsDao
import com.hootor.tmc_2.data.room.inventory.entities.TMCDbInventory
import com.hootor.tmc_2.data.room.inventory.entities.TMCDbInventoryItems

@Database(
    version = 1,
    entities = [
        TMCDbInventory::class,
        TMCDbInventoryItems::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getTMCDbInventoryDao(): TMCDbInventoryDao
    abstract fun getTMCDbInventoryItemsDao(): TMCDbInventoryItemsDao

}