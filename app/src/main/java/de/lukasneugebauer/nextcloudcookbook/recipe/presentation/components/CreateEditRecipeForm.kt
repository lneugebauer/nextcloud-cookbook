package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
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
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.OutlinedChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NcBlue700
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import timber.log.Timber
import java.time.Duration

@Composable
fun CreateEditRecipeForm(
    recipe: Recipe,
    prepTime: DurationComponents,
    cookTime: DurationComponents,
    totalTime: DurationComponents,
    categories: List<Category>,
    keywords: Set<String>,
    @StringRes title: Int,
    onNavIconClick: () -> Unit,
    onNameChanged: (name: String) -> Unit,
    onDescriptionChanged: (description: String) -> Unit,
    onUrlChanged: (url: String) -> Unit,
    onImageOriginChanged: (imageUrl: String) -> Unit,
    onPrepTimeChanged: (time: DurationComponents) -> Unit,
    onCookTimeChanged: (time: DurationComponents) -> Unit,
    onTotalTimeChanged: (time: DurationComponents) -> Unit,
    onCategoryChanged: (category: String) -> Unit,
    onKeywordsChanged: (keywords: Set<String>) -> Unit,
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
                .verticalScroll(scrollState),
        ) {
            val modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
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
                prepTime = prepTime,
                modifier = modifier,
                focusManager = focusManager,
                onPrepTimeChange = onPrepTimeChanged,
                textFieldColors = textFieldColors,
            )
            CookTime(
                cookTime = cookTime,
                modifier = modifier,
                focusManager = focusManager,
                onCookTimeChange = onCookTimeChanged,
                textFieldColors = textFieldColors,
            )
            TotalTime(
                totalTime = totalTime,
                modifier = modifier,
                focusManager = focusManager,
                onTotalTimeChange = onTotalTimeChanged,
                textFieldColors = textFieldColors,
            )
            Category(
                recipe = recipe,
                categories = categories,
                focusManager = focusManager,
                onCategoryChange = onCategoryChanged,
                textFieldColors = textFieldColors,
            )
            Keywords(
                recipe = recipe,
                keywords = keywords,
                onKeywordsChange = onKeywordsChanged,
                textFieldColors = textFieldColors,
            )
            Yield(
                recipe = recipe,
                modifier = modifier,
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
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back),
                )
            }
        },
        actions = {
            IconButton(onClick = onSaveClick) {
                Icon(
                    Icons.Default.Save,
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
    prepTime: DurationComponents,
    modifier: Modifier,
    focusManager: FocusManager,
    onPrepTimeChange: (time: DurationComponents) -> Unit,
    textFieldColors: TextFieldColors,
) {
    TimeTextField(
        time = prepTime,
        onTimeChange = onPrepTimeChange,
        label = R.string.recipe_prep_time,
        modifier = modifier,
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
    cookTime: DurationComponents,
    modifier: Modifier,
    focusManager: FocusManager,
    onCookTimeChange: (time: DurationComponents) -> Unit,
    textFieldColors: TextFieldColors,
) {
    TimeTextField(
        time = cookTime,
        onTimeChange = onCookTimeChange,
        label = R.string.recipe_cook_time,
        modifier = modifier,
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
    totalTime: DurationComponents,
    modifier: Modifier,
    focusManager: FocusManager,
    onTotalTimeChange: (time: DurationComponents) -> Unit,
    textFieldColors: TextFieldColors,
) {
    TimeTextField(
        time = totalTime,
        onTimeChange = onTotalTimeChange,
        label = R.string.recipe_total_time,
        modifier = modifier,
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
    categories: List<Category>,
    focusManager: FocusManager,
    onCategoryChange: (category: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.category,
        onValueChange = onCategoryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = { Text(text = stringResource(id = R.string.recipe_category)) },
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

    if (categories.isEmpty()) {
        Gap(size = dimensionResource(id = R.dimen.padding_m))
    } else {
        LazyRow(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_m)),
            horizontalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.padding_s)),
        ) {
            categories.forEach {
                item {
                    Chip(
                        onClick = { onCategoryChange.invoke(it.name) },
                        border = BorderStroke(2.dp, NcBlue700),
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color.Transparent,
                        ),
                    ) {
                        Text(text = it.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun Keywords(
    recipe: Recipe,
    keywords: Set<String>,
    onKeywordsChange: (keywords: Set<String>) -> Unit,
    textFieldColors: TextFieldColors,
) {
    val state = rememberChipTextFieldState(
        chips = recipe.keywords.map { Chip(text = it) },
    )

    LaunchedEffect(state.chips) {
        onKeywordsChange(state.chips.map { it.text }.toSet())
    }

    OutlinedChipTextField(
        state = state,
        onSubmit = ::Chip,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = { Text(text = "Keywords") },
        onChipClick = {
            Timber.d("$it clicked")
        },
        colors = textFieldColors,
    )

    if (keywords.isEmpty()) {
        Gap(size = dimensionResource(id = R.dimen.padding_m))
    } else {
        LazyRow(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.padding_m)),
            horizontalArrangement = Arrangement.spacedBy(space = dimensionResource(id = R.dimen.padding_s)),
        ) {
            keywords.filter { keyword -> !state.chips.any { it.text == keyword } }
                .forEach {
                    item {
                        Chip(
                            onClick = { state.addChip(Chip(text = it)) },
                            border = BorderStroke(2.dp, NcBlue700),
                            colors = ChipDefaults.chipColors(
                                backgroundColor = Color.Transparent,
                            ),
                        ) {
                            Text(text = it)
                        }
                    }
                }
        }
    }
}

@Composable
private fun Yield(
    recipe: Recipe,
    modifier: Modifier,
    focusManager: FocusManager,
    onYieldChanged: (yield: String) -> Unit,
    textFieldColors: TextFieldColors,
) {
    DefaultOutlinedTextField(
        value = recipe.yield.toString(),
        onValueChange = onYieldChanged,
        modifier = modifier.fillMaxWidth(1f / 3f),
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
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_ingredients),
            style = MaterialTheme.typography.h6,
        )
        recipe.ingredients.forEachIndexed { index, ingredient ->
            DefaultOutlinedTextField(
                value = ingredient,
                onValueChange = { onIngredientChanged.invoke(index, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
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
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_tools),
            style = MaterialTheme.typography.h6,
        )
        recipe.tools.forEachIndexed { index, tool ->
            DefaultOutlinedTextField(
                value = tool,
                onValueChange = { onToolChanged.invoke(index, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
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
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_instructions),
            style = MaterialTheme.typography.h6,
        )
        recipe.instructions.forEachIndexed { index, instruction ->
            DefaultOutlinedTextField(
                value = instruction,
                onValueChange = { onInstructionChanged.invoke(index, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
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
}

private val MockedRecipe = Recipe(
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

private val MockedCategories = listOf(
    Category("Lorem", 3),
    Category("ipsum", 2),
)

@Preview
@Composable
private fun CreateEditRecipeFormPreview() {
    NextcloudCookbookTheme {
        CreateEditRecipeForm(
            recipe = MockedRecipe,
            prepTime = DurationComponents("0", "25"),
            cookTime = DurationComponents("1", "50"),
            totalTime = DurationComponents("2", "15"),
            categories = MockedCategories,
            keywords = emptySet(),
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
            onKeywordsChanged = {},
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

@Preview(showBackground = true)
@Composable
private fun CategoryPreview() {
    NextcloudCookbookTheme {
        Column {
            Category(
                recipe = MockedRecipe,
                categories = MockedCategories,
                focusManager = LocalFocusManager.current,
                onCategoryChange = {},
                textFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeywordsPreview() {
    NextcloudCookbookTheme {
        Column {
            Keywords(
                recipe = MockedRecipe,
                keywords = setOf("Lorem Ipsum", "Lorem", "Ipsum"),
                onKeywordsChange = {},
                textFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
            )
        }
    }
}

@Preview(widthDp = 375, showBackground = true)
@Composable
private fun YieldPreview() {
    NextcloudCookbookTheme {
        Yield(
            recipe = MockedRecipe,
            modifier = Modifier,
            focusManager = LocalFocusManager.current,
            onYieldChanged = {},
            textFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientsPreview() {
    NextcloudCookbookTheme {
        Column {
            Ingredients(
                recipe = MockedRecipe,
                modifier = Modifier,
                focusManager = LocalFocusManager.current,
                onIngredientChanged = { _, _ -> },
                onIngredientDeleted = {},
                onAddIngredient = {},
                textFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
            )
        }
    }
}
