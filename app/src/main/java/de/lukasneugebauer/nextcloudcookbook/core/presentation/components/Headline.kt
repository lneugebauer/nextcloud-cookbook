package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R

@Composable
fun Headline(text: String, moreButton: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
            .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h5
        )
        if (moreButton) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "More"
                )
            }
        }
    }
}

@Preview
@Composable
fun HeadlinePreview() {
    Headline(text = "Headline", moreButton = true, onClick = {})
}