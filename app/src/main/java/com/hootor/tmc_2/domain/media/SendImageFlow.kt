package com.hootor.tmc_2.domain.media

import android.net.Uri
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.interactor.UseCaseFlow
import com.hootor.tmc_2.domain.scanning.ScanningTMCRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendImageFlow @Inject constructor(private val tmcRepository: ScanningTMCRepository) :
    UseCaseFlow<Either<Failure, Boolean>, SendImageFlow.Params>() {
    class Params(val qrCode: String, val uri: Uri)

    override fun run(params: SendImageFlow.Params): Flow<Either<Failure, Boolean>> =
        tmcRepository.sendImageFlow(qrCode = params.qrCode, uri = params.uri)
}