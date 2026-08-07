package de.lukasneugebauer.nextcloudcookbook.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lukasneugebauer.nextcloudcookbook.R
import de.lukasneugebauer.nextcloudcookbook.core.data.PreferencesManager
import de.lukasneugebauer.nextcloudcookbook.core.data.api.NcCookbookApiProvider
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearAllStoresUseCase
import de.lukasneugebauer.nextcloudcookbook.core.domain.usecase.ClearPreferencesUseCase
import de.lukasneugebauer.nextcloudcookbook.core.util.Resource
import de.lukasneugebauer.nextcloudcookbook.core.util.UiText
import de.lukasneugebauer.nextcloudcookbook.settings.domain.state.SettingsScreenState
import de.lukasneugebauer.nextcloudcookbook.settings.domain.state.ShoppingListDialogState
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.model.TaskList
import de.lukasneugebauer.nextcloudcookbook.tasks.domain.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val apiProvider: NcCookbookApiProvider,
        private val clearAllStoresUseCase: ClearAllStoresUseCase,
        private val clearPreferencesUseCase: ClearPreferencesUseCase,
        private val preferencesManager: PreferencesManager,
        private val tasksRepository: TasksRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Initial)
        val uiState = _uiState.asStateFlow()

        private val _shoppingListDialogState =
            MutableStateFlow<ShoppingListDialogState>(ShoppingListDialogState.Hidden)
        val shoppingListDialogState = _shoppingListDialogState.asStateFlow()

        init {
            preferencesManager.preferencesFlow
                .map { preferences ->
                    SettingsScreenState.Loaded(
                        isStayAwake = preferencesManager.getStayAwake(),
                        isShowRecipeSyntaxIndicator = preferences.isShowIngredientSyntaxIndicator,
                        recipeImageUploadFolder = preferences.recipeImageUploadFolder,
                        shoppingListName = preferences.shoppingList?.displayName,
                    )
                }.distinctUntilChanged()
                .onEach { loadedState ->
                    _uiState.value = loadedState
                }.launchIn(viewModelScope)
        }

        fun setStayAwake(isStayAwake: Boolean) {
            _uiState.update {
                if (it is SettingsScreenState.Loaded) {
                    preferencesManager.setStayAwake(isStayAwake = isStayAwake)
                    it.copy(isStayAwake = isStayAwake)
                } else {
                    it
                }
            }
        }

        fun setShowRecipeSyntaxIndicator(isShowRecipeSyntaxIndicator: Boolean) {
            viewModelScope.launch {
                preferencesManager.updateShowIngredientSyntaxIndicator(isShowRecipeSyntaxIndicator)
            }
        }

        fun showShoppingListDialog() {
            _shoppingListDialogState.value = ShoppingListDialogState.Loading
            viewModelScope.launch {
                when (val result = tasksRepository.getTaskLists()) {
                    is Resource.Success ->
                        _shoppingListDialogState.value =
                            ShoppingListDialogState.Loaded(
                                taskLists = result.data.orEmpty(),
                                selectedUrl =
                                    preferencesManager.preferencesFlow
                                        .first()
                                        .shoppingList
                                        ?.url,
                            )
                    is Resource.Error ->
                        _shoppingListDialogState.value =
                            ShoppingListDialogState.Error(
                                result.message ?: UiText.StringResource(R.string.settings_shopping_list_error),
                            )
                }
            }
        }

        fun hideShoppingListDialog() {
            _shoppingListDialogState.value = ShoppingListDialogState.Hidden
        }

        fun setShoppingList(taskList: TaskList) {
            viewModelScope.launch {
                preferencesManager.updateShoppingList(taskList)
                _shoppingListDialogState.value = ShoppingListDialogState.Hidden
            }
        }

        fun logout(callback: () -> Unit) {
            viewModelScope.launch {
                apiProvider.resetApi()
                clearAllStoresUseCase.invoke()
                clearPreferencesUseCase.invoke()
                callback.invoke()
            }
        }
    }
