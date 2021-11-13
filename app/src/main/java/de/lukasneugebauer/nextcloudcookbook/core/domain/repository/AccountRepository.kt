package de.lukasneugebauer.nextcloudcookbook.core.domain.repository

import de.lukasneugebauer.nextcloudcookbook.core.data.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    suspend fun getAccount(): Flow<Resource<NcAccount>>
}