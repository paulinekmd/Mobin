package com.mobin.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.Property
import com.mobin.app.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val selectedCategory: String = "All",
    val recentSearches: List<String> = listOf(
        "Boarding houses in Urgello",
        "Rooms near SWU",
        "Boarding house near SWU",
    ),
    val popularSearches: List<String> = listOf(
        "Under ₱5,000",
        "Dormitory",
        "Available Now",
        "Boarding Houses",
    ),
    val allProperties: List<Property> = emptyList(),
    val filteredResults: List<Property> = emptyList(),
    val isSearching: Boolean = false,
)

class SearchViewModel : ViewModel() {

    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    init {
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            propertyRepository.getProperties().onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    allProperties = list,
                    filteredResults = list,
                )
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        filterResults(newQuery, _uiState.value.selectedCategory)
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterResults(_uiState.value.query, category)
    }

    fun onSelectSearchSuggestion(suggestion: String) {
        _uiState.value = _uiState.value.copy(query = suggestion)
        filterResults(suggestion, _uiState.value.selectedCategory)
    }

    fun removeRecentSearch(term: String) {
        _uiState.value = _uiState.value.copy(
            recentSearches = _uiState.value.recentSearches.filterNot { it.equals(term, ignoreCase = true) }
        )
    }

    fun clearAllRecentSearches() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    private fun filterResults(query: String, category: String) {
        val all = _uiState.value.allProperties
        val q = query.trim().lowercase()

        val results = all.filter { property ->
            val matchesQuery = q.isBlank() ||
                    property.title.lowercase().contains(q) ||
                    property.location.lowercase().contains(q) ||
                    property.category.lowercase().contains(q) ||
                    (q.contains("5000") && property.price <= 5000) ||
                    (q.contains("available") && property.availableBeds > 0)

            val matchesCategory = when (category) {
                "All" -> true
                "Boarding Houses" -> property.category.equals("Boarding House", ignoreCase = true)
                "Apartments" -> property.category.equals("Apartments", ignoreCase = true)
                "Dormitories" -> property.category.equals("Dormitories", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesCategory
        }

        _uiState.value = _uiState.value.copy(
            filteredResults = results,
            isSearching = query.isNotBlank(),
        )
    }

    fun toggleFavorite(propertyId: String) {
        viewModelScope.launch {
            val isSaved = propertyRepository.toggleFavorite(propertyId)
            _uiState.value = _uiState.value.copy(
                allProperties = _uiState.value.allProperties.map {
                    if (it.id == propertyId) it.copy(isSaved = isSaved) else it
                },
                filteredResults = _uiState.value.filteredResults.map {
                    if (it.id == propertyId) it.copy(isSaved = isSaved) else it
                }
            )
        }
    }
}
