package com.hootor.tmc_2.screens.main.scanning.tmc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.ItemTmcFieldBinding
import com.hootor.tmc_2.screens.main.core.BaseViewHolder
import com.hootor.tmc_2.screens.main.core.Item
import com.hootor.tmc_2.screens.main.core.ItemTMC

class FieldTMCDescription : ItemTMC<ItemTmcFieldBinding, TMCItem> {

    override fun isRelativeItem(item: Item) = item is TMCItem

    override fun getLayoutId() = R.layout.item_tmc_field

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
    ): BaseViewHolder<ItemTmcFieldBinding, TMCItem> {
        val binding = ItemTmcFieldBinding.inflate(layoutInflater, parent, false)
        return FieldTMCDescriptionViewHolder(binding)
    }

    override fun getDiffUtil(): DiffUtil.ItemCallback<TMCItem> = object : DiffUtil.ItemCallback<TMCItem>() {

        override fun areItemsTheSame(oldItem: TMCItem, newItem: TMCItem) = oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: TMCItem, newItem: TMCItem) = oldItem == newItem

    }
}

class FieldTMCDescriptionViewHolder(binding: ItemTmcFieldBinding) : BaseViewHolder<ItemTmcFieldBinding, TMCItem>(binding) {

    override fun onBind(item: TMCItem) {
        super.onBind(item)
        with(binding){
            titleTmcTextView.text = item.description
            descriptionTmcTextView.text = item.description
        }
    }

}

data class TMCItem(
    val title: String,
    val description: String,
) : Item