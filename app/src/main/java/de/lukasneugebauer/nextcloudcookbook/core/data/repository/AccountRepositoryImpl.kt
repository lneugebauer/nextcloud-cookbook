package de.lukasneugebauer.nextcloudcookbook.core.data.repository

import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Capabilities
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val preferencesManager: PreferencesManager
) : AccountRepository {

    override suspend fun getCapabilities(): Resource<Capabilities> {
        return withContext(Dispatchers.IO) {
            val api = apiProvider.getNcCookbookApi()
                ?: return@withContext Resource.Error(text = "API not initialized")

            try {
                val response = api.getCapabilities()
                Resource.Success(data = response.ocs.data.capabilities.toCapabilities())
            } catch (e: HttpException) {
                Timber.e(e.stackTraceToString())
                val errorMessage = when (e.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    403 -> "Forbidden"
                    404 -> "Not found"
                    500 -> "Internal Server Error"
                    503 -> "Service Unavailable"
                    else -> "Unknown error"
                }
                Resource.Error(text = errorMessage)
            }
        }
    }

    override suspend fun getAccount(): Flow<Resource<NcAccount>> {
        return preferencesManager.preferencesFlow
            .distinctUntilChanged()
            .map {
                val account = it.ncAccount
                if (account.username.isBlank() &&
                    account.token.isBlank() &&
                    account.url.isBlank()
                ) {
                    return@map Resource.Error(data = account, text = "Account data empty.")
                }

                Resource.Success(data = account)
            }
    }
}