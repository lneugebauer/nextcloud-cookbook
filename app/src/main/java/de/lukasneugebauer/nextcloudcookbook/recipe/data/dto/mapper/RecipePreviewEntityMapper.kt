package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.mapper

import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreviewEntity

fun RecipePreviewEntity.toDto(): RecipePreviewDto =
    RecipePreviewDto(
        recipeId = null,         // deprecated, no lo persistimos
        id = id,
        name = name,
        keywords = keywords,
        category = category,
        dateCreated = dateCreated,
        dateModified = dateModified,
        imageUrl = imageUrl,
        imagePlaceholderUrl = imagePlaceholderUrl,
    )

fun RecipePreviewDto.toEntity(): RecipePreviewEntity =
    RecipePreviewEntity(
        id = if (!id.isNullOrBlank()) id else recipeId
            ?: throw IllegalStateException("Both 'id' and 'recipe_id' are null or blank"),
        name = name,
        keywords = keywords,
        category = category,
        dateCreated = dateCreated,
        dateModified = dateModified,
        imageUrl = imageUrl,
        imagePlaceholderUrl = imagePlaceholderUrl,
    )