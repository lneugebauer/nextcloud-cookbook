package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.edit

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.HideBottomNavigation
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components.CreateEditRecipeForm

@Destination
@Composable
fun RecipeEditScreen(
    navigator: DestinationsNavigator,
    @Suppress("UNUSED_PARAMETER") recipeId: Int,
    resultNavigator: ResultBackNavigator<Boolean>,
    viewModel: RecipeEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HideBottomNavigation()

    when (uiState) {
        is RecipeCreateEditState.Loading -> Loader()
        is RecipeCreateEditState.Success -> {
            val recipe = (uiState as RecipeCreateEditState.Success).recipe
            val prepTime = (uiState as RecipeCreateEditState.Success).prepTime
            val cookTime = (uiState as RecipeCreateEditState.Success).cookTime
            val totalTime = (uiState as RecipeCreateEditState.Success).totalTime
            val categories = (uiState as RecipeCreateEditState.Success).categories
            val keywords = (uiState as RecipeCreateEditState.Success).keywords

            CreateEditRecipeForm(
                recipe = recipe,
                prepTime = prepTime,
                cookTime = cookTime,
                totalTime = totalTime,
                categories = categories,
                keywords = keywords,
                title = R.string.recipe_edit,
                onNavIconClick = { navigator.navigateUp() },
                onNameChanged = { newName ->
                    viewModel.changeName(newName)
                },
                onDescriptionChanged = { newDescription ->
                    viewModel.changeDescription(newDescription)
                },
                onUrlChanged = { newUrl ->
                    viewModel.changeUrl(newUrl)
                },
                onImageOriginChanged = { newImageUrl ->
                    viewModel.changeImageOrigin(newImageUrl)
                },
                onPrepTimeChanged = { newPrepTime ->
                    viewModel.changePrepTime(newPrepTime)
                },
                onCookTimeChanged = { newCookTime ->
                    viewModel.changeCookTime(newCookTime)
                },
                onTotalTimeChanged = { newTotalTime ->
                    viewModel.changeTotalTime(newTotalTime)
                },
                onCategoryChanged = { newCategory ->
                    viewModel.changeCategory(newCategory)
                },
                onKeywordsChanged = { newKeywords ->
                    viewModel.changeKeywords(newKeywords)
                },
                onYieldChanged = { newYield ->
                    viewModel.changeYield(newYield)
                },
                onIngredientChanged = { index, newIngredient ->
                    viewModel.changeIngredient(index, newIngredient)
                },
                onIngredientDeleted = { index ->
                    viewModel.deleteIngredient(index)
                },
                onAddIngredient = {
                    viewModel.addIngredient()
                },
                onSwapIngredient = { fromIndex, toIndex ->
                    viewModel.swapIngredient(fromIndex, toIndex)
                },
                onCaloriesChanged = { newCalories ->
                    viewModel.changeCalories(newCalories)
                },
                onCarbohydrateContentChanged = { newCarbohydrateContent ->
                    viewModel.changeCarbohydrateContent(newCarbohydrateContent)
                },
                onCholesterolContentChanged = { newCholesterolContent ->
                    viewModel.changeCholesterolContent(newCholesterolContent)
                },
                onFatContentChanged = { newFatContent ->
                    viewModel.changeFatContent(newFatContent)
                },
                onFiberContentChanged = { newFiberContent ->
                    viewModel.changeFiberContent(newFiberContent)
                },
                onProteinContentChanged = { newProteinContent ->
                    viewModel.changeProteinContent(newProteinContent)
                },
                onSaturatedFatContentChanged = { newSaturatedFatContent ->
                    viewModel.changeSaturatedFatContent(newSaturatedFatContent)
                },
                onServingSizeChanged = { newServingSize ->
                    viewModel.changeServingSize(newServingSize)
                },
                onSodiumContentChanged = { newSodiumContent ->
                    viewModel.changeSodiumContent(newSodiumContent)
                },
                onSugarContentChanged = { newSugarContent ->
                    viewModel.changeSugarContent(newSugarContent)
                },
                onTransFatContentChanged = { newTransFatContent ->
                    viewModel.changeTransFatContent(newTransFatContent)
                },
                onUnsaturatedFatContentChanged = { newUnsaturatedFatContent ->
                    viewModel.changeUnsaturatedFatContent(newUnsaturatedFatContent)
                },
                onToolChanged = { index, newTool ->
                    viewModel.changeTool(index, newTool)
                },
                onToolDeleted = { index ->
                    viewModel.deleteTool(index)
                },
                onAddTool = {
                    viewModel.addTool()
                },
                onSwapTool = { fromIndex, toIndex ->
                    viewModel.swapTool(fromIndex, toIndex)
                },
                onInstructionChanged = { index, newInstruction ->
                    viewModel.changeInstruction(index, newInstruction)
                },
                onInstructionDeleted = { index ->
                    viewModel.deleteInstruction(index)
                },
                onAddInstruction = {
                    viewModel.addInstruction()
                },
                onSwapInstruction = { fromIndex, toIndex ->
                    viewModel.swapInstruction(fromIndex, toIndex)
                },
                onSaveClick = {
                    viewModel.save()
                },
            )
        }

        is RecipeCreateEditState.Updated -> resultNavigator.navigateBack(true)
        is RecipeCreateEditState.Error -> {
            val text = (uiState as RecipeCreateEditState.Error).error.asString()

            Text(text = "Error: $text")
        }
    }
}
