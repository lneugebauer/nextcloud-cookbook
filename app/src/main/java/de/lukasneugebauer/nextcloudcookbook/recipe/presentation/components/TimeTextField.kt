package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInput(
    time: DurationComponents,
    onTimeChange: (time: DurationComponents) -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
) {
    val initialHour =
        if (time.hours.isNotBlank()) {
            time.hours.toInt()
        } else {
            0
        }

    val initialMinute =
        if (time.minutes.isNotBlank()) {
            time.minutes.toInt()
        } else {
            0
        }

    val timePickerState =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(
            DurationComponents(
                timePickerState.hour.toString(),
                timePickerState.minute.toString(),
            ),
        )
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = label),
            style = MaterialTheme.typography.bodySmall,
        )
        TimeInput(
            state = timePickerState,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun TimeTextFieldPreview() {
    NextcloudCookbookTheme {
        TimeInput(
            time = DurationComponents("1", "25"),
            onTimeChange = {},
            label = R.string.recipe_cook_time,
        )
    }
}
