package com.hootor.tmc_2.services

import com.hootor.tmc_2.data.Prefs
import com.hootor.tmc_2.data.PrefsImpl
import com.hootor.tmc_2.services.ServiceFactory.Companion.BASE_URL
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.URISyntaxException
import javax.inject.Inject


class HostSelectionInterceptor @Inject constructor(private val prefs: Prefs) : Interceptor {

    private fun host(): HttpUrl? {
        val url = prefs.getSettings().serverUrl ?: BASE_URL
        val port = prefs.getSettings().serverPort ?: 80
        return "$url:$port".toHttpUrlOrNull()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val host = host()
        var newRequest: Request? = null
        var request: Request = chain.request()
        if (host != null) {
            val newUrl: Any =
                try {
                    request.url.newBuilder()
                        .scheme(host.scheme)
                        .host(host.toUrl().host)
                        .port(host.toUrl().port)
                        .build()
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            newRequest = request.newBuilder()
                .url(newUrl as HttpUrl)
                .build()
        }
        if (newRequest != null) return chain.proceed(newRequest)
        return chain.proceed(request)
    }
}