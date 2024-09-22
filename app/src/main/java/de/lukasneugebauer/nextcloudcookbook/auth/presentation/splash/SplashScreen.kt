package de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.SplashScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainViewModel
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.destinations.HomeScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.SplashScreenDestination

@RootNavGraph(start = true)
@Destination
@Composable
fun SplashScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = viewModel(),
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            SplashScreenState.Initial -> viewModel.initialize()
            SplashScreenState.Authorized -> {
                mainViewModel.finishSplash()
                navigator.navigate(HomeScreenDestination) {
                    popUpTo(SplashScreenDestination) {
                        inclusive = true
                    }
                }
            }
            SplashScreenState.Unauthorized -> {
                mainViewModel.finishSplash()
                navigator.navigate(LoginScreenDestination) {
                    popUpTo(SplashScreenDestination) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
