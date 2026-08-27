package com.mobin.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.ui.theme.*

// ── Stateful entry point ──────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    LoginScreenContent(
        uiState = uiState,
        email = email,
        password = password,
        passwordVisible = passwordVisible,
        snackbarHostState = snackbarHostState,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
        onLogin = { viewModel.signIn(email, password, onLoginSuccess) },
        onForgotPassword = onForgotPassword,
    )
}

// ── Stateless UI with Tilted Ombre Background ────────────────────────────────
@Composable
private fun LoginScreenContent(
    uiState: AuthUiState,
    email: String,
    password: String,
    passwordVisible: Boolean,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    // Tilted diagonal ombre gradient from top-left golden warm to white center to peach bottom
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
                Spacer(modifier = Modifier.height(130.dp))

                // Title in Comfortaa font
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = ComfortaaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(48.dp))

                // ── Email Input Field ─────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
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
                                text = "Enter your email",
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

                Spacer(modifier = Modifier.height(20.dp))

                // ── Password Input Field ──────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF222222),
                            fontSize = 13.5.sp,
                        ),
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        placeholder = {
                            Text(
                                text = "Enter your password",
                                color = Color(0xFFAFAFAF),
                                fontSize = 13.5.sp,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onPasswordVisibleToggle) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Forgot Password link (right-aligned)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFFC76B0B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(onClick = onForgotPassword)
                            .padding(end = 4.dp, top = 2.dp, bottom = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // ── Pill Log In Button with Shadow ───────────────────────────
                Button(
                    onClick = onLogin,
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
                            text = "Log In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LoginScreenPreview() {
    MobInTheme {
        LoginScreenContent(
            uiState = AuthUiState(),
            email = "user@example.com",
            password = "password123",
            passwordVisible = false,
            snackbarHostState = SnackbarHostState(),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibleToggle = {},
            onLogin = {},
            onForgotPassword = {},
        )
    }
}
