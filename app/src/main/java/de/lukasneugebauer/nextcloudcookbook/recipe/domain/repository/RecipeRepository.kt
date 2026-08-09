package de.lukasneugebauer.nextcloudcookbook.recipe.domain.repository

import de.lukasneugebauer.nextcloudcookbook.core.util.DataResult
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.SimpleResource
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipeImageUpload
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreview
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipePreviewsFlow(): Flow<DataResult<List<RecipePreview>>>

    fun getRecipePreviewsByCategory(categoryName: String): Flow<DataResult<List<RecipePreview>>>

    fun getRecipeFlow(id: String): Flow<DataResult<Recipe>>

    suspend fun getRecipe(id: String): RecipeDto

    suspend fun createRecipe(recipe: RecipeDto): Resource<String>

    suspend fun updateRecipe(recipe: RecipeDto): SimpleResource

    suspend fun deleteRecipe(id: String): SimpleResource

    suspend fun importRecipe(url: ImportUrlDto): Resource<RecipeDto>

    suspend fun uploadRecipeImage(image: RecipeImageUpload): Resource<String>
}
