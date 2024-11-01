package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents

@Composable
fun TimeInputTextField(
    time: DurationComponents,
    modifier: Modifier,
    @StringRes label: Int,
    onTimeChange: (time: DurationComponents) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    val openDialog = remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Focus -> {
                    openDialog.value = true
                }
            }
        }
    }

    DefaultOutlinedTextField(
        value = "${time.hours}:${time.minutes}",
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        label = { Text(text = stringResource(id = label)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "",
            )
        },
        singleLine = true,
        interactionSource = interactionSource,
    )

    val timePickerState =
        rememberTimePickerState(
            initialMinute = time.minutes.toIntOrNull() ?: 0,
            initialHour = time.hours.toIntOrNull() ?: 0,
            is24Hour = true,
        )

    TimePickerDialog(
        timePickerState = timePickerState,
        label = label,
        openDialog = openDialog.value,
        onDismiss = {
            openDialog.value = false
            onTimeChange.invoke(
                DurationComponents(
                    minutes = timePickerState.minute.toString(),
                    hours = timePickerState.hour.toString(),
                ),
            )
            focusManager.moveFocus(FocusDirection.Down)
        },
    )
}

@Composable
private fun TimePickerDialog(
    timePickerState: TimePickerState = rememberTimePickerState(),
    modifier: Modifier = Modifier,
    @StringRes label: Int,
    openDialog: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(text = stringResource(android.R.string.ok)) }
            },
            modifier = modifier,
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(text = stringResource(android.R.string.cancel)) }
            },
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text(
                    text = stringResource(id = label),
                    modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_m)),
                    style = MaterialTheme.typography.headlineSmall,
                )
                TimeInput(state = timePickerState)
            }
        }
    }
}

@Preview(device = Devices.PIXEL_7)
@Composable
private fun TimePickerDialogPreview() {
    NextcloudCookbookTheme {
        Scaffold { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(innerPadding),
            ) {
                TimePickerDialog(label = R.string.recipe_prep_time, openDialog = true)
            }
        }
    }
}
