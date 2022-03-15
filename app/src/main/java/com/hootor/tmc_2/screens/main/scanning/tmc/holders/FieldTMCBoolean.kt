package com.hootor.tmc_2.screens.main.scanning.tmc.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.ItemTmcFieldBooleanBinding
import com.hootor.tmc_2.screens.main.core.BaseViewHolder
import com.hootor.tmc_2.screens.main.core.Item
import com.hootor.tmc_2.screens.main.core.ItemTMC

class FieldTMCBoolean : ItemTMC<ItemTmcFieldBooleanBinding, TMCItemBoolean> {
    override fun isRelativeItem(item: Item) = item is TMCItemBoolean

    override fun getLayoutId() = R.layout.item_tmc_field_boolean

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
    ): BaseViewHolder<ItemTmcFieldBooleanBinding, TMCItemBoolean> {
        val binding = ItemTmcFieldBooleanBinding.inflate(layoutInflater, parent, false)
        return FieldTMCBooleanViewHolder(binding)
    }

    override fun getDiffUtil(): DiffUtil.ItemCallback<TMCItemBoolean> = object : DiffUtil.ItemCallback<TMCItemBoolean>() {

        override fun areItemsTheSame(oldItem: TMCItemBoolean, newItem: TMCItemBoolean) = oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: TMCItemBoolean, newItem: TMCItemBoolean) = oldItem == newItem

    }
}

class FieldTMCBooleanViewHolder(
    binding: ItemTmcFieldBooleanBinding,
) : BaseViewHolder<ItemTmcFieldBooleanBinding, TMCItemBoolean>(binding) {

    override fun onBind(item: TMCItemBoolean) {
        super.onBind(item)
        with(binding){
            checkBoxFieldTMC.isChecked = item.isChecked
            checkBoxFieldTMC.text = item.title
        }
    }

}

data class TMCItemBoolean(
    val title: String,
    val isChecked: Boolean,
) : Item