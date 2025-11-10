package de.lukasneugebauer.nextcloudcookbook.core.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.generated.destinations.SplashScreenDestination
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import dagger.hilt.android.AndroidEntryPoint
import de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash.SplashScreen
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.Credentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AppState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.AuthState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.LocalAppState
import de.lukasneugebauer.nextcloudcookbook.core.domain.state.SplashState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.BottomBar
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import org.acra.ACRA
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val appState = remember { AppState() }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        viewModel.setIntent(intent)
    }
}

@Composable
fun NextcloudCookbookApp(intent: Intent?) {
    NextcloudCookbookTheme {
        val navController = rememberNavController()
        val configuration = LocalConfiguration.current
        val appState = LocalAppState.current

        val viewModelStoreOwner =
            checkNotNull(LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }

        val wasHiddenByScroll = rememberSaveable { mutableStateOf(false) }

        val isPhone = configuration.smallestScreenWidthDp < 600
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val shouldUseScrollBehavior = isPhone && isLandscape

        val nestedScrollConnection =
            remember(shouldUseScrollBehavior, appState) {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource,
                    ): Offset {
                        if (!shouldUseScrollBehavior) return Offset.Zero

                        if (!appState.isBottomBarVisible && !wasHiddenByScroll.value) {
                            return Offset.Zero
                        }

                        val delta = available.y
                        if (delta < -10f && appState.isBottomBarVisible) {
                            appState.isBottomBarVisible = false
                            wasHiddenByScroll.value = true
                        } else if (delta > 10f && !appState.isBottomBarVisible && wasHiddenByScroll.value) {
                            appState.isBottomBarVisible = true
                            wasHiddenByScroll.value = false
                        }
                        return Offset.Zero
                    }
                }
            }

        DisposableEffect(configuration, shouldUseScrollBehavior) {
            if (!shouldUseScrollBehavior) {
                if (wasHiddenByScroll.value) {
                    appState.isBottomBarVisible = true
                    wasHiddenByScroll.value = false
                }
            }
            onDispose { }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            destination.route?.let {
                ACRA.errorReporter.putCustomData("Event at ${System.currentTimeMillis()}", it)
            }
        }

        LaunchedEffect(intent) {
            Timber.d("currentBackStackEntry: ${navController.currentBackStackEntry}")
            if (intent?.data != null && intent.action != Intent.ACTION_MAIN) {
                try {
                    navController.handleDeepLink(intent)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to handle deep link: ${intent.data}")
                }
            }

            // Handle share intents (text/plain) and route them to the DownloadRecipeScreen
            if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
                if (sharedText.isNotEmpty()) {
                    Timber.i("Received shared text: $sharedText")
                    try {
                        // Extract first https URL from sharedText and navigate with it as a query parameter
                        val matcher = android.util.Patterns.WEB_URL.matcher(sharedText)
                        var foundUrl: String? = null
                        while (matcher.find()) {
                          val candidate = matcher.group()
                          if (!candidate.isNullOrEmpty() && candidate.startsWith("https://", true)) {
                            foundUrl = candidate
                            break
                          }
                        }

                        if (!foundUrl.isNullOrEmpty()) {
                          val encoded = java.net.URLEncoder.encode(foundUrl, "UTF-8")
                          val downloadRoute = com.ramcosta.composedestinations.generated.destinations.DownloadRecipeScreenDestination.route
                          val routeWithArg = "$downloadRoute?sharedUrl=$encoded"
                          navController.navigate(routeWithArg)
                        } else {
                          Timber.w("No https URL found in shared text: $sharedText")
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to route share intent to DownloadRecipeScreen: $sharedText")
                    }
                 }
             }
        }

        val layoutDirection = LocalLayoutDirection.current
        // Material 3 Navigation Bar standard height matches NavigationBarTokens.ContainerHeight (80dp)
        // Since NavigationBarTokens is internal, we use the standard value directly
        val bottomBarHeight = 80.dp

        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                BottomBar(navController = navController)
            },
        ) { innerPadding ->
            val density = LocalDensity.current
            val safeDrawingBottom =
                with(density) {
                    WindowInsets.safeDrawing.getBottom(density).toDp()
                }
            val animatedBottomPadding by animateDpAsState(
                targetValue =
                    if (appState.isBottomBarVisible) {
                        safeDrawingBottom + bottomBarHeight
                    } else {
                        safeDrawingBottom
                    },
                label = "bottomPadding",
            )

            DestinationsNavHost(
                navGraph = MainNavGraph,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateLeftPadding(layoutDirection),
                            end = innerPadding.calculateRightPadding(layoutDirection),
                            bottom = animatedBottomPadding,
                        )
                        .consumeWindowInsets(WindowInsets.safeDrawing),
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

object DefaultTransitions : NavHostAnimatedDestinationStyle() {
    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up)
    }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
    }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(500))
    }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(500))
    }
}

@NavHostGraph(
    defaultTransitions = DefaultTransitions::class,
)
annotation class MainGraph
