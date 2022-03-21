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
    val description: String,
)

data class TMCSliderItem(
    val description: String,
    val imageUrl: String,
)

data class TMCTree(
    val uuid: String,
    val title: String,
    val children: List<TMCTree> = emptyList(),
    var parent: TMCTree? = null,
    var expanded: Boolean = false,
    val expandable: Boolean = false,
)
