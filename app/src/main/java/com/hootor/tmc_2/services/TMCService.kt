package com.hootor.tmc_2.services

import com.hootor.tmc_2.domain.exception.Failure
import com.hootor.tmc_2.domain.exception.Failure.ServerError
import com.hootor.tmc_2.domain.functional.Either
import com.hootor.tmc_2.domain.functional.Either.*
import com.hootor.tmc_2.domain.tmc.TMC
import com.hootor.tmc_2.services.tmc.GetTMCByQrCodeRequest
import com.hootor.tmc_2.services.tmc.GetTMCByQrCodeResponse
import com.hootor.tmc_2.services.tmc.GetTMCTreeQrCodeResponse
import retrofit2.Call
import retrofit2.http.*

interface TMCService {

    @GET("/server/hs/tmc/v1/tmcByQrCode/{qrCode}")
    fun fetchTMC(@Path("qrCode", encoded = false) qrCode: String): Call<TMC>

    @FormUrlEncoded
    @POST(GET_TMC_BY_QRCODE)
    fun getTMCTreeByQrCode(@FieldMap params: Map<String, String>): Call<GetTMCTreeQrCodeResponse>

    @FormUrlEncoded
    @POST(GET_TMC_TREE_BY_QRCODE)
    fun getTMCBtQrCode(@FieldMap params: Map<String, String>): Call<GetTMCByQrCodeResponse>

    companion object {
        private const val GET_TMC_BY_QRCODE = "/server/hs/tmc/v2/tmcByQrCode"
        private const val GET_TMC_TREE_BY_QRCODE = "/server/hs/tmc/v1/consist"
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