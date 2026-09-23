package com.mobin.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.Property
import com.mobin.app.data.remote.SupabaseClient
import com.mobin.app.data.repository.ProfileRepository
import com.mobin.app.data.repository.PropertyRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String? = null,
    val selectedCategory: String = "Boarding House",
    val allProperties: List<Property> = emptyList(),
    val categoryProperties: List<Property> = emptyList(),
    val availableNow: List<Property> = emptyList(),
    val error: String? = null,
) {
    val categorySectionTitle: String
        get() = when (selectedCategory.lowercase().trim()) {
            "boarding house", "boarding houses" -> "Boarding Houses"
            "apartments", "apartment" -> "Apartments"
            "dormitories", "dormitory" -> "Dormitories"
            else -> selectedCategory
        }
}

class HomeViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()
    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Fetch user's profile for personalized greeting
            try {
                val profileResult = profileRepository.getProfile()
                val fullName = profileResult.getOrNull()?.fullName
                    ?: SupabaseClient.client.auth.currentUserOrNull()?.userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"")

                val firstName = fullName?.trim()?.split(" ")?.firstOrNull()?.ifBlank { null }
                _uiState.value = _uiState.value.copy(userName = firstName)
            } catch (e: Exception) {
                // If unauthenticated or profile fails, fallback gracefully
            }

            // 2. Fetch properties
            propertyRepository.getProperties()
                .onSuccess { properties ->
                    val filteredCategory = filterByCategory(properties, _uiState.value.selectedCategory)
                    val available = properties.filter { it.availableBeds > 0 }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allProperties = properties,
                        categoryProperties = filteredCategory,
                        availableNow = available.ifEmpty { properties },
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

    fun selectCategory(category: String) {
        val filtered = filterByCategory(_uiState.value.allProperties, category)
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            categoryProperties = filtered,
        )
    }

    private fun filterByCategory(all: List<Property>, category: String): List<Property> {
        val cat = category.lowercase().trim()
        return all.filter { p ->
            val pCat = p.category.lowercase().trim()
            when (cat) {
                "boarding house", "boarding houses" -> pCat.contains("boarding")
                "apartments", "apartment" -> pCat.contains("apartment")
                "dormitories", "dormitory" -> pCat.contains("dorm")
                else -> pCat.contains(cat)
            }
        }
    }

    fun toggleFavorite(propertyId: String) {
        viewModelScope.launch {
            val isNowSaved = propertyRepository.toggleFavorite(propertyId)
            _uiState.value = _uiState.value.copy(
                allProperties = _uiState.value.allProperties.map {
                    if (it.id == propertyId) it.copy(isSaved = isNowSaved) else it
                },
                categoryProperties = _uiState.value.categoryProperties.map {
                    if (it.id == propertyId) it.copy(isSaved = isNowSaved) else it
                },
                availableNow = _uiState.value.availableNow.map {
                    if (it.id == propertyId) it.copy(isSaved = isNowSaved) else it
                }
            )
        }
    }
}
