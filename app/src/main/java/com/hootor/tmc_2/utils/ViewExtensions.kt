package com.hootor.tmc_2.utils

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.getTrimText() : String = editText?.text.toString().trim()
