package de.lukasneugebauer.nextcloudcookbook

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class NextcloudCookbookScreen(
    val icon: ImageVector
) {
    Home(
        icon = Icons.Filled.Home
    ),
    Categories(
        icon = Icons.Filled.Category
    ),
    Recipes(
        icon = Icons.Filled.Fastfood
    ),
    Search(
        icon = Icons.Filled.Search
    );

    companion object {
        fun fromRoute(route: String?): NextcloudCookbookScreen =
            when (route?.substringBefore("/")) {
                Home.name -> Home
                Categories.name -> Categories
                Recipes.name -> Recipes
                Search.name -> Search
                null -> Home
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}