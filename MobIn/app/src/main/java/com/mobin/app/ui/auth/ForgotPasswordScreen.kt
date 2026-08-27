package com.mobin.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onOtpSent: (email: String) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    ForgotPasswordContent(
        uiState = uiState,
        email = email,
        snackbarHostState = snackbarHostState,
        onEmailChange = { email = it },
        onSend = { viewModel.sendPasswordResetOtp(email) { onOtpSent(email.trim()) } },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordContent(
    uiState: AuthUiState,
    email: String,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
) {
    val tiltedOmbreBrush = Brush.linearGradient(
        0.0f to Color(0xFFF9D490),
        0.22f to Color(0xFFFAE0B0),
        0.50f to Color(0xFFFFFFFF),
        0.75f to Color(0xFFFFF7EA),
        1.0f to Color(0xFFFDE4B7),
        start = Offset(0f, 0f),
        end = Offset(1200f, 2200f),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AmberSpice,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tiltedOmbreBrush)
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = ComfortaaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Enter your email address and we'll send you a 6-digit verification code to reset your password.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF555555),
                        lineHeight = 22.sp,
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Email Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email Address",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF222222),
                            fontSize = 13.5.sp,
                        ),
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = {
                            Text(
                                text = "Enter your email address",
                                color = Color(0xFFAFAFAF),
                                fontSize = 13.5.sp,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenMarigold,
                            unfocusedBorderColor = Color(0xFFDCC3B2),
                            focusedContainerColor = White.copy(alpha = 0.95f),
                            unfocusedContainerColor = White.copy(alpha = 0.95f),
                            focusedTextColor = Color(0xFF222222),
                            unfocusedTextColor = Color(0xFF222222),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Send Verification Button
                Button(
                    onClick = onSend,
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenMarigold,
                        contentColor = White,
                        disabledContainerColor = GoldenMarigold.copy(alpha = 0.6f),
                        disabledContentColor = White.copy(alpha = 0.7f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(50), ambientColor = Color(0x33FBB81F), spotColor = Color(0x66FBB81F)),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Text(
                            text = "Send Verification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Return to Login",
                    color = Color(0xFFC76B0B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(8.dp),
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun ForgotPasswordPreview() {
    MobInTheme {
        ForgotPasswordContent(
            uiState = AuthUiState(),
            email = "user@example.com",
            snackbarHostState = SnackbarHostState(),
            onEmailChange = {},
            onSend = {},
            onBack = {},
        )
    }
}
