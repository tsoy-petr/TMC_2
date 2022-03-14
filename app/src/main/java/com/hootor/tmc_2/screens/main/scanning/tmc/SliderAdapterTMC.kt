package com.hootor.tmc_2.screens.main.scanning.tmc

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.hootor.tmc_2.R
import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.databinding.ItemTmsImageSlideBinding
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import com.hootor.tmc_2.services.TMCService.Companion.GET_TMC_IMG
import com.smarteist.autoimageslider.SliderViewAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class SliderAdapterTMC(private val prefs: Prefs) : SliderViewAdapter<SliderAdapterTMC.SliderAdapterVH>() {

    var items: List<TMCSliderItem> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    override fun getCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTmsImageSlideBinding.inflate(inflater, parent, false)
        return SliderAdapterVH(binding)
    }

    override fun onBindViewHolder(holder: SliderAdapterVH, position: Int) {
        val itemField = items[position]
        holder.itemView.tag = itemField

        val auth = LazyHeaders.Builder()
            .addHeader("Authorization", prefs.getCredentials()).build()

        Glide.with(holder.itemView.context)
            .load(GlideUrl(prefs.getUrlServer() + GET_TMC_IMG + "/" + itemField.imageUrl, auth))
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_error_load_img)
            .centerCrop()
            .transform(RoundedCornersTransformation(30, 10))
            .into(holder.binding.imgSlide)

    }

    class SliderAdapterVH(val binding: ItemTmsImageSlideBinding) :
        SliderViewAdapter.ViewHolder(binding.root)

}
