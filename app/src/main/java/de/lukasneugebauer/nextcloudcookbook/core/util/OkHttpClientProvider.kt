package de.lukasneugebauer.nextcloudcookbook.core.util

import android.annotation.SuppressLint
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Singleton
class OkHttpClientProvider
    @Inject
    constructor(
        private val baseClient: OkHttpClient,
        private val preferencesManager: PreferencesManager,
        private val scope: CoroutineScope,
    ) {
        private val _clientFlow = MutableStateFlow(baseClient)
        val clientFlow: StateFlow<OkHttpClient> = _clientFlow

        init {
            // Watch for SSL preference changes and update the client
            scope.launch {
                preferencesManager.preferencesFlow
                    .map { it.allowSelfSignedCertificates }
                    .distinctUntilChanged()
                    .collect { allowSelfSignedCerts ->
                        val newClient =
                            if (allowSelfSignedCerts) {
                                val builder = baseClient.newBuilder()
                                configureTrustAllCertificates(builder)
                                builder.build()
                            } else {
                                baseClient
                            }
                        _clientFlow.value = newClient
                    }
            }
        }

        fun getCurrentClient(): OkHttpClient = _clientFlow.value

        @SuppressLint("CustomX509TrustManager", "TrustAllX509TrustManager")
        private fun configureTrustAllCertificates(builder: OkHttpClient.Builder) {
            try {
                val trustAllCerts =
                    arrayOf<TrustManager>(
                        object : X509TrustManager {
                            override fun checkClientTrusted(
                                chain: Array<X509Certificate>,
                                authType: String,
                            ) {}

                            override fun checkServerTrusted(
                                chain: Array<X509Certificate>,
                                authType: String,
                            ) {}

                            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                        },
                    )

                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                builder
                    .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                // Do nothing if SSL configuration fails, keep default settings
            }
        }
    }
