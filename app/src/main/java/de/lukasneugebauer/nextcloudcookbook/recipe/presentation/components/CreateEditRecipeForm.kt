package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import java.time.Duration

@Composable
fun CreateEditRecipeForm(
    recipe: Recipe,
    @StringRes title: Int,
    onNavIconClick: () -> Unit,
    onNameChanged: (name: String) -> Unit,
    onDescriptionChanged: (description: String) -> Unit,
    onUrlChanged: (url: String) -> Unit,
    onImageOriginChanged: (imageUrl: String) -> Unit,
    onPrepTimeChanged: (time: String) -> Unit,
    onCookTimeChanged: (time: String) -> Unit,
    onTotalTimeChanged: (time: String) -> Unit,
    onCategoryChanged: (category: String) -> Unit,
    onYieldChanged: (yield: String) -> Unit,
    onIngredientChanged: (index: Int, ingredient: String) -> Unit,
    onIngredientDeleted: (index: Int) -> Unit,
    onAddIngredient: () -> Unit,
    onToolChanged: (index: Int, tool: String) -> Unit,
    onToolDeleted: (index: Int) -> Unit,
    onAddTool: () -> Unit,
    onInstructionChanged: (index: Int, instruction: String) -> Unit,
    onInstructionDeleted: (index: Int) -> Unit,
    onAddInstruction: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // TODO: Hide bottom navigation

    Scaffold(
        topBar = {
            RecipeEditTopBar(
                title = stringResource(id = title),
                onNavIconClick = onNavIconClick,
                onSaveClick = onSaveClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .verticalScroll(scrollState),
        ) {
            val modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.padding_m))
            val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onBackground,
                cursorColor = MaterialTheme.colors.onBackground,
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.primary,
            )

            Gap(size = dimensionResource(id = R.dimen.padding_m))
            Name(
                recipe = recipe,
                modifier = modifier,
                focusManager = focusManager,
                onNameChanged = onNameChanged,
                textFieldColors = textFieldColors,
            )
            Description(
                recipe = recipe,
                modifier = modifier,
                onDescriptionChanged = onDescriptionChanged,
                textFieldColors = textFieldColors,
            )
            Url(
                recipe = recipe,
                modifier = modifier,
                focusManager = focusManager,
                onUrlChanged = onUrlChanged,
                textFieldColors = textFieldColors,
            )
            ImageOrigin(
                recipe = recipe,
                modifier = modifier,
                focusManager = focusManager,
                onImageOriginChanged = onImageOriginChanged,
                textFieldColors = textFieldColors,
            )
            PrepTime(
                recipe = recipe,
                focusManager = focusManager,
                onPrepTimeChange = onPrepTimeChanged,
                textFieldColors = textFieldColors,
            )
            CookTime(
                recipe = recipe,
                focusManager = focusManager,
                onCookTimeChange = onCookTimeChanged,
                textFieldColors = textFieldColors,
            )
            TotalTime(
                recipe = recipe,
                focusManager = focusManager,
                onTotalTimeChange = onTotalTimeChanged,
                textFieldColors = textFieldColors,
            )
            Category(
                recipe = recipe,
                focusManager = focusManager,
                onCategoryChange = onCategoryChanged,
                textFieldColors = textFieldColors
            )
            Yield(
                recipe = recipe,
                focusManager = focusManager,
                onYieldChanged = onYieldChanged,
                textFieldColors = textFieldColors,
            )
            Ingredients(
                recipe = recipe,
                modifier = modifier,
                focusManager = focusManager,
                onIngredientChanged = onIngredientChanged,
                onIngredientDeleted = onIngredientDeleted,
                onAddIngredient = onAddIngredient,
                textFieldColors = textFieldColors,
            )
            Tools(
                recipe = recipe,
                modifier = modifier,
                focusManager = focusManager,
                onToolChanged = onToolChanged,
                onToolDeleted = onToolDeleted,
                onAddTool = onAddTool,
                textFieldColors = textFieldColors,
            )
            Instructions(
                recipe = recipe,
                modifier = modifier,
                onInstructionChanged = onInstructionChanged,
                onInstructionDeleted = onInstructionDeleted,
                onAddInstruction = onAddInstruction,
                textFieldColors = textFieldColors,
            )
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
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavIconClick) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                )
            }
        },
        actions = {
            IconButton(onClick = onSaveClick) {
                Icon(
                    Icons.Outlined.Save,
                    contentDescription = stringResource(R.string.common_save),
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
    )
}

@Composable
private fun Name(
    recipe: Recipe,
    modifier: Modifier,
    focusManager: FocusManager,
    onNameChanged: (name: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.name,
        onValueChange = onNameChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_name)) },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
        colors = textFieldColors,
    )
}

@Composable
private fun Description(
    recipe: Recipe,
    modifier: Modifier,
    onDescriptionChanged: (description: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.description,
        onValueChange = onDescriptionChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_description)) },
        colors = textFieldColors,
    )
}

@Composable
private fun Url(
    recipe: Recipe,
    modifier: Modifier,
    focusManager: FocusManager,
    onUrlChanged: (url: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.url,
        onValueChange = onUrlChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_url)) },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
        colors = textFieldColors,
    )
}

@Composable
private fun ImageOrigin(
    recipe: Recipe,
    modifier: Modifier,
    focusManager: FocusManager,
    onImageOriginChanged: (imageOrigin: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.imageOrigin,
        onValueChange = onImageOriginChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_image_url)) },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
        colors = textFieldColors,
    )
}

@Composable
private fun PrepTime(
    recipe: Recipe,
    focusManager: FocusManager,
    onPrepTimeChange: (time: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    TimeTextField(
        hours = recipe.prepTime?.toHoursPart()?.toString() ?: "",
        minutes = recipe.prepTime?.toMinutesPart()?.toString() ?: "",
        onHoursChange = {
            val hours = it.ifBlank { "0" }
            val minutes = recipe.prepTime?.toMinutesPart() ?: 0
            onPrepTimeChange.invoke("PT${hours}H${minutes}M0S")
        },
        onMinutesChange = {
            val hours = recipe.prepTime?.toHoursPart()?.toString() ?: 0
            val minutes = it.ifBlank { "0" }
            onPrepTimeChange.invoke("PT${hours}H${minutes}M0S")
        },
        label = R.string.recipe_prep_time,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        colors = textFieldColors,
        hoursKeyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
        ),
        minutesKeyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
        ),
    )
}

@Composable
private fun CookTime(
    recipe: Recipe,
    focusManager: FocusManager,
    onCookTimeChange: (time: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    TimeTextField(
        hours = recipe.cookTime?.toHoursPart()?.toString() ?: "",
        minutes = recipe.cookTime?.toMinutesPart()?.toString() ?: "",
        onHoursChange = {
            val hours = it.ifBlank { "0" }
            val minutes = recipe.cookTime?.toMinutesPart() ?: 0
            onCookTimeChange.invoke("PT${hours}H${minutes}M0S")
        },
        onMinutesChange = {
            val hours = recipe.cookTime?.toHoursPart() ?: 0
            val minutes = it.ifBlank { "0" }
            onCookTimeChange.invoke("PT${hours}H${minutes}M0S")
        },
        label = R.string.recipe_cook_time,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        colors = textFieldColors,
        hoursKeyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
        ),
        minutesKeyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
        ),
    )
}

@Composable
private fun TotalTime(
    recipe: Recipe,
    focusManager: FocusManager,
    onTotalTimeChange: (time: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    TimeTextField(
        hours = recipe.totalTime?.toHoursPart()?.toString() ?: "",
        minutes = recipe.totalTime?.toMinutesPart()?.toString() ?: "",
        onHoursChange = {
            val hours = it.ifBlank { "0" }
            val minutes = recipe.totalTime?.toMinutesPart() ?: 0
            onTotalTimeChange.invoke("PT${hours}H${minutes}M0S")
        },
        onMinutesChange = {
            val hours = recipe.totalTime?.toHoursPart() ?: 0
            val minutes = it.ifBlank { "0" }
            onTotalTimeChange.invoke("PT${hours}H${minutes}M0S")
        },
        label = R.string.recipe_total_time,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        colors = textFieldColors,
        hoursKeyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
        ),
        minutesKeyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Next)
            },
        ),
    )
}

@Composable
private fun Category(
    recipe: Recipe,
    focusManager: FocusManager,
    onCategoryChange: (category: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.category,
        onValueChange = onCategoryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        label = { Text(text = stringResource(id = R.string.recipe_category))},
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
        colors = textFieldColors,
    )
}

@Composable
private fun Yield(
    recipe: Recipe,
    focusManager: FocusManager,
    onYieldChanged: (yield: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.yield.toString(),
        onValueChange = onYieldChanged,
        modifier = Modifier
            .fillMaxWidth(1f / 3f)
            .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        label = { Text(text = stringResource(R.string.recipe_yield)) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
        ),
        singleLine = true,
        colors = textFieldColors,
    )
}

@Composable
private fun Ingredients(
    recipe: Recipe,
    modifier: Modifier,
    focusManager: FocusManager,
    onIngredientChanged: (index: Int, ingredient: String) -> Unit,
    onIngredientDeleted: (index: Int) -> Unit,
    onAddIngredient: () -> Unit,
    textFieldColors: TextFieldColors,
) {
    Text(
        text = stringResource(id = R.string.recipe_ingredients),
        style = MaterialTheme.typography.h6,
    )
    recipe.ingredients.forEachIndexed { index, ingredient ->
        DefaultOutlinedTextField(
            value = ingredient,
            onValueChange = { onIngredientChanged.invoke(index, it) },
            modifier = modifier,
            label = { Text(text = stringResource(id = R.string.recipe_ingredient) + " ${index + 1}") },
            trailingIcon = {
                IconButton(onClick = { onIngredientDeleted.invoke(index) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.recipe_ingredient_delete),
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
            singleLine = true,
            colors = textFieldColors,
        )
    }
    DefaultButton(
        onClick = onAddIngredient,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = NcBlue700,
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.recipe_ingredient_add),
        )
        Text(text = stringResource(R.string.recipe_ingredient_add))
    }
}

@Composable
private fun Tools(
    recipe: Recipe,
    modifier: Modifier,
    focusManager: FocusManager,
    onToolChanged: (index: Int, tool: String) -> Unit,
    onToolDeleted: (index: Int) -> Unit,
    onAddTool: () -> Unit,
    textFieldColors: TextFieldColors,
) {
    Text(
        text = stringResource(id = R.string.recipe_tools),
        style = MaterialTheme.typography.h6,
    )
    recipe.tools.forEachIndexed { index, tool ->
        DefaultOutlinedTextField(
            value = tool,
            onValueChange = { onToolChanged.invoke(index, it) },
            modifier = modifier,
            label = { Text(text = stringResource(id = R.string.recipe_tool) + " ${index + 1}") },
            trailingIcon = {
                IconButton(onClick = { onToolDeleted.invoke(index) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.recipe_tool_delete),
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
            singleLine = true,
            colors = textFieldColors,
        )
    }
    DefaultButton(
        onClick = onAddTool,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = NcBlue700,
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.recipe_tool_add),
        )
        Text(text = stringResource(R.string.recipe_tool_add))
    }
}

@Composable
private fun Instructions(
    recipe: Recipe,
    modifier: Modifier,
    onInstructionChanged: (index: Int, instruction: String) -> Unit,
    onInstructionDeleted: (index: Int) -> Unit,
    onAddInstruction: () -> Unit,
    textFieldColors: TextFieldColors,
) {
    Text(
        text = stringResource(id = R.string.recipe_instructions),
        style = MaterialTheme.typography.h6,
    )
    recipe.instructions.forEachIndexed { index, instruction ->
        DefaultOutlinedTextField(
            value = instruction,
            onValueChange = { onInstructionChanged.invoke(index, it) },
            modifier = modifier,
            label = { Text(text = stringResource(id = R.string.recipe_instruction) + " ${index + 1}") },
            trailingIcon = {
                IconButton(onClick = { onInstructionDeleted.invoke(index) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.recipe_instruction_delete),
                    )
                }
            },
            colors = textFieldColors,
        )
    }
    DefaultButton(
        onClick = onAddInstruction,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = NcBlue700,
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.recipe_instruction_add),
        )
        Text(text = stringResource(R.string.recipe_instruction_add))
    }
}

@Preview
@Composable
private fun CreateEditRecipeFormPreview() {
    val recipe = Recipe(
        id = 1,
        name = "Lorem ipsum",
        description = "Lorem ipsum dolor sit amet",
        url = "https://www.example.com",
        imageOrigin = "https://www.example.com/image.jpg",
        imageUrl = "/apps/cookbook/recipes/1/image?size=full",
        category = "Lorem ipsum",
        keywords = emptyList(),
        yield = 2,
        prepTime = null,
        cookTime = Duration.parse("PT0H35M0S"),
        totalTime = Duration.parse("PT1H50M0S"),
        nutrition = null,
        tools = List(1) {
            "Lorem ipsum"
        },
        ingredients = List(2) {
            "Lorem ipsum"
        },
        instructions = List(1) {
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
        },
        createdAt = "",
        modifiedAt = "",
    )
    NextcloudCookbookTheme {
        CreateEditRecipeForm(
            recipe = recipe,
            title = R.string.recipe_new,
            onNavIconClick = {},
            onNameChanged = {},
            onDescriptionChanged = {},
            onUrlChanged = {},
            onImageOriginChanged = {},
            onPrepTimeChanged = {},
            onCookTimeChanged = {},
            onTotalTimeChanged = {},
            onCategoryChanged = {},
            onYieldChanged = {},
            onIngredientChanged = { _, _ -> },
            onIngredientDeleted = {},
            onAddIngredient = {},
            onToolChanged = { _, _ -> },
            onToolDeleted = {},
            onAddTool = {},
            onInstructionChanged = { _, _ -> },
            onInstructionDeleted = {},
            onAddInstruction = {},
            onSaveClick = {},
        )
    }
}
