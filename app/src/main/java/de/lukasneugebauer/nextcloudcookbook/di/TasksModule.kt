package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.OkHttpClientProvider
import de.lukasneugebauer.nextcloudcookbook.tasks.data.remote.CalDavXmlParser
import de.lukasneugebauer.nextcloudcookbook.tasks.data.repository.TasksRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TasksModule {
    @Provides
    @Singleton
    fun provideTasksRepository(
        clientProvider: OkHttpClientProvider,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): TasksRepository = TasksRepositoryImpl(clientProvider, preferencesManager, ioDispatcher, CalDavXmlParser())
}
