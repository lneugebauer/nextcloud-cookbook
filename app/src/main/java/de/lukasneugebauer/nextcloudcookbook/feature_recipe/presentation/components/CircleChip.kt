package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue

@Composable
fun CircleChip(text: String, modifier: Modifier) {
    Box(
        Modifier
            .aspectRatio(1f)
            .border(width = 2.dp, color = NcBlue, shape = CircleShape)
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_s),
                vertical = dimensionResource(id = R.dimen.padding_xs)
            )
            .heightIn(min = dimensionResource(id = R.dimen.chip_min_height))
            .wrapContentSize()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2
        )
    }
}