package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700

@Composable
fun Chip(text: String) {
    Box(
        modifier = Modifier
            .border(width = 2.dp, color = NcBlue700, shape = CircleShape)
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_s),
                vertical = dimensionResource(id = R.dimen.padding_xs),
            )
            .heightIn(min = dimensionResource(id = R.dimen.chip_min_height)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.body2)
    }
}
