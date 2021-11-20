package de.lukasneugebauer.nextcloudcookbook.di

import android.content.Context
import com.google.gson.Gson
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.exceptions.SSOException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.core.data.api.BasicAuthInterceptor
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApi
import de.lukasneugebauer.nextcloudcookbook.core.data.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.NextcloudRetrofitApiBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiProvider(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val gson: Gson,
    private val preferencesManager: PreferencesManager
) {

    private var ncSsoApi: NextcloudAPI? = null
    private var ncCookbookApi: NcCookbookApi? = null

    init {
        initApi(object : NextcloudAPI.ApiConnectedListener {
            override fun onConnected() {}
            override fun onError(ex: Exception?) {}
        })
    }

    fun initApi(apiConnectedListener: NextcloudAPI.ApiConnectedListener) {
        ncSsoApi?.let {
            it.stop()
            ncSsoApi = null
        }

        coroutineScope.launch {
            val ncAccount = preferencesManager.preferencesFlow
                .map { it.ncAccount }
                .first()
            val useSingleSignOn = preferencesManager.preferencesFlow
                .map { it.useSingleSignOn }
                .first()

            if (ncAccount.username.isNotBlank() && ncAccount.token.isNotBlank() && ncAccount.url.isNotBlank()) {
                if (useSingleSignOn) {
                    initSsoApi(apiConnectedListener)
                } else {
                    initRetrofitApi(ncAccount)
                }
            }
        }
    }

    private fun initSsoApi(callback: NextcloudAPI.ApiConnectedListener) {
        try {
            val ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            ncSsoApi = NextcloudAPI(context, ssoAccount, gson, callback)
            ncSsoApi?.let {
                this.ncCookbookApi = NextcloudRetrofitApiBuilder(
                    it,
                    Constants.API_ENDPOINT
                ).create(NcCookbookApi::class.java)
            } ?: run {
                throw NullPointerException("Nextcloud API couldn't be created.")
            }
        } catch (e: SSOException) {
            callback.onError(e)
        }
    }

    private fun initRetrofitApi(ncAccount: NcAccount) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = BasicAuthInterceptor(ncAccount.username, ncAccount.token)

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ncAccount.url + Constants.API_ENDPOINT)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        ncCookbookApi = retrofit.create(NcCookbookApi::class.java)
    }

    fun getNcCookbookApi(): NcCookbookApi? = ncCookbookApi
}