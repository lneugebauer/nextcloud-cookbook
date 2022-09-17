package de.lukasneugebauer.nextcloudcookbook.recipe.presentation.edit

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.recipe.domain.state.RecipeCreateEditState
import de.lukasneugebauer.nextcloudcookbook.recipe.presentation.components.CreateEditRecipeForm

@Destination
@Composable
fun RecipeEditScreen(
    navigator: DestinationsNavigator,
    recipeId: Int,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is RecipeCreateEditState.Loading -> Loader()
        is RecipeCreateEditState.Success -> {
            val recipe = (uiState as RecipeCreateEditState.Success).recipe

            CreateEditRecipeForm(
                recipe = recipe,
                title = R.string.recipe_edit,
                onNavIconClick = { navigator.popBackStack() },
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
                onToolChanged = { index, newTool ->
                    viewModel.changeTool(index, newTool)
                },
                onToolDeleted = { index ->
                    viewModel.deleteTool(index)
                },
                onAddTool = {
                    viewModel.addTool()
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
                onSaveClick = {
                    viewModel.save()
                }
            )
        }
        is RecipeCreateEditState.Updated -> navigator.popBackStack()
        is RecipeCreateEditState.Error -> {
            val text = (uiState as RecipeCreateEditState.Error).error.asString()

            Text(text = "Error: $text")
        }
    }
}
