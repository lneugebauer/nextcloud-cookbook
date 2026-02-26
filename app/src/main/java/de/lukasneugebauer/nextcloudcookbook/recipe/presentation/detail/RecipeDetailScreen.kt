package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.detail

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.colintheshots.twain.MarkdownText
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.generated.destinations.RecipeEditScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RecipeListWithArgumentsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.MainGraph
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.AuthorizedImage
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Gap
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.pluralResource
import de.lukasneugebauer.nextcloudcookbook.core.presentation.ui.theme.NextcloudCookbookTheme
import de.lukasneugebauer.nextcloudcookbook.core.util.AspectRatio
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.core.util.getActivity
import de.lukasneugebauer.nextcloudcookbook.core.util.notZero
import de.lukasneugebauer.nextcloudcookbook.core.util.openInBrowser
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.CalculatedIngredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Ingredient
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Instruction
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Nutrition
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Recipe
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.Tool
import de.lukasneugebauer.nextcloudcookbook.recipe.util.emptyRecipe
import kotlinx.coroutines.launch
import java.time.Duration

@Destination<MainGraph>(
    deepLinks = [
//        DeepLink(uriPattern = "https://lneugebauer.github.io/recipe/{recipeId}"),
        DeepLink(uriPattern = "nccookbook://lneugebauer.github.io/recipe/{recipeId}"),
    ],
)
@Composable
fun AnimatedVisibilityScope.RecipeDetailScreen(
    navigator: DestinationsNavigator,
    @Suppress("UNUSED_PARAMETER") recipeId: String,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val recipe by remember { derivedStateOf { state.data ?: emptyRecipe() } }
    val errorMessage by remember { derivedStateOf { state.error } }

    if (viewModel.stayAwake) {
        KeepScreenOn()
    }

    if (state.deleted) {
        LaunchedEffect(state) {
            navigator.popBackStack()
        }
    }

    if (state.showFullScreenImage) {
        FullScreenImageViewer(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.name,
            onDismiss = { viewModel.hideFullScreenImage() },
        )
    }

    RecipeDetailLayout(
        recipe = recipe,
        calculatedIngredients = state.calculatedIngredients,
        currentYield = state.currentYield,
        onDecreaseYield = {
            viewModel.decreaseYield()
        },
        onIncreaseYield = {
            viewModel.increaseYield()
        },
        onNavIconClick = {
            if (!navigator.popBackStack()) {
                (context as? Activity)?.finish()
            }
        },
        onDetailImageClick = {
            viewModel.showFullScreenImage()
        },
        onEditClick = {
            if (recipe.isNotEmpty()) {
                navigator.navigate(RecipeEditScreenDestination(recipe.id))
            }
        },
        onDeleteClick = {
            if (recipe.isNotEmpty()) {
                viewModel.deleteRecipe(
                    recipe.id,
                    recipe.category,
                )
            }
        },
        shareText = viewModel.getShareText(),
        onFabClick = {
            if (recipe.isNotEmpty()) {
                navigator.navigate(RecipeEditScreenDestination(recipe.id))
            }
        },
        loading = state.loading,
        errorMessage = errorMessage,
        error = state.error,
        onKeywordClick = {
            navigator.navigate(
                RecipeListWithArgumentsScreenDestination(
                    categoryName = null,
                    keyword = it,
                ),
            )
        },
        onResetYield = {
            viewModel.resetYield()
        },
        isShowIngredientSyntaxIndicator = state.isShowIngredientSyntaxIndicator,
    )
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    val activity = context.getActivity()
    val window = activity?.window
    DisposableEffect(Unit) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun TopBar(
    recipe: Recipe,
    onNavIconClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    shareText: String,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    fun shareRecipe() {
        val sendIntent: Intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_TITLE, recipe.name)
                type = "text/plain"
            }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    TopAppBar(
        title = {
            Text(
                text = recipe.name,
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
            IconButton(onClick = {
                shareRecipe()
            }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.common_share),
                )
            }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.common_more),
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropDownMenuItemOpenSource(context, recipe.url)
                    DropDownMenuItemEdit(onEditClick)
                    DropDownMenuItemDelete(onDeleteClick)
                }
            }
        },
    )
}

@Composable
private fun DropDownMenuItemOpenSource(
    context: Context,
    recipeUrl: String,
) {
    if (recipeUrl.isNotBlank()) {
        DropdownMenuItem(
            onClick = { recipeUrl.toUri().openInBrowser(context) },
            text = { Text(stringResource(id = R.string.recipe_more_menu_share)) },
        )
    }
}

@Composable
private fun DropDownMenuItemEdit(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        text = { Text(text = stringResource(id = R.string.recipe_more_menu_edit)) },
    )
}

@Composable
private fun DropDownMenuItemDelete(onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        text = { Text(text = stringResource(id = R.string.recipe_more_menu_delete)) },
    )
}

@Composable
fun RecipeDetailLayout(
    recipe: Recipe,
    calculatedIngredients: List<CalculatedIngredient>,
    currentYield: Int,
    onDecreaseYield: () -> Unit,
    onIncreaseYield: () -> Unit,
    onNavIconClick: () -> Unit,
    onDetailImageClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    shareText: String,
    onFabClick: () -> Unit,
    loading: Boolean,
    errorMessage: UiText?,
    error: UiText?,
    onKeywordClick: (keyword: String) -> Unit,
    onResetYield: () -> Unit,
    isShowIngredientSyntaxIndicator: Boolean,
) {
    Scaffold(
        topBar = {
            TopBar(
                recipe = recipe,
                onNavIconClick = onNavIconClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                shareText = shareText,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.common_edit),
                )
            }
        },
    ) { innerPadding ->
        if (loading) {
            Loader()
        }
        errorMessage?.let {
            if (!loading) {
                Text(text = it.asString())
            }
        }
        if (recipe.isNotEmpty() && error == null && !loading) {
            Column(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
            ) {
                if (recipe.imageOrigin.isNotBlank() && recipe.imageUrl.isNotBlank()) {
                    Image(recipe.imageUrl, onClick = onDetailImageClick)
                    Name(recipe.name)
                }
                if (recipe.keywords.isNotEmpty()) {
                    Keywords(keywords = recipe.keywords, onClick = onKeywordClick)
                }
                if (recipe.description.isNotBlank()) {
                    Description(description = recipe.description)
                }
                if (recipe.prepTime?.notZero() == true ||
                    recipe.cookTime?.notZero() == true ||
                    recipe.totalTime?.notZero() == true
                ) {
                    Meta(recipe.prepTime, recipe.cookTime, recipe.totalTime)
                }
                if (recipe.category.isNotEmpty()) {
                    Category(category = recipe.category)
                }
                if (recipe.ingredients.isNotEmpty()) {
                    Ingredients(
                        calculatedIngredients.ifEmpty { recipe.ingredients.map { CalculatedIngredient(it.value, it.hasCorrectSyntax) } },
                        onDecreaseYield,
                        onIncreaseYield,
                        onResetYield,
                        currentYield,
                        recipe.yield != currentYield,
                        isShowIngredientSyntaxIndicator,
                    )
                }
                if (recipe.nutrition != null) {
                    Nutrition(recipe.nutrition)
                }
                if (recipe.tools.isNotEmpty()) {
                    Tools(tools = recipe.tools)
                }
                if (recipe.instructions.isNotEmpty()) {
                    Instructions(instructions = recipe.instructions)
                }
                Gap(size = dimensionResource(id = R.dimen.padding_s))
                Gap(size = dimensionResource(id = R.dimen.fab_offset))
            }
        }
    }
}

@Composable
private fun Image(
    imageUrl: String,
    onClick: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val modifier =
        if (isLandscape) {
            Modifier
                .aspectRatio(AspectRatio.CINEMA_SCOPE.ratio)
        } else {
            Modifier
                .aspectRatio(AspectRatio.PHOTO.ratio)
        }

    AuthorizedImage(
        imageUrl = imageUrl,
        contentDescription = null,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.padding_m))
                .clickable(onClick = onClick),
    )
}

@Composable
private fun Name(name: String) {
    Text(
        text = name,
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.titleLarge,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Keywords(
    keywords: List<String>,
    onClick: (keyword: String) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
    ) {
        keywords.forEach {
            AssistChip(
                onClick = { onClick.invoke(it) },
                label = { Text(text = it) },
            )
        }
    }
}

@Composable
private fun Description(description: String) {
    with(LocalDensity.current) {
        MarkdownText(
            markdown = description,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
            fontSize = LocalTextStyle.current.fontSize * this.fontScale,
            style = LocalTextStyle.current.copy(color = LocalContentColor.current),
        )
    }
}

@Composable
private fun Meta(
    prepTime: Duration?,
    cookTime: Duration?,
    totalTime: Duration?,
) {
    Row(
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(
                    top = dimensionResource(id = R.dimen.padding_m),
                    bottom = dimensionResource(id = R.dimen.padding_l),
                ).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (prepTime != null && prepTime != Duration.ZERO) {
            // TODO: 18.11.21 Use knife or countertop icon
            MetaBox(
                icon = Icons.Filled.Timer,
                duration = prepTime.toMinutes(),
                text = R.string.recipe_prep_time,
            )
        }
        if (cookTime != null && cookTime != Duration.ZERO) {
            // TODO: 18.11.21 Use cooking pot, skillet or oven icon
            MetaBox(
                icon = Icons.Filled.Timer,
                duration = cookTime.toMinutes(),
                text = R.string.recipe_cook_time,
            )
        }
        if (totalTime != null && totalTime != Duration.ZERO) {
            MetaBox(
                icon = Icons.Filled.Timer,
                duration = totalTime.toMinutes(),
                text = R.string.recipe_total_time,
            )
        }
    }
}

@Composable
private fun RowScope.MetaBox(
    icon: ImageVector,
    duration: Long,
    @StringRes text: Int,
) {
    Box(modifier = Modifier.weight(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null)
            Text(
                text = stringResource(id = R.string.recipe_duration, duration),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = text),
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle.Default.copy(textAlign = TextAlign.Center),
            )
        }
    }
}

@Composable
private fun Category(category: String) {
    Row(
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = stringResource(id = R.string.recipe_category),
        )
        Text(
            text = category,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun Ingredients(
    ingredients: List<CalculatedIngredient>,
    onDecreaseYield: () -> Unit,
    onIncreaseYield: () -> Unit,
    onResetYield: () -> Unit,
    currentYield: Int,
    showResetButton: Boolean,
    isShowIngredientSyntaxIndicator: Boolean,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.recipe_ingredients),
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_s))
                    .weight(1f),
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = {
            scope.launch {
                clipboard.setClipEntry(
                    ClipEntry(
                        ClipData.newPlainText(
                            "ingredients",
                            buildAnnotatedString {
                                ingredients.joinTo(this, separator = "\n- ", prefix = "- ") { it.ingredient }
                            }.text,
                        ),
                    ),
                )
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                Toast
                    .makeText(
                        context,
                        context.resources.getQuantityString(
                            R.plurals.recipe_ingredients_copied,
                            ingredients.size,
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = stringResource(R.string.recipe_ingredients_copy),
            )
        }
    }
    Row(
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onDecreaseYield,
            enabled = currentYield > 1,
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = stringResource(id = R.string.recipe_servings_decrease),
            )
        }
        Text(
            text = pluralResource(resId = R.plurals.recipe_servings, currentYield, currentYield),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onIncreaseYield) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(id = R.string.recipe_servings_increase),
            )
        }
        if (showResetButton) {
            Button(onClick = onResetYield) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(id = R.string.recipe_servings_reset),
                )
            }
        }
    }
    ingredients.forEach { (ingredient, hasCorrectSyntax) ->
        var checked by rememberSaveable { mutableStateOf(false) }

        Row {
            if (!ingredient.startsWith("##")) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = !checked },
                )
            }
            val textViewId = remember { View.generateViewId() }
            with(LocalDensity.current) {
                var modifier =
                    Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()

                if (ingredient.startsWith("##")) {
                    modifier = modifier.then(Modifier.padding(start = dimensionResource(id = R.dimen.padding_m)))
                }
                if (hasCorrectSyntax) {
                    modifier = modifier.then(Modifier.padding(end = dimensionResource(id = R.dimen.padding_m)))
                }

                MarkdownText(
                    markdown =
                        if (checked) {
                            "~~$ingredient~~"
                        } else {
                            ingredient
                        },
                    modifier = modifier,
                    fontSize = LocalTextStyle.current.fontSize * this.fontScale,
                    style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                    viewId = textViewId,
                )
            }
            if (isShowIngredientSyntaxIndicator && !hasCorrectSyntax) {
                Box(modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(imageVector = Icons.Default.Report, contentDescription = stringResource(R.string.recipe_ingredient_syntax_error))
                }
            }
            LaunchedEffect(Unit) {
                context.getActivity()?.findViewById<TextView>(textViewId)?.setOnLongClickListener {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "ingredient",
                                    ingredient,
                                ),
                            ),
                        )
                    }

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        Toast
                            .makeText(
                                context,
                                context.resources.getQuantityString(
                                    R.plurals.recipe_ingredients_copied,
                                    1,
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }

                    true
                }
            }
        }
    }
    Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.padding_l)))
}

@Composable
private fun NutritionItem(
    @StringRes label: Int,
    value: String?,
) {
    if (value != null) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("${stringResource(label)} ")
                }
                append(value)
            },
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(
                        top = dimensionResource(id = R.dimen.padding_xs),
                        bottom = dimensionResource(id = R.dimen.padding_xs),
                    ),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun Nutrition(nutrition: Nutrition) {
    Text(
        text = stringResource(R.string.recipe_nutrition),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.titleLarge,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_calories,
        value = nutrition.calories,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_carbohydrate,
        value = nutrition.carbohydrateContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_cholesterol,
        value = nutrition.cholesterolContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_fat_total,
        value = nutrition.fatContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_fiber,
        value = nutrition.fiberContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_protein,
        value = nutrition.proteinContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_saturated_fat,
        value = nutrition.saturatedFatContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_serving_size,
        value = nutrition.servingSize,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_sodium,
        value = nutrition.sodiumContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_sugar,
        value = nutrition.sugarContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_trans_fat,
        value = nutrition.transFatContent,
    )
    NutritionItem(
        label = R.string.recipe_nutrition_unsaturated_fat,
        value = nutrition.unsaturatedFatContent,
    )
    val spacingBottom =
        dimensionResource(id = R.dimen.padding_l) - dimensionResource(id = R.dimen.padding_xs)
    Spacer(modifier = Modifier.size(spacingBottom))
}

@Composable
private fun Tools(tools: List<Tool>) {
    Text(
        text = stringResource(R.string.recipe_tools),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_m)),
        style = MaterialTheme.typography.titleLarge,
    )
    with(LocalDensity.current) {
        MarkdownText(
            markdown = tools.joinToString(separator = ", ") { tool -> tool.value },
            modifier =
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_l)),
            fontSize = LocalTextStyle.current.fontSize * this.fontScale,
            style = LocalTextStyle.current.copy(color = LocalContentColor.current),
        )
    }
}

@Composable
private fun Instructions(instructions: List<Instruction>) {
    val enabledStates = remember(instructions) { instructions.map { mutableStateOf(false) } }
    Text(
        text = stringResource(R.string.recipe_instructions),
        modifier =
            Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.padding_m))
                .padding(bottom = dimensionResource(id = R.dimen.padding_s)),
        style = MaterialTheme.typography.titleLarge,
    )
    instructions.forEachIndexed { index, instruction ->
        Row(
            modifier =
                Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_s),
                        end = dimensionResource(id = R.dimen.padding_m),
                        bottom = dimensionResource(id = R.dimen.padding_xs),
                    ),
        ) {
            AssistChip(
                onClick = {
                    enabledStates[index].value = !enabledStates[index].value
                },
                modifier =
                    Modifier
                        .padding(end = dimensionResource(id = R.dimen.padding_xs)),
                enabled = true,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (enabledStates[index].value) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                    ),
                label = {
                    Text(
                        text = "${index + 1}",
                    )
                },
            )
            with(LocalDensity.current) {
                MarkdownText(
                    markdown = instruction.value,
                    modifier =
                        Modifier
                            .padding(bottom = dimensionResource(id = R.dimen.padding_s))
                            .fillMaxWidth(),
                    fontSize = LocalTextStyle.current.fontSize * this.fontScale,
                    style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KeywordsPreview() {
    val keywords =
        List(5) {
            "Keyword ${it + 1}"
        }
    NextcloudCookbookTheme {
        Keywords(keywords = keywords, onClick = {})
    }
}

@PreviewLightDark
@PreviewFontScale
@Composable
private fun DescriptionPreview() {
    NextcloudCookbookTheme {
        Surface {
            Description(
                description =
                    """
                    Some description
                    
                    _with_ rich text
                    
                    - a
                    - list
                    
                    and [some links](http://example.com).
                    """.trimIndent(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientsPreview() {
    val loremIpsum = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed"
    val loremIpsumList = loremIpsum.split(" ")
    val ingredients =
        MutableList(3) {
            val stringArray = loremIpsumList.take((it + 1) * (it + 1))
            CalculatedIngredient(ingredient = stringArray.joinToString(" "), hasCorrectSyntax = it != 1)
        }
    ingredients.add(0, CalculatedIngredient("## A headline", true))
    NextcloudCookbookTheme {
        Column {
            Ingredients(
                ingredients = ingredients,
                onDecreaseYield = {},
                onIncreaseYield = {},
                onResetYield = {},
                currentYield = 2,
                showResetButton = false,
                isShowIngredientSyntaxIndicator = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InstructionsPreview() {
    val loremIpsum =
        """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam
                |nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                |voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd
                |gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum
                |dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
                |ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam
                |et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
                |sanctus est Lorem ipsum dolor sit amet.
        """.trimMargin()
    val loremIpsumList = loremIpsum.split(" ")
    val instructions =
        List(5) {
            val stringArray = loremIpsumList.take((it + 1) * (it + 1))
            Instruction(id = it, value = stringArray.joinToString(" "))
        }
    NextcloudCookbookTheme {
        Column {
            Instructions(instructions = instructions)
        }
    }
}

@PreviewScreenSizes
@Composable
private fun RecipeDetailLayoutPreview() {
    val recipe =
        emptyRecipe().copy(
            id = "1",
            name = "Lorem ipsum",
            description = "Lorem ipsum dolor sit amet",
            url = "https://www.example.com",
            imageUrl = "aurelien_lemasson_theobald_x00czbt4dfk_unsplash",
            category = "Lorem ipsum",
            yield = 2,
            cookTime = Duration.parse("PT0H35M0S"),
            totalTime = Duration.parse("PT1H50M0S"),
            tools =
                List(1) {
                    Tool(id = it, value = "Lorem ipsum")
                },
            ingredients =
                List(2) {
                    Ingredient(id = it, value = "Lorem ipsum", hasCorrectSyntax = it != 1)
                },
            instructions =
                List(1) {
                    Instruction(
                        id = it,
                        value =
                            """Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                        |eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                        |voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
                        |clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit
                        |amet.
                            """.trimMargin(),
                    )
                },
        )
    NextcloudCookbookTheme {
        RecipeDetailLayout(
            recipe = recipe,
            calculatedIngredients = emptyList(),
            currentYield = 2,
            onDecreaseYield = {},
            onIncreaseYield = {},
            onNavIconClick = {},
            onDetailImageClick = {},
            onEditClick = {},
            onDeleteClick = {},
            shareText = "",
            onFabClick = {},
            loading = false,
            errorMessage = null,
            error = null,
            onKeywordClick = {},
            onResetYield = {},
            isShowIngredientSyntaxIndicator = true,
        )
    }
}
