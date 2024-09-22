package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.LocalAppState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.destinations.CategoryListScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.HomeScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.RecipeListScreenDestination

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
        BottomNavigation(backgroundColor = NcBlue700) {
            BottomBarDestination.entries.forEach { destination ->
                BottomNavigationItem(
                    selected = selected == destination,
                    onClick = {
                        selected = destination
                        destinationsNavigator.navigate(destination.direction) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = stringResource(destination.label),
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                    selectedContentColor = Color.White,
                )
            }
        }
    }
}
