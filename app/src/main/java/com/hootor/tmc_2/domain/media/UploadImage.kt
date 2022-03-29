package com.hootor.tmc_2.domain.media

import com.hootor.tmc_2.di.IoDispatcher
import com.hootor.tmc_2.di.MainDispatcher
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.interactor.UseCase
import com.hootor.tmc_2.domain.scanning.ScanningTMCRepository
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class UploadImage @Inject constructor(
    private val tmcRepository: ScanningTMCRepository,
    @IoDispatcher
    backgroundContext: CoroutineContext,
    @MainDispatcher
    foregroundContext: CoroutineContext,
) : UseCase<Boolean, UploadImage.Params>(backgroundContext, foregroundContext) {

    class Params(
        val qrCode: String,
        val image: String,
    )

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        return tmcRepository.uploadImage(params.qrCode, params.image)
    }
}