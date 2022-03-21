package com.hootor.tmc_2.screens.main.tmcTree

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hootor.tmc_2.R
import com.hootor.tmc_2.databinding.ItemTmcFieldBinding
import com.hootor.tmc_2.domain.tmc.TMCTree
import io.github.ikws4.treeview.TreeItem
import io.github.ikws4.treeview.TreeView

class TMCTreeAdapter(
    private val onClickItem: (item:TMCTree, view: View)->Unit
) : TreeView.Adapter<TMCTreeAdapter.ViewHolder, TMCTree>() {

    override fun onCreateViewHolder(view: View) = ViewHolder(view)


    override fun getLayoutRes() = R.layout.item_tmc_tree

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item: TreeItem<TMCTree> = items[position]
        holder.tv.text = item.value.title
        holder.menuItem.setOnClickListener {
            onClickItem(item.value, holder.menuItem)
        }
        if (item.isExpandable) {
            Log.i("TMCTreeAdapter", "item.isExpandable = ${item.isExpandable})")
            if (item.isExpanded) {
                holder.img.setImageResource(R.drawable.ic_group_open)
                Log.i("TMCTreeAdapter", "item.isExpanded = ${item.isExpanded})")
            } else {
                holder.img.setImageResource(R.drawable.ic_group_close)
                Log.i("TMCTreeAdapter", "item.isExpanded = ${item.isExpanded})")
            }
        } else {
            Log.i("TMCTreeAdapter", "item.isExpandable = ${item.isExpandable})")
            holder.img.setImageResource(R.drawable.ic_baseline_minimize_24)
        }
    }

    inner class ViewHolder(itemView: View) : TreeView.ViewHolder(itemView) {
        var img: ImageView = itemView.findViewById(R.id.iconTmcTree)
        var tv: TextView = itemView.findViewById(R.id.textViewTmcTree)
        var menuItem: ImageView = itemView.findViewById(R.id.imgMenuItem)
    }

}