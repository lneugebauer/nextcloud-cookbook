package de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository

import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse

interface RecipeRepository {
    fun getRecipePreviewsFlow(): Flow<StoreReadResponse<List<RecipePreviewDto>>>

    fun getRecipePreviewsByCategory(categoryName: String): Flow<StoreReadResponse<List<RecipePreviewDto>>>

    fun getRecipeFlow(id: String): Flow<StoreReadResponse<RecipeDto>>

    suspend fun getRecipe(id: String): RecipeDto

    suspend fun createRecipe(recipe: RecipeDto): Resource<String>

    suspend fun updateRecipe(recipe: RecipeDto): SimpleResource

    suspend fun deleteRecipe(
        id: String,
        categoryName: String,
    ): SimpleResource

    suspend fun importRecipe(url: ImportUrlDto): Resource<RecipeDto>
}
