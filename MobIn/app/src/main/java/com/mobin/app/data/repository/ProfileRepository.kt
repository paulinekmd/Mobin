package com.mobin.app.data.repository

import com.mobin.app.data.model.Profile
import com.mobin.app.data.model.ProfileUpdate
import com.mobin.app.data.remote.SupabaseClient
import com.mobin.app.util.Constants
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType

class ProfileRepository {

    private val supabase = SupabaseClient.client

    suspend fun getProfile(): Result<Profile> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not logged in")
        supabase.from(Constants.PROFILES_TABLE)
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingle<Profile>()
    }

    suspend fun updateProfile(update: ProfileUpdate): Result<Unit> = runCatching {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not logged in")
        supabase.from(Constants.PROFILES_TABLE)
            .update(update) {
                filter { eq("id", userId) }
            }
    }

    /**
     * Uploads avatar bytes to Cloudinary (with Supabase Storage fallback) and returns the public URL.
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> = runCatching {
        val cloudinaryResult = com.mobin.app.data.remote.CloudinaryUploader.uploadImage(
            imageBytes = imageBytes,
            fileName = "avatar_$userId.jpg",
            folder = "avatars",
        )
        if (cloudinaryResult.isSuccess) {
            return@runCatching cloudinaryResult.getOrThrow()
        }

        val path = "$userId/avatar.jpg"
        supabase.storage
            .from(Constants.AVATARS_BUCKET)
            .upload(path = path, data = imageBytes) {
                contentType = ContentType.Image.JPEG
                upsert = true
            }
        val baseUrl = supabase.storage
            .from(Constants.AVATARS_BUCKET)
            .publicUrl(path)
        "$baseUrl?t=${System.currentTimeMillis()}"
    }
}
