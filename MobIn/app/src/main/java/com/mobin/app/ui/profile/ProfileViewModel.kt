package com.mobin.app.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.model.Profile
import com.mobin.app.data.model.ProfileUpdate
import com.mobin.app.data.remote.SupabaseClient
import com.mobin.app.data.repository.AuthRepository
import com.mobin.app.data.repository.ProfileRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

class ProfileViewModel : ViewModel() {

    private val profileRepo = ProfileRepository()
    private val authRepo = AuthRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    val currentEmail: String? get() = SupabaseClient.client.auth.currentUserOrNull()?.email
    val currentUserId: String? get() = SupabaseClient.client.auth.currentUserOrNull()?.id

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            profileRepo.getProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = profile,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                    )
                }
        }
    }

    fun updateProfile(fullName: String, phone: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val update = ProfileUpdate(
                fullName = fullName.trim(),
                phone = phone.trim().ifEmpty { null },
            )
            profileRepo.updateProfile(update)
                .onSuccess {
                    // Update in-memory state immediately
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Profile updated successfully!",
                        profile = _uiState.value.profile?.copy(
                            fullName = fullName.trim(),
                            phone = phone.trim().ifEmpty { null },
                        ) ?: Profile(
                            id = currentUserId ?: "",
                            fullName = fullName.trim(),
                            phone = phone.trim().ifEmpty { null },
                        ),
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update profile.",
                    )
                }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    ?: throw Exception("Cannot read image")
                profileRepo.uploadAvatar(userId, bytes)
                    .onSuccess { url ->
                        profileRepo.updateProfile(ProfileUpdate(avatarUrl = url))
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            profile = _uiState.value.profile?.copy(avatarUrl = url),
                            successMessage = "Profile photo updated!",
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to upload photo.",
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error",
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) {
        if (currentPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your current password.")
            return
        }
        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(error = "New password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password updated successfully!",
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    val rawMsg = error.message ?: ""
                    val errorMsg = when {
                        rawMsg.contains("invalid", ignoreCase = true) || rawMsg.contains("credential", ignoreCase = true) ->
                            "Current password is incorrect."
                        rawMsg.contains("same_password", ignoreCase = true) ->
                            "New password must be different from current password."
                        else ->
                            error.message ?: "Failed to update password."
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMsg,
                    )
                }
        }
    }

    fun resetPassword(email: String, newPassword: String, onSuccess: () -> Unit) {
        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.resetPassword(email, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password reset successfully!",
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to reset password.",
                    )
                }
        }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit) {
        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.updatePassword(newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password updated successfully!",
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update password.",
                    )
                }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepo.signOut()
            onDone()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
