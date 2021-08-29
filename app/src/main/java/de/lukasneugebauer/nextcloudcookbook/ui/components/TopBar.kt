package de.lukasneugebauer.nextcloudcookbook.ui.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import de.lukasneugebauer.nextcloudcookbook.NextcloudCookbookScreen
import de.lukasneugebauer.nextcloudcookbook.R

@Composable
fun TopBar(currentScreen: NextcloudCookbookScreen) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = currentScreen.name) },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More")
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                        Text(text = stringResource(R.string.common_settings))
                    }
                }
            }
        }
    )
}