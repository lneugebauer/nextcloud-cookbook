package de.lukasneugebauer.nextcloudcookbook.tasks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.tasks.util.IngredientQuantityParser

@Composable
fun AddToShoppingListDialog(
    ingredients: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val items = remember(ingredients) { ingredients.map { it.trim() to IngredientQuantityParser.parse(it) } }
    val includedStates = remember(items) { items.map { true }.toMutableStateList() }
    val withQuantityStates = remember(items) { items.map { true }.toMutableStateList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.shopping_list_dialog_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.width(CHECKBOX_COLUMN_WIDTH),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.shopping_list_dialog_amount),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                items.forEachIndexed { index, (original, parsed) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includedStates[index],
                            onCheckedChange = { includedStates[index] = it },
                        )
                        Text(
                            text = original,
                            modifier = Modifier.weight(1f),
                        )
                        if (parsed.quantity != null) {
                            Checkbox(
                                checked = withQuantityStates[index],
                                onCheckedChange = { withQuantityStates[index] = it },
                                enabled = includedStates[index],
                            )
                        } else {
                            Spacer(modifier = Modifier.size(CHECKBOX_COLUMN_WIDTH))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val entries =
                        items.mapIndexedNotNull { index, (original, parsed) ->
                            when {
                                !includedStates[index] -> null
                                parsed.quantity != null && !withQuantityStates[index] -> parsed.name
                                else -> original
                            }
                        }
                    onConfirm(entries)
                },
                enabled = includedStates.any { it },
            ) {
                Text(text = stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

private val CHECKBOX_COLUMN_WIDTH = 48.dp

@Preview
@Composable
private fun AddToShoppingListDialogPreview() {
    NextcloudCookbookTheme {
        AddToShoppingListDialog(
            ingredients =
                listOf(
                    "200 g Mehl",
                    "1/2 TL Salz",
                    "Pfeffer nach Geschmack",
                ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
