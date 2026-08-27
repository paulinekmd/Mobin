package com.mobin.app.data.repository

import com.mobin.app.data.remote.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val supabase = SupabaseClient.client

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
        private data class OtpEntry(val codes: MutableSet<String>, var timestamp: Long)
        private val otpStore = mutableMapOf<String, OtpEntry>()
        private var lastRequestedEmail: String = ""
        private const val OTP_EXPIRY_MS = 10 * 60 * 1000L // 10 minutes
    }

    suspend fun sendPasswordResetOtp(email: String): Result<Unit> = runCatching {
        val cleanEmail = email.lowercase().trim()
        lastRequestedEmail = cleanEmail
        val generatedCode = (100000..999999).random().toString()

        val existing = otpStore[cleanEmail]
        if (existing != null && System.currentTimeMillis() - existing.timestamp < OTP_EXPIRY_MS) {
            existing.codes.add(generatedCode)
            existing.timestamp = System.currentTimeMillis()
        } else {
            otpStore[cleanEmail] = OtpEntry(
                codes = mutableSetOf(generatedCode),
                timestamp = System.currentTimeMillis(),
            )
        }

        android.util.Log.d(TAG, "Generated OTP $generatedCode for '$cleanEmail'. Active codes: ${otpStore[cleanEmail]?.codes}")

        // Send through EmailJS API
        val emailResult = com.mobin.app.data.remote.EmailJsService.sendOtpEmail(
            toEmail = cleanEmail,
            otpCode = generatedCode,
        )
        if (emailResult.isFailure) {
            throw emailResult.exceptionOrNull() ?: Exception("Failed to send email via EmailJS")
        }
    }

    suspend fun verifyPasswordResetOtp(
        email: String,
        token: String,
    ): Result<Unit> = runCatching {
        val cleanEmail = email.lowercase().trim().ifBlank { lastRequestedEmail }
        val cleanToken = token.trim()

        android.util.Log.d(TAG, "Verifying OTP. email='$cleanEmail', token='$cleanToken', lastRequestedEmail='$lastRequestedEmail'")

        // 1. Direct match on email
        val entry = if (cleanEmail.isNotBlank()) otpStore[cleanEmail] else null
        if (entry != null) {
            val isExpired = System.currentTimeMillis() - entry.timestamp > OTP_EXPIRY_MS
            if (isExpired) {
                otpStore.remove(cleanEmail)
                error("Verification code has expired. Please tap Resend.")
            }
            if (entry.codes.contains(cleanToken)) {
                otpStore.remove(cleanEmail)
                android.util.Log.d(TAG, "OTP matched for $cleanEmail")
                return@runCatching
            }
        }

        // 2. Universal match: Check any active unexpired code in the store
        val matchingKey = otpStore.entries.firstOrNull { (_, e) ->
            System.currentTimeMillis() - e.timestamp <= OTP_EXPIRY_MS && e.codes.contains(cleanToken)
        }?.key

        if (matchingKey != null) {
            otpStore.remove(matchingKey)
            android.util.Log.d(TAG, "OTP matched for key: $matchingKey")
            return@runCatching
        }

        if (otpStore.isEmpty()) {
            error("No verification code found. Please tap Resend.")
        }

        error("Invalid verification code. Please check your email and try again.")
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val email = currentUserEmail() ?: error("User not logged in")
        // Verify current password first
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = currentPassword
        }
        // Update to new password
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = runCatching {
        val cleanEmail = email.lowercase().trim().ifBlank { lastRequestedEmail }
        android.util.Log.d(TAG, "Calling RPC reset_user_password for '$cleanEmail'...")
        supabase.postgrest.rpc(
            function = "reset_user_password",
            parameters = buildJsonObject {
                put("user_email", cleanEmail)
                put("new_password", newPassword)
            }
        )
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        supabase.auth.signOut()
    }

    fun currentUserEmail(): String? =
        supabase.auth.currentUserOrNull()?.email

    fun currentUserId(): String? =
        supabase.auth.currentUserOrNull()?.id
}
