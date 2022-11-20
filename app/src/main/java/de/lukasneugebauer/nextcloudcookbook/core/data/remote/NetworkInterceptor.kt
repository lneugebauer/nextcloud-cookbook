package de.lukasneugebauer.nextcloudcookbook.core.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 301) return response.newBuilder().code(308).build()

        if (response.code == 302) return response.newBuilder().code(307).build()

        return response
    }
}
