package de.lukasneugebauer.nextcloudcookbook.di

import com.google.gson.GsonBuilder
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.BasicAuthInterceptor
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.dto.NutritionDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.deserializer.NutritionDeserializer
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiProvider @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val httpLoggingInterceptor: HttpLoggingInterceptor,
    private val preferencesManager: PreferencesManager
) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(NutritionDto::class.java, NutritionDeserializer())
        .create()

    private val _ncCookbookApiFlow = MutableStateFlow<NcCookbookApi?>(null)
    val ncCookbookApiFlow: StateFlow<NcCookbookApi?> = _ncCookbookApiFlow

    init {
        initApi()
    }

    fun initApi() {
        coroutineScope.launch {
            val ncAccount = preferencesManager.preferencesFlow
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

    private fun initRetrofitApi(ncAccount: NcAccount) {
        val authInterceptor = BasicAuthInterceptor(ncAccount.username, ncAccount.token)

        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ncAccount.url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        _ncCookbookApiFlow.value = retrofit.create(NcCookbookApi::class.java)
    }

    fun getNcCookbookApi(): NcCookbookApi? = _ncCookbookApiFlow.value
}
