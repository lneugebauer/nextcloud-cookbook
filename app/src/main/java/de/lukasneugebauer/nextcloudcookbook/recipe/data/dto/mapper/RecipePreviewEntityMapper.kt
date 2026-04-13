package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.mapper

import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.RecipePreviewDto
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.RecipePreviewEntity

fun RecipePreviewEntity.toDto(): RecipePreviewDto =
    RecipePreviewDto(
        recipeId = null,
        id = id,
        name = name,
        keywords = keywords,
        category = category,
        dateCreated = dateCreated,
        dateModified = dateModified,
        imageUrl = imageUrl,
        imagePlaceholderUrl = imagePlaceholderUrl,
    )

fun RecipePreviewDto.toEntity(categoryOverride: String? = null): RecipePreviewEntity =
    RecipePreviewEntity(
        id = id?.takeIf { it.isNotBlank() }
            ?: recipeId?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Both 'id' and 'recipe_id' are null or blank"),
        name = name,
        keywords = keywords,
        category = categoryOverride ?: category
        ?: throw IllegalStateException("Category is required (either from DTO or override)"),
        dateCreated = dateCreated,
        dateModified = dateModified,
        imageUrl = imageUrl,
        imagePlaceholderUrl = imagePlaceholderUrl,
    )