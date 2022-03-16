package com.hootor.tmc_2.data.tmcTree

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.tmc.TMCTree
import com.hootor.tmc_2.domain.tmc.tree.GetTMCTreeQrCodeRepository
import com.hootor.tmc_2.services.TMCService
import com.hootor.tmc_2.services.core.Request
import javax.inject.Inject

class GetTMCTreeQrCodeRepositoryImpl @Inject constructor(
    private val request: Request,
    private val tmcService: TMCService,
) : GetTMCTreeQrCodeRepository {
    override fun getTreeByQrCode(qrCode: String): Either<Failure, List<TMCTree>> {
        return request.make(
            tmcService.getTMCTreeByQrCode(createResponseMap(qrCode))
        ) {
            it.tmcTree
        }
    }

    private fun createResponseMap(qrCode: String) = HashMap<String, String>().apply {
        put("qrCode", qrCode)
    }

}