package com.hootor.tmc_2.screens.main.scanning.tmc.holders

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.hootor.tmc_2.R
import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.databinding.ItemHorizontalSliderBinding
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import com.hootor.tmc_2.screens.main.core.BaseViewHolder
import com.hootor.tmc_2.screens.main.core.Item
import com.hootor.tmc_2.screens.main.core.ItemTMC
import com.hootor.tmc_2.screens.main.scanning.tmc.SliderAdapterTMC
import com.smarteist.autoimageslider.SliderView

class HorizontalImgTMC(
    private val prefs: Prefs
) : ItemTMC<ItemHorizontalSliderBinding, HorizontalItem> {

    override fun isRelativeItem(item: Item) = item is HorizontalItem

    override fun getLayoutId() = R.layout.item_horizontal_slider

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
    ): BaseViewHolder<ItemHorizontalSliderBinding, HorizontalItem> {

        Log.i("happy", "HorizontalImgTMC.getViewHolder")
        val binding= ItemHorizontalSliderBinding.inflate(layoutInflater)
        return HorizontalImgViewHolder(binding, prefs)
    }

    override fun getDiffUtil()= object : DiffUtil.ItemCallback<HorizontalItem>() {

        override fun areItemsTheSame(oldItem: HorizontalItem, newItem: HorizontalItem) = oldItem == newItem

        override fun areContentsTheSame(oldItem: HorizontalItem, newItem: HorizontalItem) = oldItem == newItem

    }
}

class HorizontalImgViewHolder(
    binding: ItemHorizontalSliderBinding,
    prefs: Prefs,
) : BaseViewHolder<ItemHorizontalSliderBinding, HorizontalItem>(binding) {

    private val adapterImg = SliderAdapterTMC(prefs)

    init {
        with(binding.imageSliderItem) {
            setSliderAdapter(adapterImg)
            autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
            indicatorSelectedColor = Color.WHITE
            indicatorUnselectedColor = Color.GRAY
            scrollTimeInSec = 30
        }
    }

    override fun onBind(item: HorizontalItem) {
        super.onBind(item)
        adapterImg.items = item.items
        Log.i("happy", "HorizontalImgViewHolder.onBind")
    }

}

data class HorizontalItem(
    val items: List<TMCSliderItem>,
) : Item