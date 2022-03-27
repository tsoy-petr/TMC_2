package com.hootor.tmc_2.domain.media

import android.graphics.Bitmap
import android.net.Uri
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either

interface MediaRepository {
    fun getPickedImage(uri: Uri?): Either<Failure, Bitmap>
}