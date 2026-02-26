package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.domain.model.LocalCredentials
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.authorizedImageRequest
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.common_back),
                                tint = Color.White,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )
            },
            containerColor = Color.Black.copy(alpha = 0.75f),
        ) { _ ->
            ZoomableAsyncImage(
                modifier = Modifier.fillMaxSize(),
                state = rememberZoomableImageState(),
                model = imageRequest,
                contentDescription = contentDescription,
            )
        }
    }
}
