package com.mobin.app.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.LandlordProfile
import com.mobin.app.data.model.Property
import com.mobin.app.data.repository.LandlordRepository
import com.mobin.app.data.repository.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PropertyDetailsUiState(
    val isLoading: Boolean = false,
    val property: Property? = null,
    val isSaved: Boolean = false,
    val selectedImageIndex: Int = 0,
    val messageSent: Boolean = false,
    val error: String? = null,
)

class PropertyDetailsViewModel : ViewModel() {

    private val propertyRepository = PropertyRepository()
    private val landlordRepository = LandlordRepository()
    private val reportRepository = com.mobin.app.data.repository.ReportRepository()

    private val _uiState = MutableStateFlow(PropertyDetailsUiState())
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState

    init {
        viewModelScope.launch {
            LandlordRepository.landlordFlow.collect { landlordMap ->
                val currentProp = _uiState.value.property ?: return@collect
                val profile = landlordMap[currentProp.ownerName]
                if (profile != null && profile.overallRating != currentProp.rating) {
                    _uiState.value = _uiState.value.copy(
                        property = currentProp.copy(rating = profile.overallRating),
                    )
                }
            }
        }
    }

    fun loadProperty(propertyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prop = propertyRepository.getPropertyById(propertyId)
            if (prop != null) {
                // Fetch real landlord profile to get the live rating
                val landlordProfile = try {
                    landlordRepository.getLandlordProfile(
                        ownerName = prop.ownerName,
                        joinedDate = prop.ownerJoined,
                        knownAvatarUrl = prop.ownerAvatarUrl,
                    )
                } catch (e: Exception) {
                    null
                }

                val realRating = landlordProfile?.overallRating ?: prop.rating
                val updatedProp = prop.copy(rating = realRating)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    property = updatedProp,
                    isSaved = updatedProp.isSaved,
                )
            } else {
                // Default fallback property matching Figma
                val defaultProp = Property(
                    id = propertyId,
                    title = "Casa Urgello",
                    category = "Boarding House",
                    location = "Sambag 2, Cebu City",
                    price = 3500.0,
                    availableBeds = 3,
                    rating = 0.0,
                    isVerified = true,
                    overview = "Spacious and comfortable boarding house located in a peaceful and accessible area close to universities, convenience stores, and transportation hubs.",
                    amenities = listOf("Free WiFi", "CCTV", "Free electricity and water", "Study Area", "Laundry Area"),
                    ownerName = "Mary Ann Dasalo",
                    ownerJoined = "March 2023",
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    property = defaultProp,
                    isSaved = false,
                )
            }
        }
    }

    fun toggleSave() {
        val prop = _uiState.value.property ?: return
        viewModelScope.launch {
            val newSavedState = propertyRepository.toggleFavorite(prop.id)
            _uiState.value = _uiState.value.copy(isSaved = newSavedState)
        }
    }

    fun selectImage(index: Int) {
        _uiState.value = _uiState.value.copy(selectedImageIndex = index)
    }

    fun submitReport(reason: String, details: String?, onResult: (Boolean) -> Unit) {
        val prop = _uiState.value.property ?: return
        viewModelScope.launch {
            val result = reportRepository.submitReport(
                propertyId = prop.id,
                propertyName = prop.title,
                reason = reason,
                details = details,
            )
            onResult(result.isSuccess)
        }
    }
}
