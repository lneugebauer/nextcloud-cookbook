package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R

@Composable
fun Headline(
    text: String,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clickable(enabled = clickable, onClick = onClick)
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_m),
                    vertical = dimensionResource(id = R.dimen.padding_s),
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (clickable) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = stringResource(id = R.string.common_more),
            )
        }
    }
}

@Preview
@Composable
fun HeadlinePreview() {
    Headline(text = "Headline", clickable = true, onClick = {})
}

@Preview
@Composable
fun HeadlinePreviewWithALotOfText() {
    Headline(text = "Headline headline headline headline", clickable = true, onClick = {})
}

@Preview
@Composable
fun HeadlinePreviewWithoutMoreButton() {
    Headline(text = "Headline", clickable = false, onClick = {})
}
