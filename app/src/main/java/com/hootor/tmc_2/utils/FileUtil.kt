package com.hootor.tmc_2.utils

import java.text.SimpleDateFormat
import java.util.*

object FileUtil {

    fun getFileName() = "IMG_${getTimestamp()}"

    private fun getTimestamp(): String {
        val timeFormat = "yyyy-MM-dd-HH-mm-ss-SSS"
        return SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
    }
}