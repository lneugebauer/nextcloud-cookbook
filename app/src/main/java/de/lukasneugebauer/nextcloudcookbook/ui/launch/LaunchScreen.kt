package de.lukasneugebauer.nextcloudcookbook.ui.launch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NcBlueGradient

@Composable
fun LaunchScreen(
    navController: NavController,
    viewModel: LaunchViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(key1 = state) {
        if (state is LaunchScreenState.Loaded) {
            if (state.authenticated) {
                // TODO: 04.10.21 Load (and cache) recipes initially
                navController.navigate(NextcloudCookbookScreen.Home.name)
            } else {
                navController.navigate(NextcloudCookbookScreen.Login.name)
            }
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