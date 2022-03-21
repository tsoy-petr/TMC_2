package com.hootor.tmc_2.screens.view

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.matchParentConstraint() {
    if (parent !is ConstraintLayout) return
    with((layoutParams as ConstraintLayout.LayoutParams)) {
        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
    }
}

fun FloatingActionButton.bottomEndParentConstraint() {
    if (parent !is ConstraintLayout) return
    with((layoutParams as ConstraintLayout.LayoutParams)) {
        topToTop = ConstraintLayout.LayoutParams.UNSET
        startToStart = ConstraintLayout.LayoutParams.UNSET
        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
    }
}

fun View.getMarginBottom(): Int {
    var marginBottom = 0
    val mlayoutParams = this.layoutParams
    if (mlayoutParams is ViewGroup.MarginLayoutParams) marginBottom = mlayoutParams.bottomMargin
    return marginBottom
}

private fun View.isVisible(bool: Boolean?, nonVisibleState: Int = View.GONE) {
    visibility = if (bool == true) View.VISIBLE else nonVisibleState
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

