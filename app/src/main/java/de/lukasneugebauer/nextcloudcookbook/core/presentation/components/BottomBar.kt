package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.ramcosta.composedestinations.generated.destinations.CategoryListScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RecipeListScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.LocalAppState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    @StringRes val label: Int,
) {
    Home(HomeScreenDestination, Icons.Default.Home, R.string.common_home),
    Categories(CategoryListScreenDestination, Icons.Default.Category, R.string.common_categories),
    Recipes(RecipeListScreenDestination, Icons.Default.Fastfood, R.string.common_recipes),
}

@Composable
fun BottomBar(navController: NavController) {
    val appState = LocalAppState.current
    val destinationsNavigator = navController.rememberDestinationsNavigator()
    var selected by rememberSaveable { mutableStateOf(BottomBarDestination.Home) }

    if (appState.isBottomBarVisible) {
        BottomBarContent(
            selected = selected,
            onClick = { destination ->
                selected = destination
                destinationsNavigator.navigate(destination.direction) {
                    launchSingleTop = true
                }
            },
        )
    }
}

@Composable
fun BottomBarContent(
    selected: BottomBarDestination,
    onClick: (destination: BottomBarDestination) -> Unit,
) {
    NavigationBar {
        BottomBarDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onClick.invoke(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}

@Preview
@Composable
private fun BottomBarContentPreview() {
    NextcloudCookbookTheme {
        BottomBarContent(selected = BottomBarDestination.Home, onClick = {})
    }
}
