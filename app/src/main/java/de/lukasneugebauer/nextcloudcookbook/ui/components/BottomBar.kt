package de.lukasneugebauer.nextcloudcookbook.ui.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen

@Composable
fun BottomBar(
    allScreens: List<NextcloudCookbookScreen>,
    navController: NavHostController,
    currentScreen: NextcloudCookbookScreen
) {
    BottomNavigation(elevation = 4.dp) {
        allScreens
            .filter { it.bottomBar }
            .map {
                BottomNavigationItem(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.name
                        )
                    },
                    label = { Text(text = it.name) },
                    selected = currentScreen == it,
                    onClick = { navController.navigate(it.name) }
                )
            }
    }
}