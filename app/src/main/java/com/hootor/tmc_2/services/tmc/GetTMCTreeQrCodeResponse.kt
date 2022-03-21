package com.hootor.tmc_2.services.tmc

import com.hootor.tmc_2.domain.tmc.TMCTree
import com.hootor.tmc_2.services.core.BaseResponse

class GetTMCTreeQrCodeResponse(
    success: Int,
    message: String,
    var tmcTree: TMCTree? = null
) : BaseResponse(success, message)