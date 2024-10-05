package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
    onCaloriesChanged: (calories: String) -> Unit,
    onCarbohydrateContentChanged: (carbohydrateContent: String) -> Unit,
    onCholesterolContentChanged: (cholesterolContent: String) -> Unit,
    onFatContentChanged: (fatContent: String) -> Unit,
    onFiberContentChanged: (fiberContent: String) -> Unit,
    onProteinContentChanged: (proteinContent: String) -> Unit,
    onSaturatedFatContentChanged: (saturatedFatContent: String) -> Unit,
    onServingSizeChanged: (servingSize: String) -> Unit,
    onSodiumContentChanged: (sodiumContent: String) -> Unit,
    onSugarContentChanged: (sugarContent: String) -> Unit,
    onTransFatContentChanged: (transFatContent: String) -> Unit,
    onUnsaturatedFatContentChanged: (unsaturatedFatContent: String) -> Unit,
    onToolChanged: (index: Int, tool: String) -> Unit,
    onToolDeleted: (index: Int) -> Unit,
    onAddTool: () -> Unit,
    onInstructionChanged: (index: Int, instruction: String) -> Unit,
    onInstructionDeleted: (index: Int) -> Unit,
    onAddInstruction: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
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
            modifier =
                Modifier
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
        ) {
            val modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m))
            val textFieldColors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_m)))
            CompositionLocalProvider(LocalTextFieldColors provides textFieldColors) {
                Name(
                    recipe = recipe,
                    modifier = modifier,
                    onNameChanged = onNameChanged,
                )
                Description(
                    recipe = recipe,
                    modifier = modifier,
                    onDescriptionChanged = onDescriptionChanged,
                )
                Url(
                    recipe = recipe,
                    modifier = modifier,
                    onUrlChanged = onUrlChanged,
                )
                ImageOrigin(
                    recipe = recipe,
                    modifier = modifier,
                    onImageOriginChanged = onImageOriginChanged,
                )
                PrepTime(
                    prepTime = prepTime,
                    modifier = modifier,
                    onPrepTimeChange = onPrepTimeChanged,
                )
                CookTime(
                    cookTime = cookTime,
                    modifier = modifier,
                    onCookTimeChange = onCookTimeChanged,
                )
                TotalTime(
                    totalTime = totalTime,
                    modifier = modifier,
                    onTotalTimeChange = onTotalTimeChanged,
                )
                Category(
                    recipe = recipe,
                    categories = categories,
                    onCategoryChange = onCategoryChanged,
                )
                Keywords(
                    recipe = recipe,
                    keywords = keywords,
                    onKeywordsChange = onKeywordsChanged,
                )
                Yield(
                    recipe = recipe,
                    modifier = modifier,
                    onYieldChanged = onYieldChanged,
                )
                Ingredients(
                    recipe = recipe,
                    modifier = modifier,
                    onIngredientChanged = onIngredientChanged,
                    onIngredientDeleted = onIngredientDeleted,
                    onAddIngredient = onAddIngredient,
                )
                Nutritions(
                    recipe = recipe,
                    modifier = modifier,
                    onCaloriesChanged = onCaloriesChanged,
                    onCarbohydrateContentChanged = onCarbohydrateContentChanged,
                    onCholesterolContentChanged = onCholesterolContentChanged,
                    onFatContentChanged = onFatContentChanged,
                    onFiberContentChanged = onFiberContentChanged,
                    onProteinContentChanged = onProteinContentChanged,
                    onSaturatedFatContentChanged = onSaturatedFatContentChanged,
                    onServingSizeChanged = onServingSizeChanged,
                    onSodiumContentChanged = onSodiumContentChanged,
                    onSugarContentChanged = onSugarContentChanged,
                    onTransFatContentChanged = onTransFatContentChanged,
                    onUnsaturatedFatContentChanged = onUnsaturatedFatContentChanged,
                )
                Tools(
                    recipe = recipe,
                    modifier = modifier,
                    onToolChanged = onToolChanged,
                    onToolDeleted = onToolDeleted,
                    onAddTool = onAddTool,
                )
                Instructions(
                    recipe = recipe,
                    modifier = modifier,
                    onInstructionChanged = onInstructionChanged,
                    onInstructionDeleted = onInstructionDeleted,
                    onAddInstruction = onAddInstruction,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeEditTopBar(
    title: String,
    onNavIconClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
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
                    Icons.AutoMirrored.Filled.ArrowBack,
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
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )
}

@Composable
private fun Name(
    recipe: Recipe,
    modifier: Modifier,
    onNameChanged: (name: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    DefaultOutlinedTextField(
        value = recipe.name,
        onValueChange = onNameChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_name)) },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
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
) {
    val textFieldColors = LocalTextFieldColors.current

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
    onUrlChanged: (url: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    DefaultOutlinedTextField(
        value = recipe.url,
        onValueChange = onUrlChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_url)) },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
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
    onImageOriginChanged: (imageOrigin: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    DefaultOutlinedTextField(
        value = recipe.imageOrigin,
        onValueChange = onImageOriginChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_image_url)) },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
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
    onPrepTimeChange: (time: DurationComponents) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    TimeTextField(
        time = prepTime,
        onTimeChange = onPrepTimeChange,
        label = R.string.recipe_prep_time,
        modifier = modifier,
        colors = textFieldColors,
        hoursKeyboardActions =
            KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            ),
        minutesKeyboardActions =
            KeyboardActions(
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
    onCookTimeChange: (time: DurationComponents) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    TimeTextField(
        time = cookTime,
        onTimeChange = onCookTimeChange,
        label = R.string.recipe_cook_time,
        modifier = modifier,
        colors = textFieldColors,
        hoursKeyboardActions =
            KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            ),
        minutesKeyboardActions =
            KeyboardActions(
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
    onTotalTimeChange: (time: DurationComponents) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    TimeTextField(
        time = totalTime,
        onTimeChange = onTotalTimeChange,
        label = R.string.recipe_total_time,
        modifier = modifier,
        colors = textFieldColors,
        hoursKeyboardActions =
            KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                },
            ),
        minutesKeyboardActions =
            KeyboardActions(
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
    onCategoryChange: (category: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    DefaultOutlinedTextField(
        value = recipe.category,
        onValueChange = onCategoryChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = { Text(text = stringResource(id = R.string.recipe_category)) },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
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
                    AssistChip(
                        onClick = { onCategoryChange.invoke(it.name) },
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary,
                                // disabledContainerColor = MaterialTheme.colorScheme.surface,
                                // selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        label = {
                            Text(text = it.name)
                        },
                    )
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
) {
    val state =
        rememberChipTextFieldState(
            chips = recipe.keywords.map { Chip(text = it) },
        )

    LaunchedEffect(state.chips) {
        onKeywordsChange(state.chips.map { it.text }.toSet())
    }

    OutlinedChipTextField(
        state = state,
        onSubmit = ::Chip,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        label = { Text(text = "Keywords") },
        onChipClick = {
            Timber.d("$it clicked")
        },
        colors =
            androidx.compose.material.TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                textColor = MaterialTheme.colorScheme.onSurface,
            ),
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
                        AssistChip(
                            onClick = { state.addChip(Chip(text = it)) },
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            colors =
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary,
                                    // disabledContainerColor = MaterialTheme.colorScheme.surface,
                                    // selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            label = {
                                Text(text = it)
                            },
                        )
                    }
                }
        }
    }
}

@Composable
private fun Yield(
    recipe: Recipe,
    modifier: Modifier,
    onYieldChanged: (yield: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    DefaultOutlinedTextField(
        value = recipe.yield.toString(),
        onValueChange = onYieldChanged,
        modifier = modifier.fillMaxWidth(1f / 3f),
        label = { Text(text = stringResource(R.string.recipe_yield)) },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
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
    onIngredientChanged: (index: Int, ingredient: String) -> Unit,
    onIngredientDeleted: (index: Int) -> Unit,
    onAddIngredient: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_ingredients),
            style = MaterialTheme.typography.headlineSmall,
        )
        recipe.ingredients.forEachIndexed { index, ingredient ->
            DefaultOutlinedTextField(
                value = ingredient,
                onValueChange = { onIngredientChanged.invoke(index, it) },
                modifier =
                    Modifier
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
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(
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
private fun Nutritions(
    recipe: Recipe,
    modifier: Modifier,
    onCaloriesChanged: (calories: String) -> Unit,
    onCarbohydrateContentChanged: (carbohydrateContent: String) -> Unit,
    onCholesterolContentChanged: (cholesterolContent: String) -> Unit,
    onFatContentChanged: (fatContent: String) -> Unit,
    onFiberContentChanged: (fiberContent: String) -> Unit,
    onProteinContentChanged: (proteinContent: String) -> Unit,
    onSaturatedFatContentChanged: (saturatedFatContent: String) -> Unit,
    onServingSizeChanged: (servingSize: String) -> Unit,
    onSodiumContentChanged: (sodiumContent: String) -> Unit,
    onSugarContentChanged: (sugarContent: String) -> Unit,
    onTransFatContentChanged: (transFatContent: String) -> Unit,
    onUnsaturatedFatContentChanged: (unsaturatedFatContent: String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_nutrition),
            style = MaterialTheme.typography.headlineSmall,
        )
        NutritionItem(
            value = recipe.nutrition?.calories,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_calories,
            onChange = onCaloriesChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.carbohydrateContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_carbohydrate,
            onChange = onCarbohydrateContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.cholesterolContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_cholesterol,
            onChange = onCholesterolContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.fatContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_fat_total,
            onChange = onFatContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.fiberContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_fiber,
            onChange = onFiberContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.proteinContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_protein,
            onChange = onProteinContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.saturatedFatContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_saturated_fat,
            onChange = onSaturatedFatContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.servingSize,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_serving_size,
            onChange = onServingSizeChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.sodiumContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_sodium,
            onChange = onSodiumContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.sugarContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_sugar,
            onChange = onSugarContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.transFatContent,
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            label = R.string.recipe_nutrition_trans_fat,
            onChange = onTransFatContentChanged,
        )
        NutritionItem(
            value = recipe.nutrition?.unsaturatedFatContent,
            label = R.string.recipe_nutrition_unsaturated_fat,
            onChange = onUnsaturatedFatContentChanged,
        )
    }
}

@Composable
private fun NutritionItem(
    value: String?,
    modifier: Modifier = Modifier,
    @StringRes label: Int,
    onChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    DefaultOutlinedTextField(
        value = value ?: "",
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = stringResource(id = label)) },
        trailingIcon =
            if (value.isNullOrBlank()) {
                null
            } else {
                {
                    IconButton(onClick = { onChange.invoke("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.common_close),
                        )
                    }
                }
            },
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
        singleLine = true,
        colors = textFieldColors,
    )
}

@Composable
private fun Tools(
    recipe: Recipe,
    modifier: Modifier,
    onToolChanged: (index: Int, tool: String) -> Unit,
    onToolDeleted: (index: Int) -> Unit,
    onAddTool: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldColors = LocalTextFieldColors.current

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_tools),
            style = MaterialTheme.typography.headlineSmall,
        )
        recipe.tools.forEachIndexed { index, tool ->
            DefaultOutlinedTextField(
                value = tool,
                onValueChange = { onToolChanged.invoke(index, it) },
                modifier =
                    Modifier
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
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(
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
) {
    val textFieldColors = LocalTextFieldColors.current

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.recipe_instructions),
            style = MaterialTheme.typography.headlineSmall,
        )
        recipe.instructions.forEachIndexed { index, instruction ->
            DefaultOutlinedTextField(
                value = instruction,
                onValueChange = { onInstructionChanged.invoke(index, it) },
                modifier =
                    Modifier
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
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.recipe_instruction_add),
            )
            Text(text = stringResource(R.string.recipe_instruction_add))
        }
    }
}

private val MockedRecipe =
    Recipe(
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
        tools =
            List(1) {
                "Lorem ipsum"
            },
        ingredients =
            List(2) {
                "Lorem ipsum"
            },
        instructions =
            List(1) {
                """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod
                    |tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At
                    |vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren,
                    |no sea takimata sanctus est Lorem ipsum dolor sit amet.
                """.trimMargin()
            },
        createdAt = "",
        modifiedAt = "",
    )

private val MockedCategories =
    listOf(
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
            onCaloriesChanged = {},
            onCarbohydrateContentChanged = {},
            onCholesterolContentChanged = {},
            onFatContentChanged = {},
            onFiberContentChanged = {},
            onProteinContentChanged = {},
            onSaturatedFatContentChanged = {},
            onServingSizeChanged = {},
            onSodiumContentChanged = {},
            onSugarContentChanged = {},
            onTransFatContentChanged = {},
            onUnsaturatedFatContentChanged = {},
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
        CompositionLocalProvider(LocalTextFieldColors provides TextFieldDefaults.colors()) {
            Column {
                Category(
                    recipe = MockedRecipe,
                    categories = MockedCategories,
                    onCategoryChange = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeywordsPreview() {
    NextcloudCookbookTheme {
        CompositionLocalProvider(LocalTextFieldColors provides TextFieldDefaults.colors()) {
            Column {
                Keywords(
                    recipe = MockedRecipe,
                    keywords = setOf("Lorem Ipsum", "Lorem", "Ipsum"),
                    onKeywordsChange = {},
                )
            }
        }
    }
}

@Preview(widthDp = 375, showBackground = true)
@Composable
private fun YieldPreview() {
    NextcloudCookbookTheme {
        CompositionLocalProvider(LocalTextFieldColors provides TextFieldDefaults.colors()) {
            Yield(
                recipe = MockedRecipe,
                modifier = Modifier,
                onYieldChanged = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientsPreview() {
    NextcloudCookbookTheme {
        CompositionLocalProvider(LocalTextFieldColors provides TextFieldDefaults.colors()) {
            Column {
                Ingredients(
                    recipe = MockedRecipe,
                    modifier = Modifier,
                    onIngredientChanged = { _, _ -> },
                    onIngredientDeleted = {},
                    onAddIngredient = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionsPreview() {
    NextcloudCookbookTheme {
        CompositionLocalProvider(LocalTextFieldColors provides TextFieldDefaults.colors()) {
            Nutritions(
                recipe = MockedRecipe,
                modifier = Modifier,
                onCaloriesChanged = {},
                onCarbohydrateContentChanged = {},
                onCholesterolContentChanged = {},
                onFatContentChanged = {},
                onFiberContentChanged = {},
                onProteinContentChanged = {},
                onSaturatedFatContentChanged = {},
                onServingSizeChanged = {},
                onSodiumContentChanged = {},
                onSugarContentChanged = {},
                onTransFatContentChanged = {},
                onUnsaturatedFatContentChanged = {},
            )
        }
    }
}

val LocalTextFieldColors =
    staticCompositionLocalOf<TextFieldColors> {
        error("CompositionLocal LocalTextFieldColors not present")
    }
