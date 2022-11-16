package de.lukasneugebauer.nextcloudcookbook.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.auth.domain.state.SplashScreenState
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainViewModel
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.destinations.HomeScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.SplashScreenDestination

@RootNavGraph(start = true)
@Destination
@Composable
fun SplashScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = viewModel(),
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val unsupportedAppVersion by remember {
        derivedStateOf {
            uiState is SplashScreenState.UnsupportedAppVersion
        }
    }

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            SplashScreenState.Initial -> viewModel.initialize()
            SplashScreenState.Authorized -> {
                // TODO: 04.10.21 Load (and cache) recipes initially
                mainViewModel.finishSplash()
                navigator.navigate(HomeScreenDestination) {
                    popUpTo(SplashScreenDestination.route) {
                        inclusive = true
                    }
                }
            }
            SplashScreenState.Unauthorized -> {
                mainViewModel.finishSplash()
                navigator.navigate(LoginScreenDestination) {
                    popUpTo(SplashScreenDestination.route) {
                        inclusive = true
                    }
                }
            }
            SplashScreenState.UnsupportedAppVersion -> {
                mainViewModel.finishSplash()
            }
        }
    }

    if (unsupportedAppVersion) {
        UnsupportedAppVersionError()
    }
}

@Composable
private fun UnsupportedAppVersionError() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.error_unsupported_app_version),
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.padding_m))
        )
    }
}

@Preview(device = Devices.PIXEL_4)
@Composable
private fun UnsupportedAppVersionErrorPreview() {
    NextcloudCookbookTheme {
        UnsupportedAppVersionError()
    }
}
