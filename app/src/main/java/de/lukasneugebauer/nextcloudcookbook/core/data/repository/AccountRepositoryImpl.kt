package de.lukasneugebauer.nextcloudcookbook.core.data.repository

import de.lukasneugebauer.nextcloudcookbook.core.data.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.di.ApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val preferencesManager: PreferencesManager
) : AccountRepository {

    override suspend fun getAccount(): Flow<Resource<NcAccount>> {
        return preferencesManager.preferencesFlow
            .distinctUntilChanged()
            .map {
                val account = it.ncAccount
                if (account.username.isBlank() &&
                    account.token.isBlank() &&
                    account.url.isBlank()
                ) {
                    Resource.Error(data = account, text = "Account data empty.")
                } else {
                    Resource.Success(data = account)
                }
            }
    }
}