package de.lukasneugebauer.nextcloudcookbook.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.data.deserializer.NutritionDeserializer
import de.lukasneugebauer.nextcloudcookbook.data.model.NutritionNw
import de.lukasneugebauer.nextcloudcookbook.data.repository.AccountRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.data.repository.RecipeRepositoryImpl
import de.lukasneugebauer.nextcloudcookbook.domain.repository.AccountRepository
import de.lukasneugebauer.nextcloudcookbook.domain.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // https://medium.com/androiddevelopers/create-an-application-coroutinescope-using-hilt-dd444e721528
    @Singleton
    @Provides
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(NutritionNw::class.java, NutritionDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideApiProvider(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
        gson: Gson,
        preferencesManager: PreferencesManager
    ): ApiProvider = ApiProvider(context, coroutineScope, gson, preferencesManager)

    @Provides
    @Singleton
    fun provideAccountRepository(
        apiProvider: ApiProvider,
        preferencesManager: PreferencesManager
    ): AccountRepository = AccountRepositoryImpl(apiProvider, preferencesManager)

    @Provides
    @Singleton
    fun provideRecipeRepository(apiProvider: ApiProvider): RecipeRepository =
        RecipeRepositoryImpl(apiProvider)
}