package com.hootor.tmc_2.services.tmc

import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.services.core.BaseResponse

class GetTMCByQrCodeResponse(
    success: Int,
    message: String,
    var tmc: TMC = TMC.empty()
): BaseResponse(success, message)