package com.hootor.tmc_2.domain.tmc

data class Inventory(
    val uuid: String,
    val mol: String = "",
    val number: String,
    val date: Long,
    val subdivision: String = "",
)

data class InventoryItemString(
    val doc: String,
    val tms: String,
    val tmsTitle: String,
    val tmsParent: String = "",
    val numberString: Int,
    val accountingQuantity: Double,
    val actualQuantity: Double,
)
