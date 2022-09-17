package de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository

import com.dropbox.android.external.store4.StoreResponse
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    suspend fun getRecipePreviews(): Flow<StoreResponse<List<RecipePreviewDto>>>

    suspend fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreResponse<List<RecipePreviewDto>>>

    suspend fun getRecipeFlow(id: Int): Flow<StoreResponse<RecipeDto>>

    suspend fun getRecipe(id: Int): RecipeDto

    suspend fun createRecipe(recipe: RecipeDto): Resource<Int>

    suspend fun updateRecipe(recipe: RecipeDto): SimpleResource

    suspend fun deleteRecipe(id: Int, categoryName: String): SimpleResource
}
