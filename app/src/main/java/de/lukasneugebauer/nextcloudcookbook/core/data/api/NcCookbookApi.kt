package de.lukasneugebauer.nextcloudcookbook.core.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.CapabilitiesResponse
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.ErrorResponse
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipeDto
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NcCookbookApi {

    @Headers(
        "Accept: application/json",
        "OCS-APIRequest: true",
        "Content-Type: application/json;charset=utf-8"
    )
    @GET("/ocs/v2.php/cloud/capabilities?format=json")
    suspend fun getCapabilities(): NetworkResponse<CapabilitiesResponse, ErrorResponse>

    @GET("${Constants.API_ENDPOINT}/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("${Constants.API_ENDPOINT}/category/{categoryName}")
    suspend fun getRecipesByCategory(@Path("categoryName") categoryName: String): List<RecipePreviewDto>

    @GET("${Constants.API_ENDPOINT}/recipes")
    suspend fun getRecipes(): List<RecipePreviewDto>

    @GET("${Constants.API_ENDPOINT}/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: Int): RecipeDto

    // TODO: Check if recipeDto model as body does work even in minified production build
    @POST("${Constants.API_ENDPOINT}/recipes")
    suspend fun createRecipe(@Body recipe: RecipeDto): Int

    // TODO: Check if recipeDto model as body does work even in minified production build
    @PUT("${Constants.API_ENDPOINT}/recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: Int, @Body recipe: RecipeDto): Int

    @DELETE("${Constants.API_ENDPOINT}/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): NetworkResponse<String, ErrorResponse>
}
