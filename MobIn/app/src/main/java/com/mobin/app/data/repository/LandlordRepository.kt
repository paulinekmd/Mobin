package com.mobin.app.data.repository

import com.mobin.app.data.model.LandlordProfile
import com.mobin.app.data.model.LandlordReview
import com.mobin.app.data.model.LandlordReviewDto
import com.mobin.app.data.model.LandlordReviewInsert
import com.mobin.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LandlordRepository {

    private val supabase = SupabaseClient.client

    companion object {
        private val _landlordFlow = MutableStateFlow<Map<String, LandlordProfile>>(emptyMap())
        val landlordFlow: StateFlow<Map<String, LandlordProfile>> = _landlordFlow
    }

    suspend fun getLandlordProfile(
        ownerName: String,
        joinedDate: String,
        knownAvatarUrl: String? = null,
    ): LandlordProfile = withContext(Dispatchers.IO) {
        val cleanName = ownerName.ifBlank { "Mary Ann Dasalo" }
        var realReviews = emptyList<LandlordReview>()

        // 1. Fetch live reviews from Supabase landlord_reviews table
        try {
            val dtos = supabase.from("landlord_reviews")
                .select {
                    filter {
                        eq("landlord_name", cleanName)
                    }
                }
                .decodeList<LandlordReviewDto>()

            realReviews = dtos.map { it.toLandlordReview() }
            android.util.Log.d("LandlordRepository", "Fetched ${realReviews.size} live reviews for '$cleanName'")
        } catch (e: Exception) {
            android.util.Log.d("LandlordRepository", "landlord_reviews table fetch note: ${e.message}")
            // Check in-memory store if previously added
            _landlordFlow.value[cleanName]?.let { return@withContext it }
        }

        // 2. Fetch avatar if not provided
        var avatarUrl = knownAvatarUrl
        if (avatarUrl.isNullOrBlank()) {
            try {
                val profiles = supabase.from("profiles")
                    .select()
                    .decodeList<com.mobin.app.data.model.Profile>()
                avatarUrl = profiles.firstOrNull {
                    it.fullName?.trim()?.equals(cleanName.trim(), ignoreCase = true) == true
                }?.avatarUrl
            } catch (e: Exception) {
                // Ignore
            }
        }

        val starCounts = mutableMapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0)
        realReviews.forEach { r ->
            starCounts[r.rating] = (starCounts[r.rating] ?: 0) + 1
        }

        val reviewCount = realReviews.size
        val avgRating = if (reviewCount > 0) {
            String.format(Locale.US, "%.1f", realReviews.sumOf { it.rating }.toDouble() / reviewCount).toDoubleOrNull() ?: 5.0
        } else {
            0.0
        }

        val profile = LandlordProfile(
            id = cleanName.lowercase().replace(" ", "_"),
            name = cleanName,
            memberSince = if (joinedDate.isNotBlank()) "Member since $joinedDate" else "Member since March 2025",
            avatarUrl = avatarUrl,
            isVerified = true,
            overallRating = avgRating,
            reviewCount = reviewCount,
            starCounts = starCounts,
            reviews = realReviews,
        )

        val updatedMap = _landlordFlow.value.toMutableMap()
        updatedMap[cleanName] = profile
        _landlordFlow.value = updatedMap

        profile
    }

    suspend fun addReview(
        ownerName: String,
        rating: Int,
        comment: String,
        reviewerName: String = "You",
    ): LandlordProfile = withContext(Dispatchers.IO) {
        val cleanName = ownerName.ifBlank { "Mary Ann Dasalo" }
        val currentMonthYear = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())

        // 1. Insert into Supabase landlord_reviews table
        try {
            val insert = LandlordReviewInsert(
                landlordName = cleanName,
                reviewerName = reviewerName,
                rentalPeriod = "Rented · $currentMonthYear",
                rating = rating,
                comment = comment.trim(),
            )
            supabase.from("landlord_reviews").insert(insert)
            android.util.Log.d("LandlordRepository", "Inserted live review into Supabase for '$cleanName'")
        } catch (e: Exception) {
            android.util.Log.e("LandlordRepository", "Remote review insert note: ${e.message}", e)
        }

        // 2. Refresh profile with newly added review
        val current = _landlordFlow.value[cleanName] ?: getLandlordProfile(cleanName, "")
        val newLocalReview = LandlordReview(
            id = "r_${System.currentTimeMillis()}",
            reviewerName = reviewerName,
            rentalPeriod = "Rented · $currentMonthYear",
            rating = rating,
            comment = comment.trim(),
            createdAt = System.currentTimeMillis(),
        )

        val updatedReviews = listOf(newLocalReview) + current.reviews.filter { it.id != newLocalReview.id }
        val newStarCounts = current.starCounts.toMutableMap()
        newStarCounts[rating] = (newStarCounts[rating] ?: 0) + 1

        val newTotalReviews = updatedReviews.size
        val totalStars = updatedReviews.sumOf { it.rating }
        val newAvg = String.format(Locale.US, "%.1f", (totalStars.toDouble() / newTotalReviews.coerceAtLeast(1))).toDoubleOrNull() ?: 5.0

        val updatedProfile = current.copy(
            reviews = updatedReviews,
            starCounts = newStarCounts,
            reviewCount = newTotalReviews,
            overallRating = newAvg,
        )

        val updatedMap = _landlordFlow.value.toMutableMap()
        updatedMap[cleanName] = updatedProfile
        _landlordFlow.value = updatedMap

        updatedProfile
    }
}
