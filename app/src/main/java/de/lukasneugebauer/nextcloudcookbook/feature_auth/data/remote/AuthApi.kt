package de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote

import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.response.LoginEndpointResponse
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.response.LoginResponse
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AuthApi {

    @POST
    suspend fun getLoginEndpoint(@Url url: String): LoginEndpointResponse

    @POST
    suspend fun tryLogin(@Url url: String, @Query("token") token: String): LoginResponse
}
