package de.lukasneugebauer.nextcloudcookbook.auth.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.auth.data.remote.AuthApi
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val api: AuthApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository, BaseRepository() {
    override suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult> {
        return withContext(ioDispatcher) {
            val url =
                buildString {
                    append(baseUrl.removeSuffix("/"))
                    if (!baseUrl.contains("index.php")) {
                        append("/index.php")
                    }
                    append("/login/v2")
                }

            when (val response = api.getLoginEndpoint(url)) {
                is NetworkResponse.Success -> {
                    val result = response.body.toLoginEndpointResult()
                    Resource.Success(result)
                }

                is NetworkResponse.Error -> {
                    handleResponseError(response.error)
                }
            }
        }
    }

    override suspend fun tryLogin(
        url: String,
        token: String,
    ): Resource<LoginResult> {
        return withContext(ioDispatcher) {
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
