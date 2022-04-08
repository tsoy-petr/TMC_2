package com.hootor.tmc_2.services

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.exception.Failure.ServerError
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.functional.Either.*
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.services.tmc.GetImagesByQrCode
import com.hootor.tmc_2.services.tmc.GetTMCByQrCodeResponse
import com.hootor.tmc_2.services.tmc.GetTMCTreeQrCodeResponse
import com.hootor.tmc_2.services.tmc.UploadTMCImageResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface TMCService {

    @GET("/server/hs/tmc/v1/tmcByQrCode/{qrCode}")
    fun fetchTMC(@Path("qrCode", encoded = false) qrCode: String): Call<TMC>

    @FormUrlEncoded
    @POST(GET_TMC_TREE_BY_QRCODE)
    fun getTMCTreeByQrCode(@FieldMap params: Map<String, String>): Call<GetTMCTreeQrCodeResponse>

    @FormUrlEncoded
    @POST(GET_TMC_BY_QRCODE)
    fun getTMCBtQrCode(@FieldMap params: Map<String, String>): Call<GetTMCByQrCodeResponse>

    @FormUrlEncoded
    @POST(POST_TMC_UPLOAD_IMAGE)
    fun uploadTMCImage(@FieldMap params: Map<String, String>): Call<UploadTMCImageResponse>

    @POST(POST_TMC_UPLOAD_IMAGE)
    @Multipart
    fun sendImageFile(@Part body: MultipartBody.Part): Call<UploadTMCImageResponse>

    @FormUrlEncoded
    @POST(POST_TMC_IMAGE)
    fun getTMCImage(@FieldMap params: Map<String, String>): Call<GetImagesByQrCode>

    @FormUrlEncoded
    @POST(GET_TMC_BY_QRCODE)
    fun getInventory(): Call<GetTMCByQrCodeResponse>


    companion object {
        private const val GET_TMC_BY_QRCODE = "/server/hs/tmc/v2/tmcByQrCode"
        private const val GET_TMC_TREE_BY_QRCODE = "/server/hs/tmc/v1/consist"
        private const val POST_TMC_UPLOAD_IMAGE = "/server/hs/tmc/v2/tmcMedia"
        private const val POST_TMC_IMAGE = "/server/hs/tmc/v2/tmcImgByQrCode"
        const val GET_TMC_IMG = "/server/hs/tmc/v1/tmcImg"
    }
}

fun <T, R> Call<T>.make(
    transform: (T) -> R,
    default: T
): Either<Failure, R> {
    return try {
        val response = this.execute()
        when (response.isSuccessful) {
            true -> Right(transform((response.body() ?: default)))
            false -> Left(ServerError)
        }
    } catch (exception: Throwable) {
        Left(ServerError)
    }
}