package com.mobin.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobin.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            _uiState.value = AuthUiState(error = "Please fill in all fields.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.signIn(cleanEmail, cleanPassword)
                .onSuccess {
                    _uiState.value = AuthUiState(isSuccess = true)
                    onSuccess()
                }
                .onFailure { error ->
                    android.util.Log.e("AuthViewModel", "signIn failed for '$cleanEmail'", error)
                    val raw = error.message ?: ""
                    val displayError = when {
                        raw.contains("Invalid login credentials", ignoreCase = true) ||
                        raw.contains("invalid_credentials", ignoreCase = true) ->
                            "Incorrect email or password."
                        raw.contains("Email not confirmed", ignoreCase = true) ||
                        raw.contains("email_not_confirmed", ignoreCase = true) ->
                            "Please confirm your email address before logging in."
                        raw.contains("Unable to resolve host", ignoreCase = true) ||
                        raw.contains("ConnectException", ignoreCase = true) ||
                        raw.contains("No address associated with hostname", ignoreCase = true) ||
                        raw.contains("timeout", ignoreCase = true) ->
                            "Cannot reach server. Please check your internet connection or emulator Wi-Fi."
                        else ->
                            raw.ifBlank { "Incorrect email or password." }
                    }
                    _uiState.value = AuthUiState(error = displayError)
                }
        }
    }

    fun signUp(
        fullName: String,
        email: String,
        phone: String?,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
    ) {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone?.trim()
        val cleanPassword = password.trim()
        val cleanConfirm = confirmPassword.trim()

        if (cleanName.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your full name.")
            return
        }
        if (cleanEmail.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your email address.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = AuthUiState(error = "Please enter a valid email address.")
            return
        }
        if (cleanPassword.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter a password.")
            return
        }
        if (cleanPassword.length < 6) {
            _uiState.value = AuthUiState(error = "Password must be at least 6 characters.")
            return
        }
        if (cleanPassword != cleanConfirm) {
            _uiState.value = AuthUiState(error = "Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.signUp(
                email = cleanEmail,
                password = cleanPassword,
                fullName = cleanName,
                phone = cleanPhone,
            ).onSuccess {
                _uiState.value = AuthUiState(isSuccess = true)
                onSuccess()
            }.onFailure { error ->
                android.util.Log.e("AuthViewModel", "signUp failed for '$cleanEmail'", error)
                val raw = error.message ?: ""
                val displayError = when {
                    raw.contains("User already registered", ignoreCase = true) ||
                    raw.contains("already registered", ignoreCase = true) ||
                    raw.contains("user_already_exists", ignoreCase = true) ->
                        "An account with this email already exists."
                    raw.contains("rate limit", ignoreCase = true) ||
                    raw.contains("over_email_send_rate_limit", ignoreCase = true) ->
                        "Too many attempts. Please try again in a few minutes."
                    raw.contains("Unable to resolve host", ignoreCase = true) ||
                    raw.contains("ConnectException", ignoreCase = true) ||
                    raw.contains("No address associated with hostname", ignoreCase = true) ||
                    raw.contains("timeout", ignoreCase = true) ->
                        "Cannot reach server. Please check your internet connection."
                    else ->
                        raw.ifBlank { "Failed to create account. Please try again." }
                }
                _uiState.value = AuthUiState(error = displayError)
            }
        }
    }

    fun sendPasswordResetOtp(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your email address.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.sendPasswordResetOtp(email.trim())
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    android.util.Log.e("AuthViewModel", "sendPasswordResetOtp failed", error)
                    val msg = error.message ?: "Failed to send OTP. Check your email and try again."
                    _uiState.value = AuthUiState(error = msg)
                }
        }
    }

    fun verifyOtp(email: String, otp: String, onSuccess: () -> Unit) {
        if (otp.length < 6) {
            _uiState.value = AuthUiState(error = "Please enter the complete 6-digit code.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.verifyPasswordResetOtp(email, otp)
                .onSuccess {
                    _uiState.value = AuthUiState(isSuccess = true)
                    onSuccess()
                }
                .onFailure { error ->
                    android.util.Log.e("AuthViewModel", "verifyOtp failed for $email", error)
                    _uiState.value = AuthUiState(error = error.message ?: "Invalid or expired code. Please try again.")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
