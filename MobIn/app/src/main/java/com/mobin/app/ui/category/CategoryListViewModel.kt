package com.mobin.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.Property
import com.mobin.app.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoryFilterState(
    val minPrice: Float = 1500f,
    val maxPrice: Float = 15000f,
    val selectedBeds: Int? = null,
    val selectedRatingOption: String = "Any rating",
    val verifiedOnly: Boolean = false,
)

data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categoryName: String = "Boarding Houses",
    val allCategoryProperties: List<Property> = emptyList(),
    val filteredProperties: List<Property> = emptyList(),
    val filterState: CategoryFilterState = CategoryFilterState(),
    val tempFilterState: CategoryFilterState = CategoryFilterState(),
    val showFilterSheet: Boolean = false,
) {
    val displayTitle: String
        get() = when (categoryName.lowercase().trim()) {
            "boarding house", "boarding houses" -> "Boarding Houses"
            "apartments", "apartment" -> "Apartments"
            "dormitories", "dormitory" -> "Dormitories"
            "available now" -> "Available Now"
            else -> categoryName
        }

    val subtitleCount: String
        get() = "${filteredProperties.size} ${displayTitle.lowercase()} in Urgello Street, Cebu City"
}

class CategoryListViewModel : ViewModel() {

    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow(CategoryListUiState())
    val uiState: StateFlow<CategoryListUiState> = _uiState

    fun loadCategory(categoryName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                categoryName = categoryName,
            )

            propertyRepository.getProperties()
                .onSuccess { all ->
                    val cat = categoryName.lowercase().trim()
                    val matching = all.filter { p ->
                        val pCat = p.category.lowercase().trim()
                        when (cat) {
                            "boarding house", "boarding houses" -> pCat.contains("boarding")
                            "apartments", "apartment" -> pCat.contains("apartment")
                            "dormitories", "dormitory" -> pCat.contains("dorm")
                            "available now" -> p.availableBeds > 0
                            else -> pCat.contains(cat)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allCategoryProperties = matching,
                        filteredProperties = matching,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun openFilterSheet() {
        _uiState.value = _uiState.value.copy(
            showFilterSheet = true,
            tempFilterState = _uiState.value.filterState,
        )
    }

    fun closeFilterSheet() {
        _uiState.value = _uiState.value.copy(showFilterSheet = false)
    }

    fun updateTempPriceRange(min: Float, max: Float) {
        _uiState.value = _uiState.value.copy(
            tempFilterState = _uiState.value.tempFilterState.copy(minPrice = min, maxPrice = max)
        )
    }

    fun updateTempBeds(beds: Int?) {
        val newBeds = if (_uiState.value.tempFilterState.selectedBeds == beds) null else beds
        _uiState.value = _uiState.value.copy(
            tempFilterState = _uiState.value.tempFilterState.copy(selectedBeds = newBeds)
        )
    }

    fun updateTempRating(ratingOption: String) {
        _uiState.value = _uiState.value.copy(
            tempFilterState = _uiState.value.tempFilterState.copy(selectedRatingOption = ratingOption)
        )
    }

    fun updateTempVerified(verified: Boolean) {
        _uiState.value = _uiState.value.copy(
            tempFilterState = _uiState.value.tempFilterState.copy(verifiedOnly = verified)
        )
    }

    fun applyFilters() {
        val filters = _uiState.value.tempFilterState
        val filtered = _uiState.value.allCategoryProperties.filter { property ->
            val matchesPrice = property.price >= filters.minPrice && property.price <= filters.maxPrice
            val matchesBeds = filters.selectedBeds == null || property.availableBeds >= filters.selectedBeds
            val matchesRating = when (filters.selectedRatingOption) {
                "4.5 and Above" -> property.rating >= 4.5
                "4.0 and Above" -> property.rating >= 4.0
                "3.5 and Above" -> property.rating >= 3.5
                else -> true
            }
            val matchesVerified = !filters.verifiedOnly || property.isVerified

            matchesPrice && matchesBeds && matchesRating && matchesVerified
        }

        _uiState.value = _uiState.value.copy(
            filterState = filters,
            filteredProperties = filtered,
            showFilterSheet = false,
        )
    }

    fun resetFilters() {
        val defaultFilters = CategoryFilterState()
        _uiState.value = _uiState.value.copy(
            tempFilterState = defaultFilters,
            filterState = defaultFilters,
            filteredProperties = _uiState.value.allCategoryProperties,
            showFilterSheet = false,
        )
    }

    fun toggleFavorite(propertyId: String) {
        viewModelScope.launch {
            val isNowSaved = propertyRepository.toggleFavorite(propertyId)
            _uiState.value = _uiState.value.copy(
                allCategoryProperties = _uiState.value.allCategoryProperties.map {
                    if (it.id == propertyId) it.copy(isSaved = isNowSaved) else it
                },
                filteredProperties = _uiState.value.filteredProperties.map {
                    if (it.id == propertyId) it.copy(isSaved = isNowSaved) else it
                }
            )
        }
    }
}
