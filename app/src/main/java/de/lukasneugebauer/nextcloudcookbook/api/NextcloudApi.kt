package de.lukasneugebauer.nextcloudcookbook.api

import de.lukasneugebauer.nextcloudcookbook.data.RecipeNw
import de.lukasneugebauer.nextcloudcookbook.data.RecipePreviewNw
import retrofit2.http.GET
import retrofit2.http.Path

interface NextcloudApi {

    @GET("/apps/cookbook/api/recipes")
    suspend fun getRecipes(): List<RecipePreviewNw>

    @GET("/apps/cookbook/api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeNw
}