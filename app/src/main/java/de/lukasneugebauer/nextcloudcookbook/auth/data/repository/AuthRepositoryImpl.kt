package de.lukasneugebauer.nextcloudcookbook.auth.data.repository

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.auth.data.api.AuthApiProvider
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.BaseRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val apiProvider: AuthApiProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BaseRepository(),
    AuthRepository {
    override suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult> =
        withContext(ioDispatcher) {
            val api = apiProvider.apiFlow.filterNotNull().first()

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

    override suspend fun tryLogin(
        url: String,
        token: String,
    ): Resource<LoginResult> =
        withContext(ioDispatcher) {
            val api = apiProvider.apiFlow.filterNotNull().first()

            when (val response = api.tryLogin(url = url, token = token)) {
                is NetworkResponse.Success -> {
                    val result = response.body.toLoginResult()
                    Resource.Success(data = result)
                }

                is NetworkResponse.Error -> handleResponseError(response.error)
            }
        }
}
