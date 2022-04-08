package com.hootor.tmc_2.domain.scanning

import android.net.Uri
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import kotlinx.coroutines.flow.Flow

interface ScanningTMCRepository {
    suspend fun fetchTMCFields(qrCode: String): Either<Failure, TMC>
    suspend fun uploadImage(qrCode: String, image: String): Either<Failure, Boolean>
    fun uploadImageFlow(qrCode: String, image: String): Flow<Either<Failure, Boolean>>
    fun sendImageFlow(qrCode: String, uri: Uri): Flow<Either<Failure, Boolean>>
    fun fetchPhoto(qrCode: String):Either<Failure, List<TMCSliderItem>>
}