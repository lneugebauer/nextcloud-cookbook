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
// Get the default keystore
KeyStore ks = KeyStore.getInstance("AndroidCAStore");

// Initialize the key manager factory with the default keystore
private fun configureTrustAllCertificates(builder: OkHttpClient.Builder) {
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(null as KeyStore?)
    val trustManagers = trustManagerFactory.trustManagers
    if (trustManagers.size == 1 && trustManagers[0] is X509TrustManager) {
        val trustManager = trustManagers[0] as X509TrustManager
        builder.sslSocketFactory(TLSv12SocketFactory(), trustManager)
    } else {
        throw IllegalStateException("Unexpected default trust managers: " + Arrays.toString(trustManagers))
    }
}

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }

        synchronized (builder) {
            builder.notify()
            builder.notifyAll()
        }
    } catch (e: Exception) {
        // Do nothing if SSL configuration fails, keep default settings
    }
}
    }
