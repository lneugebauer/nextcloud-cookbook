package de.lukasneugebauer.nextcloudcookbook.features.recipes

import androidx.compose.runtime.Composable
import de.lukasneugebauer.nextcloudcookbook.data.RecipePreview
import de.lukasneugebauer.nextcloudcookbook.ui.components.ListItem
import de.lukasneugebauer.nextcloudcookbook.utils.Logger

@Composable
fun RecipeListItem(
    recipe: RecipePreview
) {
    ListItem(
        onClick = { Logger.d("Clicked recipe ${recipe.name} (${recipe.id}") },
        imageUrl = recipe.imageUrl,
        title = recipe.name
    )
}