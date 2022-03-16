package com.hootor.tmc_2.domain.tmc.tree

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.tmc.TMCTree

interface GetTMCTreeQrCodeRepository {
    fun getTreeByQrCode(qrCode: String): Either<Failure, List<TMCTree>>
}