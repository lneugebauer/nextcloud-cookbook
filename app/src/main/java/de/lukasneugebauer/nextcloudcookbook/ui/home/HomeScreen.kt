package de.lukasneugebauer.nextcloudcookbook.ui.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil.compose.rememberImagePainter
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.data.HomeScreenData
import de.lukasneugebauer.nextcloudcookbook.ui.components.*
import de.lukasneugebauer.nextcloudcookbook.utils.Logger
import okhttp3.Credentials

private const val TAG = "HomeScreen"

@ExperimentalMaterialApi
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state = viewModel.state.value
    if (state.data.isEmpty()) {
        Loader()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.padding_m))
        ) {
            items(state.data) { data ->
                when (data) {
                    is HomeScreenData.Grid -> {
                        Headline(text = stringResource(id = data.headline))
                        for (category in data.categories) {
                            CommonListItem(
                                name = category.name,
                                modifier = Modifier
                                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                                    .padding(bottom = dimensionResource(id = R.dimen.padding_s))
                            ) { /* On click */ }
                        }
                    }
                    is HomeScreenData.Row -> {
                        Headline(text = stringResource(id = data.headline, "CATEGORY"))
                        RowContainer(data = data.recipes.map { RowContent(it.name, it.imageUrl) })
                    }
                    is HomeScreenData.Single -> {
                        Headline(text = stringResource(id = data.headline))
                        SingleItem(name = data.recipe.name, imageUrl = data.recipe.imageUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleItem(name: String, imageUrl: Uri) {
    Card(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_l))
    ) {
        Column {
            Image(
                painter = rememberImagePainter(
                    data = imageUrl,
                    builder = {
                        val credentials = Credentials.basic(
                            BuildConfig.NC_USERNAME,
                            BuildConfig.NC_APP_PASSWORD
                        )
                        addHeader("Authorization", credentials)
                        crossfade(750)
                    }),
                contentDescription = name,
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .fillMaxWidth()
            )
            CommonItemBody(name = name, modifier = Modifier.fillMaxWidth()) {
                Logger.d("More icon clicked", TAG)
            }
        }
    }
}