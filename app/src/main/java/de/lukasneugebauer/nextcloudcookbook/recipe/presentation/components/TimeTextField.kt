package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme

@Composable
fun TimeTextField(
    hours: String,
    minutes: String,
    onHoursChange: (hours: String) -> Unit,
    onMinutesChange: (minutes: String) -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = Color.White,
        cursorColor = Color.White,
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White,
    ),
    hoursKeyboardActions: KeyboardActions = KeyboardActions.Default,
    minutesKeyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    // TODO: Change to TimeInput once Material 3 is implemented.
    //  https://m3.material.io/components/time-pickers/overview
    Column {
        Text(
            text = stringResource(id = label),
            style = MaterialTheme.typography.caption
        )
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = hours,
                onValueChange = onHoursChange,
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(id = R.string.common_hours)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = hoursKeyboardActions,
                singleLine = true,
                colors = colors,
            )
            Text(text = ":")
            OutlinedTextField(
                value = minutes,
                onValueChange = onMinutesChange,
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(id = R.string.common_minutes)) },
                keyboardOptions = KeyboardOptions.Default.copy(
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

@Preview(widthDp = 400)
@Composable
private fun TimeTextFieldPreview() {
    NextcloudCookbookTheme {
        TimeTextField(
            hours = "1",
            minutes = "25",
            onHoursChange = {},
            onMinutesChange = {},
            label = R.string.recipe_cook_time
        )
    }
}
