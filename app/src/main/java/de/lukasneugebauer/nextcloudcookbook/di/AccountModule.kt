package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.data.repository.AccountRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.domain.repository.AccountRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideAccountRepository(
        apiProvider: ApiProvider,
        preferencesManager: PreferencesManager
    ): AccountRepository = AccountRepositoryImpl(apiProvider, preferencesManager)
}