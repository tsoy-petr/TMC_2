package com.hootor.tmc_2.data.room.inventory

import androidx.room.*
import com.hootor.tmc_2.data.room.inventory.entities.TMCDbInventory
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TMCDbInventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: TMCDbInventory)

    @Transaction
    open suspend fun save(list: List<TMCDbInventory>) {
        list.forEach {
            insert(it)
        }
    }

    @Query("SELECT * FROM inventoryDocs")
    abstract fun getFlowList(): Flow<List<TMCDbInventory>>

    @Delete
    abstract suspend fun delete(entity: TMCDbInventory)
}