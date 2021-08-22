package de.lukasneugebauer.nextcloudcookbook.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import okhttp3.Credentials

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListItem(
    onClick: () -> Unit,
    imageUrl: Uri,
    title: String,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Row {
            Image(
                painter = rememberImagePainter(
                    data = imageUrl,
                    builder = {
                        val credentials =
                            Credentials.basic(BuildConfig.NC_USERNAME, BuildConfig.NC_APP_PASSWORD)
                        addHeader("Authorization", credentials)
                        crossfade(750)
                    }
                ),
                contentDescription = title,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .size(96.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = title,
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                style = MaterialTheme.typography.h5
            )
        }
    }
}