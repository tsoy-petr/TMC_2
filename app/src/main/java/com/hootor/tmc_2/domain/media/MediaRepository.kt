package com.hootor.tmc_2.domain.media

import android.graphics.Bitmap
import android.net.Uri
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getPickedImage(uri: Uri?): Either<Failure, Bitmap>
    fun uriToBase64(uri: Uri?): Flow<Either<Failure, String>>
}