package com.mobin.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val id: String = "",
    val title: String = "",
    val category: String = "Boarding House", // "Boarding House", "Apartments", "Dormitories"
    val location: String = "Urgello, Cebu City",
    val price: Double = 3500.0,
    @SerialName("available_beds") val availableBeds: Int = 3,
    val rating: Double = 4.5,
    @SerialName("image_url") val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("is_saved") val isSaved: Boolean = false,
    @SerialName("is_verified") val isVerified: Boolean = true,
    val overview: String = "Spacious and comfortable accommodation located in a peaceful and accessible area close to universities, convenience stores, and transportation hubs.",
    val amenities: List<String> = listOf("Free WiFi", "CCTV", "Free electricity and water", "Study Area", "Laundry Area"),
    @SerialName("owner_name") val ownerName: String = "Mary Ann Dasalo",
    @SerialName("owner_avatar_url") val ownerAvatarUrl: String? = null,
    @SerialName("owner_joined") val ownerJoined: String = "March 2023",
)
