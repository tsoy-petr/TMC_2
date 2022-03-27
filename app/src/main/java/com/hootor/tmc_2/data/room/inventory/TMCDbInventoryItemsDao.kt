package com.hootor.tmc_2.data.room.inventory

import androidx.room.*
import com.hootor.tmc_2.data.room.inventory.entities.TMCDbInventoryItems
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TMCDbInventoryItemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(item: TMCDbInventoryItems)

    @Transaction
    open suspend fun save(items: List<TMCDbInventoryItems>) {
        items.forEach {
            insert(it)
        }
    }

    @Query("SELECT * FROM inventoryDocsItems WHERE inventoryDoc_uuid = :docUuid")
    abstract fun getItemsByDoc(docUuid: String): Flow<List<TMCDbInventoryItems>>

    @Delete
    abstract suspend fun delete(item: TMCDbInventoryItems)
}