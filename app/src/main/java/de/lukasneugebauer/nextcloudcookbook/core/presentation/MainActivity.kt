package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NavGraphs
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Credentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AuthState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.SplashState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.SplashScreenDestination
import de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.splash.SplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var keepOnScreen = true
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { keepOnScreen }

        viewModel.splashState.observe(this) { state ->
            keepOnScreen = when (state) {
                SplashState.Initial -> true
                SplashState.Loaded -> false
            }
        }

        setContent {
            val authState by viewModel.authState.collectAsState()
            val credentials: Credentials? by derivedStateOf {
                when (authState) {
                    is AuthState.Unauthorized -> null
                    is AuthState.Authorized -> (authState as AuthState.Authorized).credentials
                }
            }

            CompositionLocalProvider(LocalCredentials provides credentials) {
                NextcloudCookbookApp()
            }
        }
    }
}

@Composable
fun NextcloudCookbookApp() {
    NextcloudCookbookTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }

        Scaffold(
            bottomBar = {
                if (currentDestination?.route != SplashScreenDestination.route &&
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
            ) {
                composable(SplashScreenDestination) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        SplashScreen(navigator = destinationsNavigator)
                    }
                }
            }
        }
    }
}
