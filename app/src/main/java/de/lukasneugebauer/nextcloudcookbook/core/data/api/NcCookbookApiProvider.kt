package de.lukasneugebauer.nextcloudcookbook.core.data.api

import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.BasicAuthInterceptor
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.ErrorResponseDeserializer
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.ErrorResponse
import de.lukasneugebauer.nextcloudcookbook.core.domain.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.util.addSuffix
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.NutritionDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.remote.deserializer.NutritionDeserializer
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
class NcCookbookApiProvider
    @Inject
    constructor(
        private val client: OkHttpClient,
        private val preferencesManager: PreferencesManager,
        private val scope: CoroutineScope,
    ) : ApiProvider<NcCookbookApi?> {
        private val gson =
            GsonBuilder()
                .registerTypeAdapter(ErrorResponse::class.java, ErrorResponseDeserializer())
                .registerTypeAdapter(NutritionDto::class.java, NutritionDeserializer())
                .create()

        private val _apiFlow = MutableStateFlow<NcCookbookApi?>(null)
        override val apiFlow: StateFlow<NcCookbookApi?> = _apiFlow

        init {
            initApi()
        }

        override fun initApi() {
            scope.launch {
                preferencesManager.preferencesFlow.map {
                    Pair(
                        it.allowSelfSignedCertificates,
                        it.ncAccount,
                    )
                }.distinctUntilChanged().collectLatest {
                        (allowSelfSignedCertificates, ncAccount) ->
                    if (ncAccount.username.isNotBlank() &&
                        ncAccount.token.isNotBlank() &&
                        ncAccount.url.isNotBlank()
                    ) {
                        initRetrofitApi(ncAccount = ncAccount, allowSelfSignedCertificates = allowSelfSignedCertificates)
                    }
                }
            }
        }

        override fun resetApi() {
            _apiFlow.value = null
        }

        private fun initRetrofitApi(
            ncAccount: NcAccount,
            allowSelfSignedCertificates: Boolean = false,
        ) {
            val authInterceptor = BasicAuthInterceptor(ncAccount.username, ncAccount.token)

            val builder = client.newBuilder()

            if (allowSelfSignedCertificates) {
                trustAllCertificates(builder)
            }

            val client = builder.addInterceptor(authInterceptor).build()

            val ncCookbookApi =
                Retrofit.Builder()
                    .baseUrl(ncAccount.url.addSuffix("/"))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(NetworkResponseAdapterFactory())
                    .build()
                    .create(NcCookbookApi::class.java)

            _apiFlow.value = ncCookbookApi
        }

        override fun getApi(): NcCookbookApi? = _apiFlow.value
    }
