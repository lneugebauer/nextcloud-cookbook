package de.lukasneugebauer.nextcloudcookbook.di

import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.BasicAuthInterceptor
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.ErrorResponseDeserializer
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.NetworkInterceptor
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.ErrorResponse
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.util.addSuffix
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.NutritionDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.remote.deserializer.NutritionDeserializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiProvider
    @Inject
    constructor(
        private val scope: CoroutineScope,
        private val httpLoggingInterceptor: HttpLoggingInterceptor,
        private val preferencesManager: PreferencesManager,
    ) {
        private val gson =
            GsonBuilder()
                .registerTypeAdapter(ErrorResponse::class.java, ErrorResponseDeserializer())
                .registerTypeAdapter(NutritionDto::class.java, NutritionDeserializer())
                .create()

        private val _ncCookbookApiFlow = MutableStateFlow<NcCookbookApi?>(null)
        val ncCookbookApiFlow: StateFlow<NcCookbookApi?> = _ncCookbookApiFlow

        init {
            initApi()
        }

        fun initApi() {
            scope.launch {
                val ncAccount =
                    preferencesManager.preferencesFlow
                        .map { it.ncAccount }
                        .first()

                if (ncAccount.username.isNotBlank() &&
                    ncAccount.token.isNotBlank() &&
                    ncAccount.url.isNotBlank()
                ) {
                    initRetrofitApi(ncAccount)
                }
            }
        }

        fun resetApi() {
            _ncCookbookApiFlow.value = null
        }

        private fun initRetrofitApi(ncAccount: NcAccount) {
            val authInterceptor = BasicAuthInterceptor(ncAccount.username, ncAccount.token)

            val client =
                OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .addInterceptor(authInterceptor)
                    .addNetworkInterceptor(NetworkInterceptor())
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

            val retrofit =
                Retrofit.Builder()
                    .baseUrl(ncAccount.url.addSuffix("/"))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .build()

            _ncCookbookApiFlow.value = retrofit.create(NcCookbookApi::class.java)
        }

        fun getNcCookbookApi(): NcCookbookApi? = _ncCookbookApiFlow.value
    }
