package com.hootor.tmc_2.domain.tmc.tree

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.interactor.UseCase
import com.hootor.tmc_2.domain.tmc.TMCTree
import javax.inject.Inject

class GetTMCTreeByQrCodeUseCase @Inject constructor(
    private val getTMCTreeQrCodeRepository: GetTMCTreeQrCodeRepository
) : UseCase<List<TMCTree>, GetTMCTreeByQrCodeUseCase.Param>() {

    data class Param(val qrCode: String)

    override suspend fun run(params: Param) = getTMCTreeQrCodeRepository.getTreeByQrCode(params.qrCode)

}