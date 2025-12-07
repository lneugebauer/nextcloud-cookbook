package de.lukasneugebauer.nextcloudcookbook.core.domain

import android.annotation.SuppressLint
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiProvider<T> {
    val apiFlow: StateFlow<T>

    fun initApi()

    fun resetApi()

    fun getApi(): T

    fun trustAllCertificates(builder: OkHttpClient.Builder) {
        try {
            @SuppressLint("CustomX509TrustManager")
            val trustAllCerts =
                arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String,
                        ) {}

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String,
                        ) {}

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    },
                )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            Timber.e(e, "Error configuring SSL to trust all certificates")
        }
    }
}
