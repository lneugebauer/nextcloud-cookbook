package de.lukasneugebauer.nextcloudcookbook.ui.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.ui.theme.NcBlue

@Composable
fun TopBar() {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More")
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { /* Handle settings! */ }) {
                        Text(text = stringResource(R.string.common_settings))
                    }
                }
            }
        },
        backgroundColor = NcBlue,
        contentColor = Color.White
    )
}