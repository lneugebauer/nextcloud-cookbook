package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import android.annotation.SuppressLint
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme

@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalCoilApi::class)
@Composable
fun AuthorizedImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val credentials = LocalCredentials.current

    val path = credentials?.baseUrl?.toUri()?.path
    val regex = """^$path""".toRegex()
    val newImageUrl = imageUrl.replace(regex, "")
    val fullImageUrl = credentials?.baseUrl + newImageUrl

    val headers =
        NetworkHeaders
            .Builder()
            .set("Authorization", credentials?.basic ?: "")
            .build()

    val imageRequest =
        ImageRequest
            .Builder(context)
            .data(fullImageUrl)
            .httpHeaders(headers)
            .crossfade(true)
            .memoryCacheKey(key = imageUrl)
            .diskCacheKey(key = imageUrl)
            .build()

    val previewHandler =
        AsyncImagePreviewHandler {
            val resId =
                context.resources.getIdentifier(
                    imageUrl,
                    "drawable",
                    context.packageName,
                )
            val drawable = AppCompatResources.getDrawable(context, resId)
            drawable?.asImage()
        }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        SubcomposeAsyncImage(
            model = imageRequest,
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
}

@Preview(widthDp = 600, heightDp = 400)
@Composable
private fun AuthorizedImagePreview() {
    NextcloudCookbookTheme {
        AuthorizedImage(
            imageUrl = "https://placehold.co/600x400",
            contentDescription = "",
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeContentPadding(),
        )
    }
}
