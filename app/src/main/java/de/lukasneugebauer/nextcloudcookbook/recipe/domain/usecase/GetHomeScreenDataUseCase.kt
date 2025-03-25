package de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase

import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.RecipeOfTheDay
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.DEFAULT_RECIPE_OF_THE_DAY_ID
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipeStore
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mobilenativefoundation.store.store5.impl.extensions.get
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHomeScreenDataUseCase
    @Inject
    constructor(
        private val categoriesStore: CategoriesStore,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val preferencesManager: PreferencesManager,
        private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
        private val recipePreviewsStore: RecipePreviewsStore,
        private val recipeStore: RecipeStore,
    ) {
        suspend operator fun invoke(): List<HomeScreenDataResult> {
            val currentDate =
                LocalDateTime.now()
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
            val homeScreenData = mutableListOf<HomeScreenDataResult>()
            var recipeOfTheDay = preferencesManager.preferencesFlow.map { it.recipeOfTheDay }.first()

            if (recipeOfTheDay.id == DEFAULT_RECIPE_OF_THE_DAY_ID || recipeOfTheDay.updatedAt.isBefore(currentDate)) {
                try {
                    val newRecipeOfTheDayId =
                        recipePreviewsStore.get(Unit).randomOrNull()?.toRecipePreview()?.id
                            ?: DEFAULT_RECIPE_OF_THE_DAY_ID

                    recipeOfTheDay =
                        RecipeOfTheDay(
                            id = newRecipeOfTheDayId,
                            updatedAt = LocalDateTime.now(),
                        )
                    preferencesManager.updateRecipeOfTheDay(recipeOfTheDay)
                } catch (e: Exception) {
                    Timber.e(e.stackTraceToString())
                }
            }

            // FIXME: 25.12.21 Get different recipe of the day if api returns 404 error.
            //  E.g. after deleting recipe of the day.
            withContext(ioDispatcher) {
                try {
                    val result =
                        HomeScreenDataResult.Single(
                            R.string.home_recommendation,
                            recipeStore.get(recipeOfTheDay.id).toRecipe(),
                        )
                    homeScreenData.add(result)
                } catch (e: Exception) {
                    Timber.e(e.stackTraceToString())
                }
            }

            withContext(ioDispatcher) {
                try {
                    categoriesStore.get(Unit)
                        .sortedByDescending { it.recipeCount }
                        .take(RecipeConstants.HOME_SCREEN_CATEGORIES)
                        .forEach { categoryDto ->
                            val recipePreviews =
                                recipePreviewsByCategoryStore
                                    .get(categoryDto.name)
                                    .map { it.toRecipePreview() }
                            if (recipePreviews.isNotEmpty()) {
                                val result =
                                    HomeScreenDataResult.Row(
                                        categoryDto.name,
                                        recipePreviews,
                                    )
                                homeScreenData.add(result)
                            }
                        }
                } catch (e: Exception) {
                    Timber.e(e.stackTraceToString())
                }
            }

            return homeScreenData.toList()
        }
    }
