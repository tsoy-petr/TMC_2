package com.hootor.tmc_2.data.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.hootor.tmc_2.di.ApplicationScope
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.media.MediaRepository
import java.io.File
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(val context: Context): MediaRepository {

    override fun getPickedImage(uri: Uri?): Either<Failure, Bitmap> {
        if (uri == null) return Either.Left(Failure.FilePickError)

        val filePath = MediaHelper.getPath(context, uri)
        val image = MediaHelper.saveBitmapToFile(File(filePath))

        return if (image == null) {
            Either.Left(Failure.FilePickError)
        } else {
            Either.Right(image)
        }
    }

}