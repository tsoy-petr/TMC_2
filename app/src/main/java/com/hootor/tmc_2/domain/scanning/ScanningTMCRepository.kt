package com.hootor.tmc_2.domain.scanning

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.tmc.TMC

interface ScanningTMCRepository {
    suspend fun fetchTMCFields(qrCode: String): Either<Failure, TMC>
    suspend fun uploadImage(qrCode: String, image: String): Either<Failure, Boolean>
}