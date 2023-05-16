package de.lukasneugebauer.nextcloudcookbook.category.data.dto

import com.google.gson.annotations.SerializedName
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category

data class CategoryDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("recipe_count")
    val recipeCount: Int,
) {
    fun toCategory() = Category(
        name = name,
        recipeCount = recipeCount,
    )
}
