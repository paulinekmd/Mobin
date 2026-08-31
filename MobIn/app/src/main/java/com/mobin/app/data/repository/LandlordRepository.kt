package com.mobin.app.data.repository

import com.mobin.app.data.model.LandlordProfile
import com.mobin.app.data.model.LandlordReview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LandlordRepository {

    companion object {
        private val defaultReviews = mutableListOf(
            LandlordReview(
                id = "r1",
                reviewerName = "John D.",
                rentalPeriod = "Rented · May 2026",
                rating = 5,
                comment = "Very responsive and easy to communicate with. The place was clean and exactly as described. Highly recommended!",
                createdAt = 1779000000000L,
            ),
            LandlordReview(
                id = "r2",
                reviewerName = "April L.",
                rentalPeriod = "Rented · November 2026",
                rating = 4,
                comment = "Great landlord! Always quick to respond and takes good care of the property.",
                createdAt = 1764000000000L,
            ),
            LandlordReview(
                id = "r3",
                reviewerName = "Miguel S.",
                rentalPeriod = "Rented · January 2026",
                rating = 5,
                comment = "Smooth transaction and very accommodating. Thank you!",
                createdAt = 1768000000000L,
            ),
        )

        private val _landlordFlow = MutableStateFlow<Map<String, LandlordProfile>>(emptyMap())
        val landlordFlow: StateFlow<Map<String, LandlordProfile>> = _landlordFlow
    }

    suspend fun getLandlordProfile(ownerName: String, joinedDate: String): LandlordProfile = withContext(Dispatchers.IO) {
        val currentMap = _landlordFlow.value
        if (currentMap.containsKey(ownerName)) {
            return@withContext currentMap[ownerName]!!
        }

        val profile = LandlordProfile(
            id = ownerName.lowercase().replace(" ", "_"),
            name = ownerName.ifBlank { "Mary Ann Dasalo" },
            memberSince = if (joinedDate.isNotBlank()) "Member since $joinedDate" else "Member since March 2025",
            isVerified = true,
            overallRating = 4.5,
            reviewCount = 32,
            starCounts = mapOf(5 to 22, 4 to 6, 3 to 2, 2 to 1, 1 to 1),
            reviews = defaultReviews.toList(),
        )

        val updated = currentMap.toMutableMap()
        updated[ownerName] = profile
        _landlordFlow.value = updated

        profile
    }

    suspend fun addReview(
        ownerName: String,
        rating: Int,
        comment: String,
        reviewerName: String = "You",
    ): LandlordProfile = withContext(Dispatchers.IO) {
        val current = getLandlordProfile(ownerName, "")
        val currentMonthYear = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
        val newReview = LandlordReview(
            id = "r_${System.currentTimeMillis()}",
            reviewerName = reviewerName,
            rentalPeriod = "Rented · $currentMonthYear",
            rating = rating,
            comment = comment.trim(),
            createdAt = System.currentTimeMillis(),
        )

        val updatedReviews = listOf(newReview) + current.reviews
        val newStarCounts = current.starCounts.toMutableMap()
        newStarCounts[rating] = (newStarCounts[rating] ?: 0) + 1

        val newTotalReviews = current.reviewCount + 1
        val totalStars = updatedReviews.sumOf { it.rating }
        val newAvg = String.format(Locale.US, "%.1f", (totalStars.toDouble() / updatedReviews.size.coerceAtLeast(1))).toDoubleOrNull() ?: 4.5

        val updatedProfile = current.copy(
            reviews = updatedReviews,
            starCounts = newStarCounts,
            reviewCount = newTotalReviews,
            overallRating = newAvg,
        )

        val updatedMap = _landlordFlow.value.toMutableMap()
        updatedMap[ownerName] = updatedProfile
        _landlordFlow.value = updatedMap

        updatedProfile
    }
}
