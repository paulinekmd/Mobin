package com.mobin.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.Property
import com.mobin.app.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SavedUiState(
    val isLoading: Boolean = false,
    val savedProperties: List<Property> = emptyList(),
    val error: String? = null,
)

class SavedViewModel : ViewModel() {

    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState

    init {
        observeSavedProperties()
    }

    private fun observeSavedProperties() {
        viewModelScope.launch {
            PropertyRepository.savedPropertyIdsFlow.collectLatest {
                loadSavedList()
            }
        }
    }

    fun loadSavedList() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            propertyRepository.getSavedProperties()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        savedProperties = list,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message,
                    )
                }
        }
    }

    fun toggleFavorite(propertyId: String) {
        viewModelScope.launch {
            propertyRepository.toggleFavorite(propertyId)
        }
    }
}
