package de.lukasneugebauer.nextcloudcookbook.auth.data.remote

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.auth.data.remote.response.LoginEndpointResponse
import de.lukasneugebauer.nextcloudcookbook.auth.data.remote.response.LoginResponse
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AuthApi {
    @POST
    suspend fun getLoginEndpoint(
        @Url url: String,
    ): NetworkResponse<LoginEndpointResponse, Any>

    @POST
    suspend fun tryLogin(
        @Url url: String,
        @Query("token") token: String,
    ): NetworkResponse<LoginResponse, Any>
}
