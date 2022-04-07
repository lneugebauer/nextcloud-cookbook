package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.model.Recipe

@Composable
fun CreateEditRecipeForm(
    recipe: Recipe,
    onNavIconClick: () -> Unit,
    onNameChanged: (name: String) -> Unit,
    onDescriptionChanged: (description: String) -> Unit,
    onUrlChanged: (url: String) -> Unit,
    onYieldChanged: (yield: String) -> Unit,
    onIngredientChanged: (index: Int, ingredient: String) -> Unit,
    onIngredientDeleted: (index: Int) -> Unit,
    onAddIngredient: () -> Unit,
    onInstructionChanged: (index: Int, instruction: String) -> Unit,
    onInstructionDeleted: (index: Int) -> Unit,
    onAddInstruction: () -> Unit,
    onSaveClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // TODO: Hide bottom navigation

    Scaffold(
        topBar = {
            RecipeEditTopBar(
                title = recipe.name,
                onNavIconClick = onNavIconClick,
                onSaveClick = onSaveClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .verticalScroll(scrollState)
        ) {
            val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.Black,
                cursorColor = Color.Black,
                focusedBorderColor = NcBlue700,
                unfocusedBorderColor = NcBlue700
            )

            Gap(size = dimensionResource(id = R.dimen.padding_m))
            DefaultOutlinedTextField(
                value = recipe.name,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                label = { Text(text = "Name") },
                singleLine = true,
                colors = textFieldColors
            )
            DefaultOutlinedTextField(
                value = recipe.description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                label = { Text(text = "Description") },
                colors = textFieldColors
            )
            DefaultOutlinedTextField(
                value = recipe.url,
                onValueChange = onUrlChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                label = { Text(text = "URL") },
                singleLine = true,
                colors = textFieldColors
            )
            DefaultOutlinedTextField(
                value = recipe.yield.toString(),
                onValueChange = onYieldChanged,
                modifier = Modifier
                    .fillMaxWidth(1f / 3f)
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                label = { Text(text = "Yield") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                colors = textFieldColors
            )
            recipe.ingredients.forEachIndexed { index, ingredient ->
                DefaultOutlinedTextField(
                    value = ingredient,
                    onValueChange = { onIngredientChanged.invoke(index, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                    label = { Text(text = "Ingredient ${index + 1}") },
                    trailingIcon = {
                        IconButton(onClick = { onIngredientDeleted.invoke(index) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    colors = textFieldColors
                )
            }
            DefaultButton(
                onClick = onAddIngredient,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = NcBlue700,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add ingredient")
                Text(text = "Add ingredient")
            }
            recipe.instructions.forEachIndexed { index, instruction ->
                DefaultOutlinedTextField(
                    value = instruction,
                    onValueChange = { onInstructionChanged.invoke(index, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                    label = { Text(text = "Instruction ${index + 1}") },
                    trailingIcon = {
                        IconButton(onClick = { onInstructionDeleted.invoke(index) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    colors = textFieldColors
                )
            }
            DefaultButton(
                onClick = onAddInstruction,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = NcBlue700,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add instruction")
                Text(text = "Add instruction")
            }
        }
    }
}

@Composable
private fun RecipeEditTopBar(title: String, onNavIconClick: () -> Unit, onSaveClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onSaveClick) {
                Icon(
                    Icons.Outlined.Save,
                    contentDescription = "Save"
                )
            }
        },
        backgroundColor = NcBlue700,
        contentColor = Color.White
    )
}

@Preview
@Composable
private fun CreateEditRecipeFormPreview() {
    val recipe = Recipe(
        id = 1,
        name = "Lorem ipsum",
        description = "Lorem ipsum dolor sit amet",
        url = "https://www.example.com",
        imageUrl = "/apps/cookbook/recipes/1/image?size=full",
        category = "",
        keywords = emptyList(),
        yield = 2,
        prepTime = null,
        cookTime = null,
        totalTime = null,
        nutrition = null,
        tools = emptyList(),
        ingredients = List(2) {
            "Lorem ipsum"
        },
        instructions = List(1) {
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
        },
        createdAt = "",
        modifiedAt = ""
    )
    NextcloudCookbookTheme {
        CreateEditRecipeForm(
            recipe = recipe,
            onNavIconClick = {},
            onNameChanged = {},
            onDescriptionChanged = {},
            onUrlChanged = {},
            onYieldChanged = {},
            onIngredientChanged = { _, _ -> },
            onIngredientDeleted = {},
            onAddIngredient = {},
            onInstructionChanged = { _, _ -> },
            onInstructionDeleted = {},
            onAddInstruction = {},
            onSaveClick = {}
        )
    }
}
