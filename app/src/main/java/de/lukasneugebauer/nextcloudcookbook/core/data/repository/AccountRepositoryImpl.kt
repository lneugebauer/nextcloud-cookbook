package de.lukasneugebauer.nextcloudcookbook.core.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Capabilities
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val preferencesManager: PreferencesManager,
) : AccountRepository, BaseRepository() {

    override suspend fun getCapabilities(): Resource<Capabilities> {
        return withContext(ioDispatcher) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(message = UiText.StringResource(R.string.error_api_not_initialized))

            when (val response = api.getCapabilities()) {
                is NetworkResponse.Success -> {
                    val result = response.body.ocs.data.capabilities.toCapabilities()
                    Resource.Success(data = result)
                }
                is NetworkResponse.Error -> handleResponseError(response.error?.cause)
            }
        }
    }

    override fun getAccount(): Flow<Resource<NcAccount>> {
        return preferencesManager.preferencesFlow
            .distinctUntilChanged()
            .map {
                val account = it.ncAccount
                if (account.username.isBlank() &&
                    account.token.isBlank() &&
                    account.url.isBlank()
                ) {
                    return@map Resource.Error(
                        data = account,
                        message = UiText.StringResource(R.string.error_no_account_data),
                    )
                }

                Resource.Success(data = account)
            }
    }
}
