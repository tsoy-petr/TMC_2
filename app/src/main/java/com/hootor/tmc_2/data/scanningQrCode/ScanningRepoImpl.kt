package com.hootor.tmc_2.data.scanningQrCode

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.scanning.ScanningTMCRepository
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.services.TMCService
import com.hootor.tmc_2.services.core.Request
import javax.inject.Inject

class ScanningRepoImpl @Inject constructor(
    private val request: Request,
    private val tmcService: TMCService,
) :
    ScanningTMCRepository {
    override suspend fun fetchTMCFields(qrCode: String): Either<Failure, TMC> = request.make(
        tmcService.getTMCBtQrCode(createGetTMCByQrCodeResponseMap(qrCode))
    ) {
        it.tmc
    }


    private fun createGetTMCByQrCodeResponseMap(qrCode: String) = HashMap<String, String>().apply {
        put("qrCode", qrCode)
    }

}