package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import de.lukasneugebauer.nextcloudcookbook.R

@Composable
fun CommonItem(
    name: String,
    imageUrl: String,
    width: Dp,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(width),
    ) {
        Column {
            AuthorizedImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(width),
            )
            CommonItemBody(
                name = name,
                modifier = Modifier.width(width),
            )
        }
    }
}

@Composable
fun CommonItemBody(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = name,
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .padding(dimensionResource(id = R.dimen.padding_s))
                .wrapContentWidth(Alignment.Start),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.bodyLarge,
    )
}
