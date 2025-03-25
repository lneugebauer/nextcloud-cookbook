package de.lukasneugebauer.nextcloudcookbook.core.data.api

import com.haroldadmin.cnradapter.NetworkResponse
import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.core.data.dto.CookbookVersionDto
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.CapabilitiesResponse
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.ErrorResponse
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.UserMetadataResponse
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.API_ENDPOINT
import de.lukasneugebauer.nextcloudcookbook.core.util.Constants.FULL_PATH
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.ImportUrlDto
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
        "Content-Type: application/json;charset=utf-8",
    )
    @GET("ocs/v2.php/cloud/capabilities?format=json")
    suspend fun getCapabilities(): NetworkResponse<CapabilitiesResponse, ErrorResponse>

    @Headers(
        "Accept: application/json",
        "OCS-APIRequest: true",
        "Content-Type: application/json;charset=utf-8",
    )
    @GET("ocs/v2.php/cloud/users/{username}?format=json")
    suspend fun getUserMetadata(
        @Path("username") username: String,
    ): NetworkResponse<UserMetadataResponse, ErrorResponse>

    @GET("$FULL_PATH/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("$FULL_PATH/category/{categoryName}")
    suspend fun getRecipesByCategory(
        @Path("categoryName") categoryName: String,
    ): List<RecipePreviewDto>

    @GET("$FULL_PATH/recipes")
    suspend fun getRecipes(): List<RecipePreviewDto>

    @GET("$FULL_PATH/recipes/{id}")
    suspend fun getRecipe(
        @Path("id") id: String,
    ): RecipeDto

    @POST("$FULL_PATH/recipes")
    suspend fun createRecipe(
        @Body recipe: RecipeDto,
    ): String

    @PUT("$FULL_PATH/recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: String,
        @Body recipe: RecipeDto,
    ): String

    @DELETE("$FULL_PATH/recipes/{id}")
    suspend fun deleteRecipe(
        @Path("id") id: String,
    ): NetworkResponse<String, ErrorResponse>

    @POST("$FULL_PATH/import")
    suspend fun importRecipe(
        @Body url: ImportUrlDto,
    ): NetworkResponse<RecipeDto, ErrorResponse>

    @GET("$API_ENDPOINT/version")
    suspend fun getCookbookVersion(): NetworkResponse<CookbookVersionDto, ErrorResponse>
}
