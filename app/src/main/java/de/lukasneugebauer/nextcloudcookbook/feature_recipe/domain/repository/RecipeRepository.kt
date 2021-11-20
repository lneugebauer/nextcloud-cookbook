package de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.repository

import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto.RecipePreviewDto
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    suspend fun getRecipePreviews(): Flow<StoreResponse<List<RecipePreviewDto>>>

    suspend fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreResponse<List<RecipePreviewDto>>>

    suspend fun getRecipe(id: Int): Flow<StoreResponse<RecipeDto>>
}