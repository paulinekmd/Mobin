package com.mobin.app.ui.landlord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.LandlordProfile
import com.mobin.app.data.model.LandlordReview
import com.mobin.app.data.repository.LandlordRepository
import com.mobin.app.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LandlordProfileUiState(
    val isLoading: Boolean = false,
    val profile: LandlordProfile? = null,
    val sortedReviews: List<LandlordReview> = emptyList(),
    val selectedSort: String = "Most Recent",
    val showRateSheet: Boolean = false,
    val userRating: Int = 0,
    val reviewText: String = "",
    val isSubmitting: Boolean = false,
)

class LandlordProfileViewModel : ViewModel() {

    private val landlordRepository = LandlordRepository()
    private val propertyRepository = PropertyRepository()

    private val _uiState = MutableStateFlow(LandlordProfileUiState())
    val uiState: StateFlow<LandlordProfileUiState> = _uiState

    private var currentOwnerName: String = "Mary Ann Dasalo"

    fun loadLandlord(propertyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val property = propertyRepository.getPropertyById(propertyId)
            val ownerName = property?.ownerName ?: "Mary Ann Dasalo"
            val ownerJoined = property?.ownerJoined ?: "March 2025"
            val ownerAvatar = property?.ownerAvatarUrl
            currentOwnerName = ownerName

            val profile = landlordRepository.getLandlordProfile(ownerName, ownerJoined, ownerAvatar)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = profile,
                sortedReviews = sortReviews(profile.reviews, _uiState.value.selectedSort),
            )
        }
    }

    fun openRateSheet() {
        _uiState.value = _uiState.value.copy(showRateSheet = true, userRating = 0, reviewText = "")
    }

    fun closeRateSheet() {
        _uiState.value = _uiState.value.copy(showRateSheet = false)
    }

    fun setRating(rating: Int) {
        _uiState.value = _uiState.value.copy(userRating = rating)
    }

    fun setReviewText(text: String) {
        _uiState.value = _uiState.value.copy(reviewText = text)
    }

    fun setSort(sort: String) {
        val currentReviews = _uiState.value.profile?.reviews ?: emptyList()
        _uiState.value = _uiState.value.copy(
            selectedSort = sort,
            sortedReviews = sortReviews(currentReviews, sort),
        )
    }

    private fun sortReviews(reviews: List<LandlordReview>, sort: String): List<LandlordReview> {
        return when (sort) {
            "Highest Rating" -> reviews.sortedByDescending { it.rating }
            "Lowest Rating" -> reviews.sortedBy { it.rating }
            else -> reviews.sortedByDescending { it.createdAt }
        }
    }

    fun submitReview() {
        val rating = _uiState.value.userRating
        if (rating <= 0) return
        val text = _uiState.value.reviewText
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val updated = landlordRepository.addReview(currentOwnerName, rating, text)
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                showRateSheet = false,
                profile = updated,
                sortedReviews = sortReviews(updated.reviews, _uiState.value.selectedSort),
            )
        }
    }
}
