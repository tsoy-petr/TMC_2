package com.hootor.tmc_2.domain.inventory

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.tmc.Inventory
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    suspend fun getAllInventory(): Either<Failure, Flow<Inventory>>
}