package com.hootor.tmc_2.screens.main.scanning.tmc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hootor.tmc_2.databinding.ItemTmcFieldBinding
import com.hootor.tmc_2.domain.tmc.ItemField

class TMCFieldsAdapter : RecyclerView.Adapter<TMCFieldsAdapter.TMCViewHolder>() {

    var items: List<ItemField> = emptyList()
        set(newValue) {
            val diffCallback = TMCFieldsDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TMCViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTmcFieldBinding.inflate(inflater, parent, false)

        return TMCViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TMCViewHolder, position: Int) {
        val itemField = items[position]
        with(holder.binding){
            holder.itemView.tag = itemField

            titleTmcTextView.text = itemField.title
            descriptionTmcTextView.text = if(itemField.description.isNotBlank()) itemField.description else "<...>"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemCount() = items.size


    class TMCViewHolder(
        val binding: ItemTmcFieldBinding,
    ) : RecyclerView.ViewHolder(binding.root)


}

class TMCFieldsDiffCallback(
    private val oldList: List<ItemField>,
    private val newList: List<ItemField>,
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldField = oldList[oldItemPosition]
        val newField = newList[newItemPosition]
        return oldField.title == newField.title
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldField = oldList[oldItemPosition]
        val newField = newList[newItemPosition]
        return oldField == newField
    }

}