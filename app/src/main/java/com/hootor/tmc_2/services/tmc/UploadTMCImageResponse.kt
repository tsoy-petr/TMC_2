package com.hootor.tmc_2.services.tmc

import com.hootor.tmc_2.services.core.BaseResponse

class UploadTMCImageResponse(
    success: Int,
    message: String,
    val result: Boolean? = null
) : BaseResponse(success, message)