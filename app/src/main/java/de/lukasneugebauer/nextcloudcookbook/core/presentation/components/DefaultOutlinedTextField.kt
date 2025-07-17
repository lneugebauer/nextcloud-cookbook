package de.lukasneugebauer.nextcloudcookbook.core.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme

@Composable
fun DefaultOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    colors: TextFieldColors =
        OutlinedTextFieldDefaults.colors(),
) {
    val isError = errorText?.isNotBlank() == true

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            readOnly = readOnly,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon =
                if (isError) {
                    {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = stringResource(R.string.common_error),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    trailingIcon
                },
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            interactionSource = interactionSource,
            colors = colors,
        )
        if (isError) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
            )
        }
    }
}

@Preview
@Composable
private fun DefaultOutlinedTextFieldPreview() {
    NextcloudCookbookTheme {
        DefaultOutlinedTextField(value = "OutlinedTextField", onValueChange = {})
    }
}

@Preview
@Composable
private fun DefaultOutlinedTextFieldWithErrorPreview() {
    NextcloudCookbookTheme {
        DefaultOutlinedTextField(
            value = "OutlinedTextField",
            onValueChange = {},
            errorText = "Error message",
        )
    }
}
