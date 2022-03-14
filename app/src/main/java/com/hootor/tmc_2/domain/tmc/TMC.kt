package com.hootor.tmc_2.domain.tmc

data class TMC(
    val fields: List<ItemField> = emptyList(),
    val images: List<TMCSliderItem> = emptyList()
) {
    companion object {
        fun empty() = TMC(emptyList(), emptyList())
    }
}

data class ItemField(
    val title: String,
    val type: String,
    val description: String
)

data class TMCSliderItem(
    val description: String,
    val imageUrl: String
)
