package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.OutlinedChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.category.domain.model.Category
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultButton
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.DefaultOutlinedTextField
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import sh.calvin.reorderable.ReorderableColumn
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
    onSwapIngredient: (fromIndex: Int, toIndex: Int) -> Unit,
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
    onSwapTool: (fromIndex: Int, toIndex: Int) -> Unit,
    onInstructionChanged: (index: Int, instruction: String) -> Unit,
    onInstructionDeleted: (index: Int) -> Unit,
    onAddInstruction: () -> Unit,
    onSwapInstruction: (fromIndex: Int, toIndex: Int) -> Unit,
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

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_m)))
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
                onIngredientChanged = onIngredientChanged,
                onIngredientDeleted = onIngredientDeleted,
                onAddIngredient = onAddIngredient,
                onSwapIngredient = onSwapIngredient,
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
                onToolChanged = onToolChanged,
                onToolDeleted = onToolDeleted,
                onAddTool = onAddTool,
                onSwapTool = onSwapTool,
            )
            Instructions(
                recipe = recipe,
                onInstructionChanged = onInstructionChanged,
                onInstructionDeleted = onInstructionDeleted,
                onAddInstruction = onAddInstruction,
                onSwapInstruction = onSwapInstruction,
            )
        }
    }
}

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
    )
}

@Composable
private fun Name(
    recipe: Recipe,
    modifier: Modifier,
    onNameChanged: (name: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

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
    )
}

@Composable
private fun Description(
    recipe: Recipe,
    modifier: Modifier,
    onDescriptionChanged: (description: String) -> Unit,
) {
    DefaultOutlinedTextField(
        value = recipe.description,
        onValueChange = onDescriptionChanged,
        modifier = modifier,
        label = { Text(text = stringResource(R.string.recipe_description)) },
    )
}

@Composable
private fun Url(
    recipe: Recipe,
    modifier: Modifier,
    onUrlChanged: (url: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

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
    )
}

@Composable
private fun ImageOrigin(
    recipe: Recipe,
    modifier: Modifier,
    onImageOriginChanged: (imageOrigin: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

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
    )
}

@Composable
private fun PrepTime(
    prepTime: DurationComponents,
    modifier: Modifier,
    onPrepTimeChange: (time: DurationComponents) -> Unit,
) {
    TimeInputTextField(
        time = prepTime,
        modifier = modifier,
        label = R.string.recipe_prep_time,
        onTimeChange = onPrepTimeChange,
    )
}

@Composable
private fun CookTime(
    cookTime: DurationComponents,
    modifier: Modifier,
    onCookTimeChange: (time: DurationComponents) -> Unit,
) {
    TimeInputTextField(
        time = cookTime,
        modifier = modifier,
        label = R.string.recipe_cook_time,
        onTimeChange = onCookTimeChange,
    )
}

@Composable
private fun TotalTime(
    totalTime: DurationComponents,
    modifier: Modifier,
    onTotalTimeChange: (time: DurationComponents) -> Unit,
) {
    TimeInputTextField(
        time = totalTime,
        modifier = modifier,
        label = R.string.recipe_total_time,
        onTimeChange = onTotalTimeChange,
    )
}

@Composable
private fun Category(
    recipe: Recipe,
    categories: List<Category>,
    onCategoryChange: (category: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

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
                    SuggestionChip(
                        onClick = { onCategoryChange.invoke(it.name) },
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m)),
        onSubmit = ::Chip,
        label = { Text(text = "Keywords") },
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
                        SuggestionChip(
                            onClick = { state.addChip(Chip(text = it)) },
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
    )
}

@Composable
private fun Ingredients(
    recipe: Recipe,
    onIngredientChanged: (index: Int, ingredient: String) -> Unit,
    onIngredientDeleted: (index: Int) -> Unit,
    onAddIngredient: () -> Unit,
    onSwapIngredient: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Text(
        text = stringResource(id = R.string.recipe_ingredients),
        modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ReorderableColumn(
        list = recipe.ingredients,
        onSettle = { fromIndex, toIndex ->
            onSwapIngredient.invoke(fromIndex, toIndex)
        },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) { index, ingredient, isDragging ->
        key(ingredient.hashCode()) {
            Row(
                modifier =
                    if (isDragging) {
                        Modifier.background(color = Color.Yellow.copy(alpha = 0.5f))
                    } else {
                        Modifier
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    modifier = Modifier.draggableHandle(),
                    onClick = {},
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = stringResource(R.string.common_reorder),
                    )
                }
                DefaultOutlinedTextField(
                    value = ingredient,
                    onValueChange = { onIngredientChanged.invoke(index, it) },
                    modifier =
                        Modifier
                            .padding(end = dimensionResource(R.dimen.padding_m))
                            .fillMaxWidth(),
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
                )
            }
        }
    }
    DefaultButton(
        onClick = onAddIngredient,
        modifier = Modifier.padding(all = dimensionResource(R.dimen.padding_m)),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.recipe_ingredient_add),
        )
        Text(text = stringResource(R.string.recipe_ingredient_add))
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
    )
}

@Composable
private fun Tools(
    recipe: Recipe,
    onToolChanged: (index: Int, tool: String) -> Unit,
    onToolDeleted: (index: Int) -> Unit,
    onAddTool: () -> Unit,
    onSwapTool: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Text(
        text = stringResource(id = R.string.recipe_tools),
        modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ReorderableColumn(
        list = recipe.tools,
        onSettle = { fromIndex, toIndex ->
            onSwapTool.invoke(fromIndex, toIndex)
        },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) { index, tool, isDragging ->
        Row(
            modifier =
                if (isDragging) {
                    Modifier.background(color = Color.Yellow.copy(alpha = 0.5f))
                } else {
                    Modifier
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                modifier = Modifier.draggableHandle(),
                onClick = {},
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.common_reorder),
                )
            }
            DefaultOutlinedTextField(
                value = tool,
                onValueChange = { onToolChanged.invoke(index, it) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = dimensionResource(R.dimen.padding_m)),
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
            )
        }
    }
    DefaultButton(
        onClick = onAddTool,
        modifier = Modifier.padding(all = dimensionResource(R.dimen.padding_m)),
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
    onInstructionChanged: (index: Int, instruction: String) -> Unit,
    onInstructionDeleted: (index: Int) -> Unit,
    onAddInstruction: () -> Unit,
    onSwapInstruction: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    Text(
        text = stringResource(id = R.string.recipe_instructions),
        modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_m)),
        style = MaterialTheme.typography.headlineSmall,
    )
    ReorderableColumn(
        list = recipe.instructions,
        onSettle = { fromIndex, toIndex ->
            onSwapInstruction.invoke(fromIndex, toIndex)
        },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) { index, instruction, isDragging ->
        Row(
            modifier =
                if (isDragging) {
                    Modifier.background(color = Color.Yellow.copy(alpha = 0.5f))
                } else {
                    Modifier
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                modifier = Modifier.draggableHandle(),
                onClick = {},
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.common_reorder),
                )
            }
            DefaultOutlinedTextField(
                value = instruction,
                onValueChange = { onInstructionChanged.invoke(index, it) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = dimensionResource(R.dimen.padding_m)),
                label = { Text(text = stringResource(id = R.string.recipe_instruction) + " ${index + 1}") },
                trailingIcon = {
                    IconButton(onClick = { onInstructionDeleted.invoke(index) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.recipe_instruction_delete),
                        )
                    }
                },
            )
        }
    }
    DefaultButton(
        onClick = onAddInstruction,
        modifier = Modifier.padding(all = dimensionResource(R.dimen.padding_m)),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.recipe_instruction_add),
        )
        Text(text = stringResource(R.string.recipe_instruction_add))
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
            onSwapIngredient = { _, _ -> },
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
            onSwapTool = { _, _ -> },
            onInstructionChanged = { _, _ -> },
            onInstructionDeleted = {},
            onAddInstruction = {},
            onSwapInstruction = { _, _ -> },
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
                onCategoryChange = {},
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
            onYieldChanged = {},
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
                onIngredientChanged = { _, _ -> },
                onIngredientDeleted = {},
                onAddIngredient = {},
                onSwapIngredient = { _, _ -> },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionsPreview() {
    NextcloudCookbookTheme {
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
