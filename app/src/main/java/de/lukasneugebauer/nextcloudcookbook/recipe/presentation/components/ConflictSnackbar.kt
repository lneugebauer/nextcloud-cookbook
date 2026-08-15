package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.lukasneugebauer.nextcloudcookbook.R

@Composable
fun ConflictSnackbar(
    conflictingRecipeName: String?,
    conflictingRecipeId: String?,
    onViewOriginal: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val actionLabelText = stringResource(id = R.string.banner_action_view_original)
    val recipeExistsMessage = stringResource(id = R.string.error_recipe_exists, conflictingRecipeName.orEmpty())

    LaunchedEffect(conflictingRecipeName, conflictingRecipeId) {
        if (conflictingRecipeName != null) {
            val result =
                snackbarHostState.showSnackbar(
                    message = recipeExistsMessage,
                    actionLabel = if (conflictingRecipeId != null) actionLabelText else null,
                    duration = SnackbarDuration.Long,
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    if (conflictingRecipeId != null) onViewOriginal(conflictingRecipeId)
                    onDismiss()
                }
                SnackbarResult.Dismissed -> onDismiss()
            }
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data -> Snackbar(snackbarData = data, actionOnNewLine = true) },
    )
}
