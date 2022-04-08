package com.hootor.tmc_2.services.tmc

import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import com.hootor.tmc_2.services.core.BaseResponse

class GetImagesByQrCode(
    success: Int,
    message: String,
    val images: List<TMCSliderItem>? = emptyList()
) : BaseResponse(success, message)