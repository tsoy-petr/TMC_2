package com.hootor.tmc_2.data.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.media.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Error
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(val context: Context) : MediaRepository {

    override fun getPickedImage(uri: Uri?): Either<Failure, Bitmap> {

        val bitmap = MediaHelper.uriToBitmap(context, uri)

        return if (bitmap == null) {
            Either.Left(Failure.FilePickError)
        } else {
            Either.Right(bitmap)
        }
    }

    override fun uriToBase64(uri: Uri?): Flow<Either<Failure, String>> = flow {

        Log.i("bildovich", "${Thread.currentThread().name} uriToBase64:MediaRepositoryImpl")

        try {
//            val bitmap = MediaHelper.uriToBitmap(context, uri)
            val bitmap = MediaHelper.decodeBitmap(uri!!, context)
            if (bitmap != null) {
                val base64 = MediaHelper.encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100)
                Log.i("bildovich_base64", base64)
                emit(Either.Right(base64))
            } else {
                emit(Either.Left(Failure.UriToBitmapError))
            }

        } catch (e: Error) {
            emit(Either.Left(Failure.FilePickError))
        }
    }

}