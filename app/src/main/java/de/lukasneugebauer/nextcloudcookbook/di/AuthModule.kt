package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.auth.data.api.AuthApiProvider
import de.lukasneugebauer.nextcloudcookbook.auth.data.repository.AuthRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.auth.domain.repository.AuthRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        apiProvider: AuthApiProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AuthRepository = AuthRepositoryImpl(apiProvider, ioDispatcher)
}
