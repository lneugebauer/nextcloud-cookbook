package de.lukasneugebauer.nextcloudcookbook.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import de.lukasneugebauer.nextcloudcookbook.BuildConfig
import okhttp3.Credentials

@Composable
fun AuthorizedImage(imageUrl: Uri, contentDescription: String, modifier: Modifier) {
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
            }
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}