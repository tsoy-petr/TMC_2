package com.hootor.tmc_2.domain.scanning

import com.hootor.tmc_2.di.IoDispatcher
import com.hootor.tmc_2.di.MainDispatcher
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.interactor.UseCase
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GetTMCByQrCode @Inject constructor(
    private val scanningTMCRepository: ScanningTMCRepository,
    @IoDispatcher
    backgroundContext: CoroutineContext,
    @MainDispatcher
    foregroundContext: CoroutineContext,
) : UseCase<TMC, GetTMCByQrCode.Params>(
    backgroundContext = backgroundContext,
    foregroundContext = foregroundContext
) {

    data class Params(val qrCode: String)

    override suspend fun run(params: Params): Either<Failure, TMC> {
        return scanningTMCRepository.fetchTMCFields(params.qrCode)
    }
}

class GetImagesTMCByQrCode @Inject constructor(
    private val scanningTMCRepository: ScanningTMCRepository,
    @IoDispatcher
    backgroundContext: CoroutineContext,
    @MainDispatcher
    foregroundContext: CoroutineContext,
) : UseCase<List<TMCSliderItem>, GetImagesTMCByQrCode.Params>(
    backgroundContext = backgroundContext,
    foregroundContext = foregroundContext
) {

    data class Params(val qrCode: String)

    override suspend fun run(params: Params): Either<Failure, List<TMCSliderItem>> =
        scanningTMCRepository.fetchPhoto(params.qrCode)
}
