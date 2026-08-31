package com.mobin.app.data.model

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
    val overallRating: Double = 4.5,
    val reviewCount: Int = 32,
    val starCounts: Map<Int, Int> = mapOf(
        5 to 22,
        4 to 6,
        3 to 2,
        2 to 1,
        1 to 1,
    ),
    val reviews: List<LandlordReview> = emptyList(),
)
