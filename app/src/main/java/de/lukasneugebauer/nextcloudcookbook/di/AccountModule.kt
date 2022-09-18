package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.repository.AccountRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.core.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideAccountRepository(
        apiProvider: ApiProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        preferencesManager: PreferencesManager
    ): AccountRepository = AccountRepositoryImpl(apiProvider, ioDispatcher, preferencesManager)
}
