package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.repository

import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.AuthApi
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginEndpointResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.model.LoginResult
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.repository.AuthRepository
import retrofit2.HttpException
import timber.log.Timber
import java.net.UnknownHostException

class AuthRepositoryImpl(private val authApi: AuthApi) : AuthRepository {

    override suspend fun getLoginEndpoint(baseUrl: String): Resource<LoginEndpointResult> {
        return try {
            val response = authApi.getLoginEndpoint("$baseUrl/login/v2")
            val result = response.toLoginEndpointResult()
            Resource.Success(data = result)
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
        } catch (e: UnknownHostException) {
            Timber.e(e.stackTraceToString())
            Resource.Error(text = e.localizedMessage ?: "Unknown host")
        } catch (e: Error) {
            Timber.e(e.stackTraceToString())
            Resource.Error(text = e.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun tryLogin(url: String, token: String): Resource<LoginResult> {
        return try {
            val response = authApi.tryLogin(url = url, token = token)
            val result = response.toLoginResult()
            Resource.Success(data = result)
        } catch (e: Exception) {
            Resource.Error(text = e.localizedMessage ?: "Unknown error")
        }
    }
}
