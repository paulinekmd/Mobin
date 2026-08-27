package com.mobin.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.ui.components.MobInButton
import com.mobin.app.ui.components.MobInTextField
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    isResetMode: Boolean = false,
    targetEmail: String = "",
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                if (isResetMode) onPasswordChanged() else onBack()
            },
            title = {
                Text(
                    text = if (isResetMode) "Password Reset Successful" else "Password Updated",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChestnutBark,
                    ),
                )
            },
            text = {
                Text(
                    text = if (isResetMode) {
                        "Your password has been successfully reset. Please log in with your new password."
                    } else {
                        "Your password has been changed successfully. Would you like to log out now and log in with your new password?"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OliveBronze,
                        lineHeight = 20.sp,
                    ),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.signOut(onPasswordChanged)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenMarigold,
                        contentColor = White,
                    ),
                ) {
                    Text(if (isResetMode) "Go to Login" else "Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = if (!isResetMode) {
                {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onBack()
                        },
                    ) {
                        Text("Stay Logged In", color = AmberSpice, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else null,
            containerColor = White,
            shape = RoundedCornerShape(16.dp),
        )
    }

    ChangePasswordContent(
        uiState = uiState,
        isResetMode = isResetMode,
        currentPassword = currentPassword,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        currentPasswordVisible = currentPasswordVisible,
        newPasswordVisible = newPasswordVisible,
        confirmPasswordVisible = confirmPasswordVisible,
        snackbarHostState = snackbarHostState,
        onCurrentPasswordChange = { currentPassword = it },
        onNewPasswordChange = { newPassword = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onCurrentPasswordVisibleToggle = { currentPasswordVisible = !currentPasswordVisible },
        onNewPasswordVisibleToggle = { newPasswordVisible = !newPasswordVisible },
        onConfirmPasswordVisibleToggle = { confirmPasswordVisible = !confirmPasswordVisible },
        onSave = {
            if (isResetMode) {
                viewModel.resetPassword(targetEmail, newPassword) {
                    showSuccessDialog = true
                }
            } else {
                viewModel.changePassword(currentPassword, newPassword) {
                    showSuccessDialog = true
                }
            }
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChangePasswordContent(
    uiState: ProfileUiState,
    isResetMode: Boolean = false,
    currentPassword: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    currentPasswordVisible: Boolean = false,
    newPasswordVisible: Boolean = false,
    confirmPasswordVisible: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onCurrentPasswordChange: (String) -> Unit = {},
    onNewPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onCurrentPasswordVisibleToggle: () -> Unit = {},
    onNewPasswordVisibleToggle: () -> Unit = {},
    onConfirmPasswordVisibleToggle: () -> Unit = {},
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val passwordMismatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
    val passwordTooShort = newPassword.isNotEmpty() && newPassword.length < 8
    val sameAsCurrent = !isResetMode && currentPassword.isNotEmpty() && newPassword.isNotEmpty() && currentPassword == newPassword

    val isFormValid = (isResetMode || currentPassword.isNotBlank()) &&
            newPassword.length >= 8 &&
            confirmPassword.isNotEmpty() &&
            !passwordMismatch &&
            !sameAsCurrent

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isResetMode) "Reset Password" else "Change Password",
                        style = MaterialTheme.typography.titleLarge,
                        color = ChestnutBark,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AmberSpice)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isResetMode) {
                    "Create a new password with at least 8 characters for your Mob'in account."
                } else {
                    "Enter your current password and choose a new password with at least 8 characters."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OliveBronze,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            // Current Password (Only shown when user is logged in)
            if (!isResetMode) {
                MobInTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = "Current Password *",
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onCurrentPasswordVisibleToggle) {
                            Icon(
                                imageVector = if (currentPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = OliveBronze,
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // New Password
            MobInTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = "New Password *",
                modifier = Modifier.fillMaxWidth(),
                isError = passwordTooShort || sameAsCurrent,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onNewPasswordVisibleToggle) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = OliveBronze,
                        )
                    }
                },
                supportingText = when {
                    sameAsCurrent -> { { Text("New password must be different from current password", color = ErrorRed) } }
                    passwordTooShort -> { { Text("Password must be at least 8 characters", color = ErrorRed) } }
                    else -> null
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            MobInTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm New Password *",
                modifier = Modifier.fillMaxWidth(),
                isError = passwordMismatch,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onConfirmPasswordVisibleToggle) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = OliveBronze,
                        )
                    }
                },
                supportingText = if (passwordMismatch) {
                    { Text("Passwords do not match", color = ErrorRed) }
                } else null,
            )

            Spacer(modifier = Modifier.height(32.dp))

            MobInButton(
                text = if (uiState.isLoading) "Updating..." else if (isResetMode) "Reset Password" else "Update Password",
                onClick = onSave,
                isLoading = uiState.isLoading,
                enabled = isFormValid,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915, name = "Change Password (Logged In)")
@Composable
private fun ChangePasswordPreview() {
    MobInTheme {
        ChangePasswordContent(
            uiState = ProfileUiState(),
            isResetMode = false,
            currentPassword = "oldpassword",
            newPassword = "newpassword123",
            confirmPassword = "newpassword123",
            currentPasswordVisible = false,
            newPasswordVisible = false,
            confirmPasswordVisible = false,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915, name = "Reset Password (After Verification)")
@Composable
private fun ResetPasswordPreview() {
    MobInTheme {
        ChangePasswordContent(
            uiState = ProfileUiState(),
            isResetMode = true,
            newPassword = "newpassword123",
            confirmPassword = "newpassword123",
            newPasswordVisible = false,
            confirmPasswordVisible = false,
        )
    }
}
