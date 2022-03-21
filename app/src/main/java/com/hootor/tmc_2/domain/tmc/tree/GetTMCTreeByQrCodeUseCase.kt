package com.hootor.tmc_2.domain.tmc.tree

import com.hootor.tmc_2.di.IoDispatcher
import com.hootor.tmc_2.di.MainDispatcher
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.interactor.UseCase
import com.hootor.tmc_2.domain.tmc.TMCTree
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class GetTMCTreeByQrCodeUseCase @Inject constructor(
    private val getTMCTreeQrCodeRepository: GetTMCTreeQrCodeRepository,
    @IoDispatcher
    backgroundContext: CoroutineContext,
    @MainDispatcher
    foregroundContext: CoroutineContext,
) : UseCase<TMCTree, GetTMCTreeByQrCodeUseCase.Param>(backgroundContext = backgroundContext,
    foregroundContext = foregroundContext) {

    data class Param(val qrCode: String)

    override suspend fun run(params: Param) =
        getTMCTreeQrCodeRepository.getTreeByQrCode(params.qrCode)
}
