package de.lukasneugebauer.nextcloudcookbook.settings.presentation.imageuploadfolder

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation

@Destination<MainGraph>
@Composable
fun AnimatedVisibilityScope.RecipeImageUploadFolderScreen(
    navigator: DestinationsNavigator,
    viewModel: RecipeImageUploadFolderViewModel = hiltViewModel(),
) {
    val folder by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    Scaffold(
        topBar = {
            RecipeImageUploadFolderTopBar {
                navigator.navigateUp()
            }
        },
    ) { innerPadding ->
        RecipeImageUploadFolderScreen(
            folder = folder,
            onFolderChange = { viewModel.updateFolder(it) },
            onResetClick = { viewModel.reset() },
            onSaveClick = {
                viewModel.save()
                navigator.navigateUp()
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun RecipeImageUploadFolderScreen(
    folder: String,
    onFolderChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSave = folder.isNotBlank()

    Column(
        modifier = modifier,
    ) {
        DefaultOutlinedTextField(
            value = folder,
            onValueChange = onFolderChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_xs)),
            label = { Text(text = stringResource(R.string.settings_recipe_image_upload_folder)) },
            placeholder = { Text(text = stringResource(R.string.settings_recipe_image_upload_folder_placeholder)) },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = { if (canSave) onSaveClick.invoke() },
                ),
            singleLine = true,
        )
        Text(
            text = stringResource(R.string.settings_recipe_image_upload_folder_description),
            modifier =
                Modifier.padding(
                    horizontal = dimensionResource(R.dimen.padding_m),
                    vertical = dimensionResource(R.dimen.padding_xs),
                ),
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = onSaveClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(top = dimensionResource(id = R.dimen.padding_s)),
            enabled = canSave,
        ) {
            Text(text = stringResource(R.string.common_save))
        }
        TextButton(
            onClick = onResetClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        ) {
            Text(text = stringResource(R.string.settings_recipe_image_upload_folder_reset))
        }
    }
}

@Composable
private fun RecipeImageUploadFolderTopBar(onNavIconClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_recipe_image_upload_folder),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                )
            }
        },
    )
}
