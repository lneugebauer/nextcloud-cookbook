package de.lukasneugebauer.nextcloudcookbook.api

import de.lukasneugebauer.nextcloudcookbook.data.CategoryNw
import de.lukasneugebauer.nextcloudcookbook.data.RecipeNw
import de.lukasneugebauer.nextcloudcookbook.data.RecipePreviewNw
import retrofit2.http.GET
import retrofit2.http.Path

interface NextcloudApi {

    @GET("/apps/cookbook/categories")
    suspend fun getCategories(): List<CategoryNw>

    @GET("/apps/cookbook/api/recipes")
    suspend fun getRecipes(): List<RecipePreviewNw>

    @GET("/apps/cookbook/api/category/{category}")
    suspend fun getRecipesByCategory(@Path("category") category: String): List<RecipePreviewNw>

    @GET("/apps/cookbook/api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeNw

    @GET("/apps/cookbook/api/search/{query}")
    suspend fun search(@Path("query") query: String): List<RecipePreviewNw>
}