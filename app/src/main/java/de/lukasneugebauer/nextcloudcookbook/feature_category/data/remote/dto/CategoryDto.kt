package de.lukasneugebauer.nextcloudcookbook.feature_category.data.remote.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.feature_category.domain.model.Category

data class CategoryDto(
    val name: String,
    @SerializedName("recipe_count") val recipeCount: Int,
) {
    fun toCategory() = Category(
        name = name,
        recipeCount = recipeCount,
    )
}