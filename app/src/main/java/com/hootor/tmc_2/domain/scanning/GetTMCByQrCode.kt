package com.hootor.tmc_2.domain.scanning

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.interactor.UseCase
import com.hootor.tmc_2.domain.tmc.TMC
import javax.inject.Inject

class GetTMCByQrCode @Inject constructor(
    private val scanningTMCRepository: ScanningTMCRepository,
) : UseCase<TMC, GetTMCByQrCode.Params>() {


    data class Params(val qrCode: String)

    override suspend fun run(params: Params): Either<Failure, TMC> {
        return scanningTMCRepository.fetchTMCFields(params.qrCode)
    }
}