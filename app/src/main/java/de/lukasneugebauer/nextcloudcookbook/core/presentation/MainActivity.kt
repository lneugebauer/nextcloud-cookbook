package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NavGraphs
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.LaunchScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination

@ExperimentalCoilApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NextcloudCookbookApp()
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun NextcloudCookbookApp() {
    NextcloudCookbookTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Scaffold(
            bottomBar = {
                if (currentDestination?.route != LaunchScreenDestination.route &&
                    currentDestination?.route != LoginScreenDestination.route
                ) {
                    BottomBar(navController = navController)
                }
            }
        ) { innerPadding ->
            DestinationsNavHost(
                navGraph = NavGraphs.root,
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
    }
}
