package de.lukasneugebauer.nextcloudcookbook.ui.recipies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.ui.components.CommonListItem
import de.lukasneugebauer.nextcloudcookbook.ui.components.Loader

@ExperimentalMaterialApi
@Composable
fun RecipesScreen(viewModel: RecipesViewModel) {
    val state = viewModel.state.value
    if (state.data.isEmpty()) {
        Loader()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = dimensionResource(id = R.dimen.padding_m),
                vertical = dimensionResource(id = R.dimen.padding_s)
            ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s))
        ) {
            items(state.data) {
                CommonListItem(name = it.name, imageUrl = it.imageUrl) { /* On click */ }
            }
        }
    }
}