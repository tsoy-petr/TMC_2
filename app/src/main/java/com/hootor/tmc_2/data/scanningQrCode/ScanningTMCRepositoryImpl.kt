package com.hootor.tmc_2.data.scanningQrCode

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.scanning.ScanningTMCRepository
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.services.TMCService
import com.hootor.tmc_2.services.make
import javax.inject.Inject

class ScanningTMCRepositoryImpl
    @Inject constructor(private val tmcService: TMCService): ScanningTMCRepository {

    override suspend fun fetchTMCFields(qrCode: String): Either<Failure, TMC> {

        return tmcService.fetchTMC(qrCode).make(
            {it}, TMC.empty()
        )

    }
}