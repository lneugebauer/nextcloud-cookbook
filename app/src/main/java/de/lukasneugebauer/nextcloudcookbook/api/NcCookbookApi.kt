package de.lukasneugebauer.nextcloudcookbook.api

import de.lukasneugebauer.nextcloudcookbook.data.model.CategoryNw
import de.lukasneugebauer.nextcloudcookbook.data.model.RecipeNw
import de.lukasneugebauer.nextcloudcookbook.data.model.RecipePreviewNw
import retrofit2.http.GET
import retrofit2.http.Path

interface NcCookbookApi {

    @GET("categories")
    suspend fun getCategories(): List<CategoryNw>

    @GET("api/recipes")
    suspend fun getRecipes(): List<RecipePreviewNw>

    @GET("api/category/{categoryName}")
    suspend fun getRecipesByCategory(@Path("categoryName") categoryName: String): List<RecipePreviewNw>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeNw

    @GET("api/search/{query}")
    suspend fun search(@Path("query") query: String): List<RecipePreviewNw>
}