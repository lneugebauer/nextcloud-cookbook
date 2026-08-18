package de.lukasneugebauer.nextcloudcookbook.recipe.data.dto

import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText

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
) {
    fun toUiText(): UiText =
        if (name.isBlank()) {
            UiText.StringResource(R.string.error_http_409)
        } else {
            UiText.StringResource(R.string.error_recipe_exists, name as Any)
        }
}
