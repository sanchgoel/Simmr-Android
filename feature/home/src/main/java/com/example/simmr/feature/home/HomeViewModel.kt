package com.example.simmr.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simmr.core.data.RecipeRepository
import com.example.simmr.core.data.SettingsStore
import com.example.simmr.core.model.Recipe
import com.example.simmr.core.model.RecipeOptimization
import kotlinx.coroutines.launch

internal data class HomeUiState(
    val input: String = "",
    val optimizations: Set<RecipeOptimization> = emptySet(),
    val isGenerating: Boolean = false,
    val hasApiKey: Boolean = false,
    val error: String? = null,
) {
    val canGenerate: Boolean get() = input.isNotBlank() && !isGenerating
}

internal class HomeViewModel(
    private val repository: RecipeRepository,
    private val settings: SettingsStore,
) : ViewModel() {
    var state by mutableStateOf(HomeUiState(hasApiKey = settings.hasApiKey()))
        private set

    fun updateInput(value: String) { state = state.copy(input = value, error = null) }
    fun toggle(option: RecipeOptimization) {
        state = state.copy(
            optimizations = state.optimizations.toMutableSet().apply {
                if (!add(option)) remove(option)
            },
        )
    }
    fun refreshApiKey() { state = state.copy(hasApiKey = settings.hasApiKey()) }
    fun generate(onRecipe: (Recipe) -> Unit) {
        if (!state.canGenerate) return
        state = state.copy(isGenerating = true, error = null)
        viewModelScope.launch {
            runCatching { repository.generate(state.input.trim(), state.optimizations) }
                .onSuccess(onRecipe)
                .onFailure { state = state.copy(error = it.message ?: HomeCopy.UNKNOWN_ERROR) }
            state = state.copy(isGenerating = false)
        }
    }
}

internal class HomeViewModelFactory(
    private val repository: RecipeRepository,
    private val settings: SettingsStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository, settings) as T
}

internal object HomeCopy {
    const val UNKNOWN_ERROR = "Something went wrong. Please try again."
    const val TITLE = "Simmr"
    const val SUBTITLE = "Paste a recipe or just type a dish name — let's get cooking."
    const val PLACEHOLDER = "Paste a full recipe, or just type a dish name like \"spicy chicken curry\"."
}
