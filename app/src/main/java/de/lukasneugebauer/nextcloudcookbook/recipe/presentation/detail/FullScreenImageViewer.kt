package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorizedImageRequest
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    contentDescription: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        val context = LocalContext.current
        val credentials = LocalCredentials.current

        val imageRequest = authorizedImageRequest(imageUrl, context, credentials)
        Box(modifier = Modifier.fillMaxSize()) {
            ZoomableAsyncImage(
                modifier = Modifier.fillMaxSize(),
                state = rememberZoomableImageState(),
                model = imageRequest,
                contentDescription = contentDescription,
            )

            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close recipe image",
                    tint = Color.White,
                )
            }
        }
    }
}
