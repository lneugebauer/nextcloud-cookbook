package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import coil.annotation.ExperimentalCoilApi
import de.lukasneugebauer.nextcloudcookbook.R

data class RowContent(
    val name: String,
    val imageUrl: String,
    val onClick: () -> Unit
)

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun RowContainer(data: List<RowContent>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s))
    ) {
        items(data) {
            RowItem(name = it.name, imageUrl = it.imageUrl, it.onClick)
        }
    }
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun RowItem(name: String, imageUrl: String, onClick: () -> Unit) {
    CommonItem(
        name = name,
        imageUrl = imageUrl,
        width = dimensionResource(id = R.dimen.common_item_width_m),
        onClick = onClick
    )
}
