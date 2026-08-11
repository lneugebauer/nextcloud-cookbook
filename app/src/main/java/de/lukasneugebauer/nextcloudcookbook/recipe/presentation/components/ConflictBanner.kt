package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.lukasneugebauer.nextcloudcookbook.R

@Composable
fun ConflictSnackbar(
    conflictingRecipeName: String,
    conflictingRecipeId: String?,
    onViewOriginal: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = stringResource(id = R.string.error_recipe_exists, conflictingRecipeName)
    val actionLabelText = if (conflictingRecipeId != null) {
        stringResource(id = R.string.banner_action_view_original, conflictingRecipeName)
    } else {
        null
    }

    LaunchedEffect(conflictingRecipeName, conflictingRecipeId) {
        val result = snackbarHostState.showSnackbar(
            message = messageText,
            actionLabel = actionLabelText,
            duration = SnackbarDuration.Indefinite,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                if (conflictingRecipeId != null) {
                    onViewOriginal(conflictingRecipeId)
                }
                onDismiss()
            }
            SnackbarResult.Dismissed -> {
                onDismiss()
            }
        }
    }

    LaunchedEffect(conflictingRecipeName, conflictingRecipeId) {
        kotlinx.coroutines.delay(10000)
        onDismiss()
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                actionOnNewLine = true,
            )
        },
    )
}
