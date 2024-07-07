package de.lukasneugebauer.nextcloudcookbook.auth.domain.repository

import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource

interface AuthRepository {
    suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult>

    suspend fun tryLogin(
        url: String,
        token: String,
    ): Resource<LoginResult>
}
