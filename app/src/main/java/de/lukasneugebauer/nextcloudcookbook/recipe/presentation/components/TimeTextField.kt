package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents

@Composable
fun TimeTextField(
    time: DurationComponents,
    onTimeChange: (time: DurationComponents) -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    hoursKeyboardActions: KeyboardActions = KeyboardActions.Default,
    minutesKeyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = label),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = time.hours,
                onValueChange = { onTimeChange(DurationComponents(it, time.minutes)) },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(id = R.string.common_hours)) },
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = hoursKeyboardActions,
                singleLine = true,
                colors = colors,
            )
            Text(text = ":")
            OutlinedTextField(
                value = time.minutes,
                onValueChange = { onTimeChange(DurationComponents(time.hours, it)) },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(id = R.string.common_minutes)) },
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = minutesKeyboardActions,
                singleLine = true,
                colors = colors,
            )
        }
    }
}

@Preview(widthDp = 400, showBackground = true)
@Composable
private fun TimeTextFieldPreview() {
    NextcloudCookbookTheme {
        TimeTextField(
            time = DurationComponents("1", "25"),
            onTimeChange = {},
            label = R.string.recipe_cook_time,
        )
    }
}
