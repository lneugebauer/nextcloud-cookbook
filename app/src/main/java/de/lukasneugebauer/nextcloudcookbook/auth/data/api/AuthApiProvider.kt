package de.lukasneugebauer.nextcloudcookbook.auth.data.api

import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import de.lukasneugebauer.nextcloudcookbook.core.domain.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.util.OkHttpClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
        private val clientProvider: OkHttpClientProvider,
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
                clientProvider.clientFlow.collectLatest { client ->
                    initRetrofitApi(client)
                }
            }
        }

        override fun resetApi() {
            _apiFlow.value = null
        }

        private fun initRetrofitApi(client: OkHttpClient) {
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
