package de.lukasneugebauer.nextcloudcookbook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.core.util.DefaultDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.MainDispatcher
import de.lukasneugebauer.nextcloudcookbook.core.util.MainImmediateDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesDispatchersModule {

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @MainImmediateDispatcher
    @Provides
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
