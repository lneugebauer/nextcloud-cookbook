package de.lukasneugebauer.nextcloudcookbook.category.data.dto.mapper

import de.lukasneugebauer.nextcloudcookbook.category.data.dto.CategoryDto
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.CategoryEntity

fun CategoryEntity.toDto(): CategoryDto =
    CategoryDto(
        name = name,
        recipeCount = recipeCount,
    )

fun CategoryDto.toEntity(): CategoryEntity =
    CategoryEntity(
        name = name,
        recipeCount = recipeCount,
    )