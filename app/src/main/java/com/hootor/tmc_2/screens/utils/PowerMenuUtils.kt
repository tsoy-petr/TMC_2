package com.hootor.tmc_2.screens.utils

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.LifecycleOwner
import com.hootor.tmc_2.R
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem

object PowerMenuUtils {

    fun getIconPowerMenu(
        context: Context,
        lifecycleOwner: LifecycleOwner?,
        onMenuItemClickListener: OnMenuItemClickListener<PowerMenuItem>,
    ): PowerMenu {
        val styledContext: Context = ContextThemeWrapper(context, R.style.PopupCardThemeOverlay)
        return PowerMenu.Builder(styledContext)
            .addItem(PowerMenuItem("Открыть карточку ТМЦ", R.drawable.ic_open_in_new))
            .setLifecycleOwner(lifecycleOwner!!)
            .setOnMenuItemClickListener(onMenuItemClickListener)
            .setAnimation(MenuAnimation.FADE)
            .setMenuRadius(context.resources.getDimensionPixelSize(R.dimen.menu_corner_radius)
                .toFloat())
            .setMenuShadow(context.resources.getDimensionPixelSize(R.dimen.menu_elevation)
                .toFloat())
            .setIsMaterial(true)
            .build()
    }
}