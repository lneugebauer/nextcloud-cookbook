package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.rememberNavHostEngine
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.NavGraphs
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash.SplashScreen
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Credentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AppState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AuthState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.LocalAppState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.SplashState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.SplashScreenDestination
import org.acra.ACRA
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val appState = AppState()
            val authState by viewModel.authState.collectAsState()
            val intent by viewModel.intentState.collectAsState()
            val splashState by viewModel.splashState.collectAsState()
            val credentials: Credentials? by remember {
                derivedStateOf {
                    when (authState) {
                        is AuthState.Unauthorized -> null
                        is AuthState.Authorized -> (authState as AuthState.Authorized).credentials
                    }
                }
            }
            val keepOnScreen by remember {
                derivedStateOf {
                    when (splashState) {
                        SplashState.Initial -> true
                        SplashState.Loaded -> false
                    }
                }
            }

            SideEffect {
                splashScreen.setKeepOnScreenCondition { keepOnScreen }
            }

            CompositionLocalProvider(
                LocalAppState provides appState,
                LocalCredentials provides credentials,
            ) {
                NextcloudCookbookApp(intent = intent)
            }
        }
    }

    override fun onResume() {
        this.addOnNewIntentListener {
            viewModel.setIntent(it)
        }
        super.onResume()
    }
}

@Composable
fun NextcloudCookbookApp(intent: Intent) {
    NextcloudCookbookTheme {
        val navController = rememberNavController()
        val navHostEngine =
            rememberNavHostEngine(
                rootDefaultAnimations =
                    RootNavGraphDefaultAnimations(
                        enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up) },
                        exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down) },
                        popEnterTransition = { fadeIn(animationSpec = tween(500)) },
                        popExitTransition = { fadeOut(animationSpec = tween(500)) },
                    ),
            )
        val viewModelStoreOwner =
            checkNotNull(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            destination.route?.let {
                ACRA.errorReporter.putCustomData("Event at ${System.currentTimeMillis()}", it)
            }
        }

        LaunchedEffect(intent) {
            Timber.d(navController.currentBackStackEntry.toString())
            navController.handleDeepLink(intent)
        }

        Scaffold(
            bottomBar = { BottomBar(navController = navController) },
        ) { innerPadding ->
            DestinationsNavHost(
                navGraph = NavGraphs.root,
                modifier =
                    Modifier
                        .safeDrawingPadding()
                        .padding(innerPadding)
                        .fillMaxSize(),
                engine = navHostEngine,
                navController = navController,
            ) {
                composable(SplashScreenDestination) {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner,
                    ) {
                        SplashScreen(navigator = destinationsNavigator)
                    }
                }
            }
        }
    }
}
