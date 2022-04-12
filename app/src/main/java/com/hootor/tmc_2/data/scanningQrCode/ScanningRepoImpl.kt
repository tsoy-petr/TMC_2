package com.hootor.tmc_2.data.scanningQrCode

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.hootor.tmc_2.data.media.MediaHelper
import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.scanning.ScanningTMCRepository
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.domain.tmc.TMCSliderItem
import com.hootor.tmc_2.services.TMCService
import com.hootor.tmc_2.services.core.Request
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject


class ScanningRepoImpl @Inject constructor(
    private val request: Request,
    private val tmcService: TMCService,
    private val application: Application
) :
    ScanningTMCRepository {
    override suspend fun fetchTMCFields(qrCode: String): Either<Failure, TMC> = request.make(
        tmcService.getTMCBtQrCode(createGetTMCByQrCodeResponseMap(qrCode))
    ) {
        it.tmc
    }

    override suspend fun uploadImage(qrCode: String, image: String): Either<Failure, Boolean> {
        return request.make(tmcService.uploadTMCImage(HashMap<String, String>().apply {
            put("qrCode", qrCode)
            put("image", image)
        })) {
            it.result ?: false
        }
    }

    override fun uploadImageFlow(qrCode: String, image: String): Flow<Either<Failure, Boolean>> =
        flow {
            emit(request.make(tmcService.uploadTMCImage(HashMap<String, String>().apply {
                put("qrCode", qrCode)
                put("image", image)
            })) {
                it.result ?: false
            })
        }

    override fun sendImageFlow(
        qrCode: String,
        uri: Uri
    ): Flow<Either<Failure, Boolean>> =
        flow {

//            val file = File(MediaHelper.getPath(application, uri))
            val file = uri.toFile()
//            if(!File(uri.path).delete()) {
//                Log.d("happy", "!file.delete() - sendImageFlow")
//            }

            val requestFile =
                file.asRequestBody(application.contentResolver.getType(uri)?.toMediaTypeOrNull())
            val body =
                MultipartBody.Part.createFormData("picture", file.name, requestFile);

            emit(request.make(tmcService.sendImageFile(body)) {
                it.result ?: false
            })

            if(!file.delete()) {
                Log.d("happy", "!file.delete() - sendImageFlow")
            }
        }

    override fun fetchPhoto(qrCode: String): Either<Failure, List<TMCSliderItem>> {
        return request.make(tmcService.getTMCImage(HashMap<String, String>().apply {
            put("qrCode", qrCode)
        })) {
            it.images ?: emptyList()
        }
    }

    private fun createGetTMCByQrCodeResponseMap(qrCode: String) = HashMap<String, String>().apply {
        put("qrCode", qrCode)
    }

}