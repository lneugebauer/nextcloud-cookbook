package de.lukasneugebauer.nextcloudcookbook.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.SHARED_PREFERENCES_KEY
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.deserializer.NutritionDeserializer
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto.NutritionDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // https://medium.com/androiddevelopers/create-an-application-coroutinescope-using-hilt-dd444e721528
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): PreferencesManager =
        PreferencesManager(context, sharedPreferences)

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(NutritionDto::class.java, NutritionDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideApiProvider(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
        gson: Gson,
        preferencesManager: PreferencesManager
    ): ApiProvider = ApiProvider(context, coroutineScope, gson, preferencesManager)
}