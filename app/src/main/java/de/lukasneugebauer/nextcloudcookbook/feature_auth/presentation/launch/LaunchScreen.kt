package de.lukasneugebauer.nextcloudcookbook.feature_auth.presentation.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlueGradient
import de.lukasneugebauer.nextcloudcookbook.destinations.HomeScreenDestination
import de.lukasneugebauer.nextcloudcookbook.destinations.LoginScreenDestination

@OptIn(ExperimentalMaterialApi::class)
@Destination(start = true)
@Composable
fun LaunchScreen(
    navigator: DestinationsNavigator,
    viewModel: LaunchViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(key1 = state) {
        if (state.authorized == true) {
            // TODO: 04.10.21 Load (and cache) recipes initially
            navigator.navigate(HomeScreenDestination())
        }
        if (state.authorized == false) {
            navigator.navigate(LoginScreenDestination())
        }
    }

    LaunchScreenContent()
}

@Composable
fun LaunchScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NcBlueGradient),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo",
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.launcher_logo_width))
                .fillMaxWidth(),
            alignment = Alignment.Center
        )
    }
}

@Composable
@Preview
fun LaunchScreenContentPreview() {
    LaunchScreenContent()
}
