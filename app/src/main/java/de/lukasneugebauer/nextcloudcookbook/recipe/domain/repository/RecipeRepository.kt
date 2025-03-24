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

    fun getRecipeFlow(id: Int): Flow<StoreReadResponse<RecipeDto>>

    suspend fun getRecipe(id: Int): RecipeDto

    suspend fun createRecipe(recipe: RecipeDto): Resource<Int>

    suspend fun updateRecipe(recipe: RecipeDto): SimpleResource

    suspend fun deleteRecipe(
        id: Int,
        categoryName: String,
    ): SimpleResource

    suspend fun importRecipe(url: ImportUrlDto): Resource<RecipeDto>
}
