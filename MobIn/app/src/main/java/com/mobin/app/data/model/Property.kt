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
    @SerialName("available_from") val availableFrom: String = "Available now",
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
    @SerialName("available_from") val availableFrom: String? = null,
    @SerialName("available_date") val availableDate: String? = null,
    val availability: String? = null,
) {
    fun toProperty(
        isSaved: Boolean = false,
        avatarUrl: String? = null,
        landlordRating: Double? = null,
    ): Property {
        val allImages = mutableListOf<String>()
        if (!image.isNullOrBlank()) allImages.add(image)
        photos?.filter { it.isNotBlank() }?.let { allImages.addAll(it) }
        val distinctImages = allImages.distinct()

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

        val dateText = formatAvailableDate(availableFrom)
            ?: formatAvailableDate(availableDate)
            ?: availability?.ifBlank { null }
            ?: formatAvailableDate(createdAt)
            ?: "Available now"

        val parsedBeds = description?.let { desc ->
            Regex("(\\d+)\\s*(?:bed|person|pax|slot|room)", RegexOption.IGNORE_CASE).find(desc)?.groupValues?.get(1)?.toIntOrNull()
        } ?: propertyName?.let { name ->
            Regex("(\\d+)\\s*(?:bed|person|pax|slot|room)", RegexOption.IGNORE_CASE).find(name)?.groupValues?.get(1)?.toIntOrNull()
        } ?: when {
            type.contains("studio", ignoreCase = true) || type.contains("single", ignoreCase = true) -> 1
            type.contains("apart", ignoreCase = true) -> 2
            else -> when ((id % 4L).toInt()) {
                0 -> 4
                1 -> 1
                2 -> 2
                else -> 3
            }
        }

        return Property(
            id = id.toString(),
            title = propertyName?.ifBlank { null } ?: "Accommodation #$id",
            category = categoryFormatted,
            location = loc,
            price = rent ?: 3500.0,
            availableBeds = parsedBeds,
            rating = landlordRating ?: 0.0,
            imageUrl = distinctImages.firstOrNull() ?: image?.ifBlank { null },
            images = distinctImages,
            isSaved = isSaved,
            isVerified = isVer,
            overview = description?.ifBlank { null }
                ?: "Spacious and comfortable accommodation located in a peaceful and accessible area close to universities, convenience stores, and transportation hubs.",
            amenities = amenities?.ifEmpty { null } ?: listOf("WiFi", "CCTV", "Study Area", "Laundry Area"),
            ownerName = landlordName?.ifBlank { null } ?: "Mary Ann Dasalo",
            ownerAvatarUrl = avatarUrl,
            ownerJoined = "March 2025",
            availableFrom = dateText,
        )
    }

    private fun formatAvailableDate(dateStr: String?): String? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val cleanStr = dateStr.trim().take(10)
            val parts = cleanStr.split("-")
            if (parts.size == 3 && parts[0].length == 4) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                val monthName = when (month) {
                    1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
                    7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
                    else -> ""
                }
                if (monthName.isNotEmpty()) "$monthName $day, $year" else dateStr
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}
