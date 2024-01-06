package de.lukasneugebauer.nextcloudcookbook.core.domain.repository

import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Capabilities
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.CookbookVersion
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.NcAccount
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.UserMetadata
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    suspend fun getCapabilities(): Resource<Capabilities>

    suspend fun getUserMetadata(): Resource<UserMetadata>

    fun getAccount(): Flow<Resource<NcAccount>>

    suspend fun getCookbookVersion(): Resource<CookbookVersion>
}
