package de.lukasneugebauer.nextcloudcookbook.di

import com.google.gson.GsonBuilder
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.AuthApi
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.remote.UserAgentInterceptor
import de.lukasneugebauer.nextcloudcookbook.feature_auth.data.repository.AuthRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.feature_auth.domain.repository.AuthRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApi(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        userAgentInterceptor: UserAgentInterceptor
    ): AuthApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(userAgentInterceptor)
            .build()

        val gson = GsonBuilder().create()

        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(NetworkResponseAdapterFactory())
            .build()
            .create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authApi: AuthApi): AuthRepository = AuthRepositoryImpl(authApi)
}
