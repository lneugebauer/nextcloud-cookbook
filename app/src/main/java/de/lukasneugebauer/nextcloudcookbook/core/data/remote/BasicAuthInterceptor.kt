package de.lukasneugebauer.nextcloudcookbook.core.data.remote

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.StandardCharsets.UTF_8

class BasicAuthInterceptor(username: String, password: String) : Interceptor {

    private val credentials: String = Credentials.basic(username, password, UTF_8)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("Authorization", credentials)
            .build()
        return chain.proceed(request)
    }
}
