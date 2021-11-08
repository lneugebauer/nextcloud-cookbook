package de.lukasneugebauer.nextcloudcookbook.domain.repository

import de.lukasneugebauer.nextcloudcookbook.data.NcAccount
import de.lukasneugebauer.nextcloudcookbook.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    suspend fun getAccount(): Flow<Resource<NcAccount>>
}