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
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Please fill in all fields.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.signIn(email.trim(), password)
                .onSuccess { onSuccess() }
                .onFailure { error ->
                    android.util.Log.e("AuthViewModel", "signIn failed for $email", error)
                    val rawMsg = error.message ?: ""
                    val causeMsg = error.cause?.message ?: ""
                    val fullError = "$rawMsg $causeMsg"
                    val displayMsg = when {
                        fullError.contains("Invalid login credentials", ignoreCase = true) ->
                            "Incorrect email or password."
                        fullError.contains("Email not confirmed", ignoreCase = true) ->
                            "Please confirm your email before signing in."
                        fullError.contains("Unable to resolve host", ignoreCase = true) ->
                            "DNS lookup failed: Could not resolve Supabase host. Check device internet / DNS."
                        fullError.contains("ConnectException", ignoreCase = true) || fullError.contains("SocketTimeout", ignoreCase = true) ->
                            "Connection timed out. Check network connection."
                        rawMsg.isNotBlank() -> rawMsg
                        else -> "${error::class.simpleName}: ${error.localizedMessage ?: "Unknown error"}"
                    }
                    _uiState.value = AuthUiState(error = displayMsg)
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
