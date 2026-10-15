package com.mobin.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.mobin.app.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobin.app.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        fullName = fullName,
        email = email,
        phone = phone,
        password = password,
        confirmPassword = confirmPassword,
        passwordVisible = passwordVisible,
        confirmPasswordVisible = confirmPasswordVisible,
        snackbarHostState = snackbarHostState,
        onFullNameChange = { fullName = it },
        onEmailChange = { email = it },
        onPhoneChange = { phone = it },
        onPasswordChange = { password = it },
        onConfirmPasswordChange = { confirmPassword = it },
        onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
        onConfirmPasswordVisibleToggle = { confirmPasswordVisible = !confirmPasswordVisible },
        onRegister = {
            viewModel.signUp(
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                confirmPassword = confirmPassword,
                onSuccess = onRegisterSuccess,
            )
        },
        onNavigateToLogin = onNavigateToLogin,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenContent(
    uiState: AuthUiState,
    fullName: String,
    email: String,
    phone: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    snackbarHostState: SnackbarHostState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onConfirmPasswordVisibleToggle: () -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
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

    val passwordMismatch = confirmPassword.isNotEmpty() && password.isNotEmpty() && password != confirmPassword

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
                            tint = OliveBronze,
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
                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "Mob'in Logo",
                    modifier = Modifier
                        .width(110.dp)
                        .height(78.dp),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Title in Comfortaa font
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = ComfortaaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Color(0xFF1E1E1E),
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign up to start finding your perfect stay.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = AlbertSansFamily,
                        fontSize = 14.sp,
                        color = OliveBronze.copy(alpha = 0.85f),
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Full Name Input Field ────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Full Name",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF222222),
                            fontSize = 13.5.sp,
                        ),
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = onFullNameChange,
                        placeholder = {
                            Text(
                                text = "Enter your full name",
                                color = Color(0xFFAFAFAF),
                                fontSize = 13.5.sp,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                        ),
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

                Spacer(modifier = Modifier.height(18.dp))

                // ── Email Input Field ─────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
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

                Spacer(modifier = Modifier.height(18.dp))

                // ── Phone Input Field (Optional) ──────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Phone Number (Optional)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF222222),
                            fontSize = 13.5.sp,
                        ),
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        placeholder = {
                            Text(
                                text = "e.g. 0912 345 6789",
                                color = Color(0xFFAFAFAF),
                                fontSize = 13.5.sp,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                Spacer(modifier = Modifier.height(18.dp))

                // ── Password Input Field ──────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
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
                                text = "Create a password (min. 6 characters)",
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

                Spacer(modifier = Modifier.height(18.dp))

                // ── Confirm Password Input Field ──────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Confirm Password",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF222222),
                            fontSize = 13.5.sp,
                        ),
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        placeholder = {
                            Text(
                                text = "Confirm your password",
                                color = Color(0xFFAFAFAF),
                                fontSize = 13.5.sp,
                            )
                        },
                        singleLine = true,
                        isError = passwordMismatch,
                        shape = RoundedCornerShape(50),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onConfirmPasswordVisibleToggle) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldenMarigold,
                            unfocusedBorderColor = if (passwordMismatch) ErrorRed else Color(0xFFDCC3B2),
                            errorBorderColor = ErrorRed,
                            focusedContainerColor = White.copy(alpha = 0.95f),
                            unfocusedContainerColor = White.copy(alpha = 0.95f),
                            focusedTextColor = Color(0xFF222222),
                            unfocusedTextColor = Color(0xFF222222),
                        ),
                        supportingText = if (passwordMismatch) {
                            { Text("Passwords do not match", color = ErrorRed, fontSize = 12.sp) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Register Button with Shadow ───────────────────────────────
                Button(
                    onClick = onRegister,
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
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color(0x33FBB81F),
                            spotColor = Color(0x66FBB81F),
                        ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Text(
                            text = "Register",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = AlbertSansFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Switch to Log In Link ──────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 36.dp),
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
                            color = Color(0xFF555555),
                            fontSize = 14.sp,
                        ),
                    )
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = AlbertSansFamily,
                            color = GoldenMarigold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLogin)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun RegisterScreenPreview() {
    MobInTheme {
        RegisterScreenContent(
            uiState = AuthUiState(),
            fullName = "Juan dela Cruz",
            email = "juan@example.com",
            phone = "09123456789",
            password = "password123",
            confirmPassword = "password123",
            passwordVisible = false,
            confirmPasswordVisible = false,
            snackbarHostState = SnackbarHostState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onPasswordVisibleToggle = {},
            onConfirmPasswordVisibleToggle = {},
            onRegister = {},
            onNavigateToLogin = {},
            onBack = {},
        )
    }
}
