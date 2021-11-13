package de.lukasneugebauer.nextcloudcookbook.core.data.api

import de.lukasneugebauer.nextcloudcookbook.feature_category.data.remote.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.data.remote.dto.RecipePreviewDto
import retrofit2.http.GET
import retrofit2.http.Path

interface NcCookbookApi {

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/recipes")
    suspend fun getRecipes(): List<RecipePreviewDto>

    @GET("api/category/{categoryName}")
    suspend fun getRecipesByCategory(@Path("categoryName") categoryName: String): List<RecipePreviewDto>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeDto

    @GET("api/search/{query}")
    suspend fun search(@Path("query") query: String): List<RecipePreviewDto>
}