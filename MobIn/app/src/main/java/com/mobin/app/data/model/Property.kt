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
    @SerialName("owner_joined") val ownerJoined: String = "March 2025",
)

/**
 * Data Transfer Object directly mapping the Supabase 'properties' table.
 */
@Serializable
data class PropertyDto(
    val id: Long = 0,
    @SerialName("property_name") val propertyName: String? = null,
    val address: String? = null,
    val rent: Double? = null,
    @SerialName("property_type") val propertyType: String? = null,
    val amenities: List<String>? = null,
    val description: String? = null,
    val city: String? = null,
    val image: String? = null,
    val photos: List<String>? = null,
    @SerialName("landlord_id") val landlordId: String? = null,
    @SerialName("landlord_name") val landlordName: String? = null,
    @SerialName("landlord_email") val landlordEmail: String? = null,
    @SerialName("landlord_phone") val landlordPhone: String? = null,
    val status: String? = null,
    val verification: String? = null,
    @SerialName("verification_status") val verificationStatus: String? = null,
    @SerialName("verification_notes") val verificationNotes: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    fun toProperty(isSaved: Boolean = false): Property {
        val allImages = mutableListOf<String>()
        if (!image.isNullOrBlank()) allImages.add(image)
        photos?.filter { it.isNotBlank() }?.let { allImages.addAll(it) }

        val isVer = verificationStatus?.equals("Verified", ignoreCase = true) == true ||
                verification?.equals("Verified", ignoreCase = true) == true

        val loc = if (!city.isNullOrBlank() && address?.contains(city, ignoreCase = true) != true) {
            "${address ?: "Cebu City"}, $city"
        } else {
            address?.ifBlank { null } ?: "Urgello, Cebu City"
        }

        val type = propertyType?.trim() ?: ""
        val categoryFormatted = when {
            type.contains("dorm", ignoreCase = true) || type.contains("bedspace", ignoreCase = true) -> "Dormitories"
            type.contains("apart", ignoreCase = true) || type.contains("studio", ignoreCase = true) || type.contains("condo", ignoreCase = true) -> "Apartments"
            else -> "Boarding House"
        }

        return Property(
            id = id.toString(),
            title = propertyName?.ifBlank { null } ?: "Accommodation #$id",
            category = categoryFormatted,
            location = loc,
            price = rent ?: 3500.0,
            availableBeds = 3,
            rating = 4.5,
            imageUrl = image?.ifBlank { null } ?: photos?.firstOrNull()?.ifBlank { null },
            images = allImages,
            isSaved = isSaved,
            isVerified = isVer,
            overview = description?.ifBlank { null }
                ?: "Spacious and comfortable accommodation located in a peaceful and accessible area close to universities, convenience stores, and transportation hubs.",
            amenities = amenities?.ifEmpty { null } ?: listOf("WiFi", "CCTV", "Study Area", "Laundry Area"),
            ownerName = landlordName?.ifBlank { null } ?: "Mary Ann Dasalo",
            ownerAvatarUrl = null,
            ownerJoined = "March 2025",
        )
    }
}
