package com.mobin.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LandlordReview(
    val id: String = "",
    val reviewerName: String = "",
    val rentalPeriod: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
data class LandlordProfile(
    val id: String = "",
    val name: String = "Mary Ann Dasalo",
    val memberSince: String = "Member since March 2025",
    val avatarUrl: String? = null,
    val isVerified: Boolean = true,
    val overallRating: Double = 0.0,
    val reviewCount: Int = 0,
    val starCounts: Map<Int, Int> = mapOf(
        5 to 0,
        4 to 0,
        3 to 0,
        2 to 0,
        1 to 0,
    ),
    val reviews: List<LandlordReview> = emptyList(),
)

@Serializable
data class LandlordReviewDto(
    val id: String = "",
    @SerialName("landlord_id") val landlordId: String? = null,
    @SerialName("landlord_name") val landlordName: String = "",
    @SerialName("property_id") val propertyId: Long? = null,
    @SerialName("reviewer_id") val reviewerId: String? = null,
    @SerialName("reviewer_name") val reviewerName: String = "",
    @SerialName("rental_period") val rentalPeriod: String? = null,
    val rating: Int = 5,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
) {
    fun toLandlordReview(): LandlordReview = LandlordReview(
        id = id,
        reviewerName = reviewerName.ifBlank { "Renter" },
        rentalPeriod = rentalPeriod ?: "Rented · Recent",
        rating = rating,
        comment = comment ?: "",
        createdAt = System.currentTimeMillis(),
    )
}

@Serializable
data class LandlordReviewInsert(
    @SerialName("landlord_name") val landlordName: String,
    @SerialName("reviewer_name") val reviewerName: String,
    @SerialName("rental_period") val rentalPeriod: String,
    val rating: Int,
    val comment: String,
    @SerialName("property_id") val propertyId: Long? = null,
)
