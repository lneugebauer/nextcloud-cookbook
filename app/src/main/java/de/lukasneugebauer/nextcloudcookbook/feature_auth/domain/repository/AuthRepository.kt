package de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.repository

import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginResult

interface AuthRepository {

    suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult>

    suspend fun tryLogin(url: String, token: String): Resource<LoginResult>
}