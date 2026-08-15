package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

/**
 * Carries conflict context when a 409 response indicates a recipe with the same name
 * already exists on the server.
 *
 * @param id ID of the existing conflicting recipe, or `null` if not found in the local cache.
 * @param name Name of the conflicting recipe.
 */
data class RecipeConflictDto(
    val id: String?,
    val name: String,
)
