package com.hootor.tmc_2.services

import com.hootor.tmc_2.data.Prefs
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val prefs: Prefs) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        prefs.getSettings().apply {
            val credentials: String = Credentials.basic(userName ?: "", userPass ?: "")
            requestBuilder.addHeader("Authorization", credentials)
        }

        return chain.proceed(requestBuilder.build())
    }
}