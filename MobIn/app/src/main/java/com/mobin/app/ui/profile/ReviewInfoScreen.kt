package com.mobin.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.data.model.Profile
import com.mobin.app.ui.components.MobInButton
import com.mobin.app.ui.components.MobInTextField
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewInfoScreen(onBack: () -> Unit, viewModel: ProfileViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val profile  = uiState.profile

    // Editing state — initialised from profile, reset on Cancel
    var fullName  by remember(profile) { mutableStateOf(profile?.fullName  ?: "") }
    var phone     by remember(profile) { mutableStateOf(profile?.phone     ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let          { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    ReviewInfoContent(
        uiState       = uiState,
        fullName      = fullName,
        phone         = phone,
        currentEmail  = viewModel.currentEmail,
        snackbarHostState = snackbarHostState,
        onFullNameChange = { fullName = it },
        onPhoneChange    = { phone    = it },
        onSave = { viewModel.updateProfile(fullName, phone, onBack) },
        onCancel = {
            // Restore original values then go back
            fullName = profile?.fullName ?: ""
            phone    = profile?.phone    ?: ""
            onBack()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewInfoContent(
    uiState: ProfileUiState,
    fullName: String,
    phone: String,
    currentEmail: String?,
    snackbarHostState: SnackbarHostState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    // ── Validation ────────────────────────────────────────────────────────────
    val fullNameError: String? = when {
        fullName.isBlank()           -> "Full name is required."
        fullName.trim().length < 3   -> "Full name must be at least 3 characters."
        else                          -> null
    }
    val isFormValid = fullNameError == null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = White,
        topBar = {
            TopAppBar(
                title = { Text("Personal Information", style = MaterialTheme.typography.titleLarge, color = ChestnutBark) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel", tint = AmberSpice)
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
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Update your personal information below.",
                style = MaterialTheme.typography.bodyMedium,
                color = OliveBronze,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            // Full name — required, min 3 chars
            MobInTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = "Full Name *",
                modifier = Modifier.fillMaxWidth(),
                isError = fullName.isNotEmpty() && fullNameError != null,
                supportingText = if (fullName.isNotEmpty() && fullNameError != null) {
                    { Text(fullNameError, color = ErrorRed, style = MaterialTheme.typography.labelSmall) }
                } else null,
            )

            Spacer(Modifier.height(16.dp))

            // Email — read-only
            MobInTextField(
                value = currentEmail ?: "",
                onValueChange = {},
                label = "Email (cannot be changed)",
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
            )

            Spacer(Modifier.height(16.dp))

            // Phone — optional
            MobInTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Phone Number (optional)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            Spacer(Modifier.height(32.dp))

            // Save (disabled while loading or form invalid)
            MobInButton(
                text = if (uiState.isLoading) "Saving..." else "Save Changes",
                onClick = onSave,
                isLoading = uiState.isLoading,
                enabled = isFormValid,
            )

            Spacer(Modifier.height(12.dp))

            // Cancel
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OliveBronze),
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ReviewInfoPreview() {
    MobInTheme {
        ReviewInfoContent(
            uiState = ProfileUiState(profile = Profile(id = "1", fullName = "Angel Ann Alfeche", phone = "+63 912 345 6789")),
            fullName = "Angel Ann Alfeche",
            phone    = "+63 912 345 6789",
            currentEmail = "angel@example.com",
            snackbarHostState = SnackbarHostState(),
            onFullNameChange = {}, onPhoneChange = {},
            onSave = {}, onCancel = {},
        )
    }
}
