package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials

@Composable
fun AuthorizedImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val credentials = LocalCredentials.current

    val path = credentials?.baseUrl?.toUri()?.path
    val regex = """^$path""".toRegex()
    val newImageUrl = imageUrl.replace(regex, "")
    val fullImageUrl = credentials?.baseUrl + newImageUrl

    val headers =
        NetworkHeaders.Builder()
            .set("Authorization", credentials?.basic ?: "")
            .build()

    SubcomposeAsyncImage(
        model =
            ImageRequest.Builder(context)
                .data(fullImageUrl)
                .httpHeaders(headers)
                .crossfade(true)
                .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    ) {
        val state by painter.state.collectAsState()
        when (state) {
            is AsyncImagePainter.State.Error -> {
                Image(
                    painter =
                        rememberDrawablePainter(
                            drawable =
                                AppCompatResources.getDrawable(
                                    context,
                                    R.drawable.common_image_placeholder,
                                ),
                        ),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = ContentScale.Crop,
                )
            }

            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}
