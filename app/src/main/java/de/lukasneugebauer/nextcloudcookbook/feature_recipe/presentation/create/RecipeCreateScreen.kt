package de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.create

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.lukasneugebauer.nextcloudcookbook.core.presentation.components.Loader
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.domain.state.RecipeEditState
import de.lukasneugebauer.nextcloudcookbook.feature_recipe.presentation.components.CreateEditRecipeForm

@Destination
@Composable
fun RecipeCreateScreen(
    navigator: DestinationsNavigator,
    viewModel: RecipeCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is RecipeEditState.Loading -> Loader()
        is RecipeEditState.Success -> {
            val recipe = (uiState as RecipeEditState.Success).recipe

            CreateEditRecipeForm(
                recipe = recipe,
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
        is RecipeEditState.Updated -> {
            /* TODO: Navigate to recipe */
        }
        is RecipeEditState.Error -> {
            val text = (uiState as RecipeEditState.Error).text

            Text(text = "Error: $text")
        }
    }
}