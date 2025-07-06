package de.lukasneugebauer.nextcloudcookbook.recipe.domain.usecase

import android.content.Context
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.RANDOM_RECIPES_ON_HOME
import de.lukasneugebauer.nextcloudcookbook.core.util.IoDispatcher
import de.lukasneugebauer.nextcloudcookbook.di.CategoriesStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsByCategoryStore
import de.lukasneugebauer.nextcloudcookbook.di.RecipePreviewsStore
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.HomeScreenDataResult
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.recipe.util.RecipeConstants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.mobilenativefoundation.store.store5.impl.extensions.get
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHomeScreenDataUseCase
    @Inject
    constructor(
        private val categoriesStore: CategoriesStore,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val recipePreviewsByCategoryStore: RecipePreviewsByCategoryStore,
        private val recipePreviewsStore: RecipePreviewsStore,
    ) {
        suspend operator fun invoke(): List<HomeScreenDataResult> {
            val homeScreenData = mutableListOf<HomeScreenDataResult>()
            var randomRecipes: List<RecipePreview> = emptyList()

            try {
                randomRecipes = recipePreviewsStore
                    .get(Unit)
                    .shuffled()
                    .take(RANDOM_RECIPES_ON_HOME)
                    .map { it.toRecipePreview() }
            } catch (e: Exception) {
                Timber.e(e.stackTraceToString())
            }

            if (randomRecipes.isNotEmpty()) {
                withContext(ioDispatcher) {
                    try {
                        val result =
                            HomeScreenDataResult.Row(
                                "context.getString(R.string.home_recommendations)", // TODO
                                randomRecipes
                            )
                        homeScreenData.add(result)
                    } catch (e: Exception) {
                        Timber.e(e.stackTraceToString())
                    }
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
