package com.mobin.app.data.repository

import com.mobin.app.data.model.Property
import com.mobin.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PropertyRepository {

    private val supabase = SupabaseClient.client

    // Initial mock properties matching Figma designs
    private val defaultProperties = listOf(
        Property(
            id = "1",
            title = "Casa Urgello",
            category = "Boarding House",
            location = "Urgello, Cebu City",
            price = 3500.0,
            availableBeds = 3,
            rating = 4.5,
            imageUrl = null,
            isVerified = true,
            overview = "Casa Urgello offers a cozy and budget-friendly living space for students and professionals. Conveniently situated in Urgello, Cebu City, near major universities.",
            amenities = listOf("Free WiFi", "CCTV", "Free electricity and water", "Study Area", "Laundry Area"),
            ownerName = "Mary Ann Dasalo",
            ownerJoined = "March 2023",
        ),
        Property(
            id = "2",
            title = "Maria's Boarding",
            category = "Boarding House",
            location = "Urgello, Cebu City",
            price = 3500.0,
            availableBeds = 3,
            rating = 4.5,
            imageUrl = null,
            isVerified = true,
            overview = "Quiet and clean rooms with modern amenities. Perfect for students seeking a productive study environment.",
            amenities = listOf("Free WiFi", "Study Area", "Laundry Area", "CCTV"),
            ownerName = "Maria Santos",
            ownerJoined = "January 2024",
        ),
        Property(
            id = "3",
            title = "Greenview Apartment",
            category = "Apartments",
            location = "Sambag 1, Cebu City",
            price = 3500.0,
            availableBeds = 3,
            rating = 4.5,
            imageUrl = null,
            isVerified = true,
            overview = "A modern apartment unit with ample ventilation, secure gates, and nearby grocery stores.",
            amenities = listOf("Free WiFi", "CCTV", "Kitchen Area", "Private Bathroom"),
            ownerName = "John Green",
            ownerJoined = "November 2022",
        ),
        Property(
            id = "4",
            title = "Student Hub Dorm",
            category = "Dormitories",
            location = "Urgello, Cebu City",
            price = 3500.0,
            availableBeds = 3,
            rating = 4.5,
            imageUrl = null,
            isVerified = true,
            overview = "State-of-the-art dormitory with dedicated study lounges, high-speed fiber internet, and 24/7 security.",
            amenities = listOf("High-speed WiFi", "24/7 Security", "Aircon", "Study Lounge"),
            ownerName = "Student Hub Management",
            ownerJoined = "August 2023",
        ),
        Property(
            id = "5",
            title = "Cityside boarding house",
            category = "Boarding House",
            location = "Sambag 1, Cebu City",
            price = 3500.0,
            availableBeds = 3,
            rating = 4.5,
            imageUrl = null,
            isVerified = true,
            overview = "Affordable city living close to transport terminals and food hubs.",
            amenities = listOf("Free WiFi", "CCTV", "Water Refill"),
            ownerName = "Carlos Reyes",
            ownerJoined = "May 2023",
        ),
    )

    companion object {
        private val _savedPropertyIds = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(setOf("1")) // Start with initial saved item or empty
        val savedPropertyIdsFlow: kotlinx.coroutines.flow.StateFlow<Set<String>> = _savedPropertyIds
    }

    suspend fun getProperties(): Result<List<Property>> = withContext(Dispatchers.IO) {
        runCatching {
            val currentSaved = _savedPropertyIds.value
            // Attempt to query Supabase if a properties/listings table exists
            try {
                val remote = supabase.from("properties")
                    .select()
                    .decodeList<Property>()
                if (remote.isNotEmpty()) {
                    return@runCatching remote.map { it.copy(isSaved = currentSaved.contains(it.id)) }
                }
            } catch (e: Exception) {
                // Table doesn't exist yet on backend, safely fallback to local defaults
            }

            defaultProperties.map {
                it.copy(isSaved = currentSaved.contains(it.id))
            }
        }
    }

    suspend fun getPropertyById(id: String): Property? = withContext(Dispatchers.IO) {
        getProperties().getOrNull()?.find { it.id == id }
    }

    suspend fun getSavedProperties(): Result<List<Property>> = withContext(Dispatchers.IO) {
        runCatching {
            val all = getProperties().getOrNull() ?: emptyList()
            val currentSaved = _savedPropertyIds.value
            all.filter { currentSaved.contains(it.id) }
        }
    }

    suspend fun toggleFavorite(propertyId: String): Boolean = withContext(Dispatchers.IO) {
        val current = _savedPropertyIds.value.toMutableSet()
        val isNowSaved = if (current.contains(propertyId)) {
            current.remove(propertyId)
            false
        } else {
            current.add(propertyId)
            true
        }
        _savedPropertyIds.value = current
        isNowSaved
    }
}
