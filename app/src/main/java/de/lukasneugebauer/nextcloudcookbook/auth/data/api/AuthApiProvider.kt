package de.lukasneugebauer.nextcloudcookbook.auth.data.api

import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.ALLOW_SELF_SIGNED_CERTIFICATES_DEFAULT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApiProvider
    @Inject
    constructor(
        private val client: OkHttpClient,
        private val preferencesManager: PreferencesManager,
        private val scope: CoroutineScope,
    ) : ApiProvider<AuthApi?> {
        private val gson = GsonBuilder().create()

        private val _apiFlow = MutableStateFlow<AuthApi?>(null)
        override val apiFlow: StateFlow<AuthApi?> = _apiFlow

        init {
            initApi()
        }

        override fun initApi() {
            scope.launch {
                preferencesManager.preferencesFlow
                    .map { it.allowSelfSignedCertificates }
                    .distinctUntilChanged()
                    .collectLatest { allowSelfSignedCertificates ->
                        initRetrofitApi(allowSelfSignedCertificates = allowSelfSignedCertificates)
                    }
            }
        }

        override fun resetApi() {
            _apiFlow.value = null
        }

        private fun initRetrofitApi(allowSelfSignedCertificates: Boolean = ALLOW_SELF_SIGNED_CERTIFICATES_DEFAULT) {
            val builder = client.newBuilder()

            if (allowSelfSignedCertificates) {
                trustAllCertificates(builder)
            }

            val client = builder.build()

            val authApi =
                Retrofit
                    .Builder()
                    .baseUrl("http://localhost/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .build()
                    .create(AuthApi::class.java)

            _apiFlow.value = authApi
        }

        override fun getApi() = _apiFlow.value
    }
