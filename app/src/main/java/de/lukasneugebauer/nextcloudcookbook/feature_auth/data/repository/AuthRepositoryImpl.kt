package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.AuthApi
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository, BaseRepository() {

    override suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult> {
        return withContext(Dispatchers.IO) {
            return@withContext when (val response = api.getLoginEndpoint("$baseUrl/login/v2")) {
                is NetworkResponse.Success -> {
                    val result = response.body.toLoginEndpointResult()
                    Resource.Success(result)
                }
                is NetworkResponse.Error -> handleResponseError(response.error)
            }
        }
    }

    override suspend fun tryLogin(url: String, token: String): Resource<LoginResult> {
        return withContext(Dispatchers.IO) {
            return@withContext when (val response = api.tryLogin(url = url, token = token)) {
                is NetworkResponse.Success -> {
                    val result = response.body.toLoginResult()
                    Resource.Success(data = result)
                }
                is NetworkResponse.Error -> handleResponseError(response.error)
            }
        }
    }
}
