package com.mobin.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.mobin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var otpValue by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    VerificationContent(
        uiState = uiState,
        email = email,
        otpValue = otpValue,
        snackbarHostState = snackbarHostState,
        onOtpChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpValue = it },
        onVerify = { viewModel.verifyOtp(email, otpValue, onVerified) },
        onResend = {
            viewModel.sendPasswordResetOtp(email) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("A new 6-digit code has been sent to $email")
                }
            }
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerificationContent(
    uiState: AuthUiState,
    email: String,
    otpValue: String,
    snackbarHostState: SnackbarHostState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
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
                    text = "Verify Your Email",
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
                    text = "Enter the 6-digit code sent to\n${email.ifBlank { "your email address" }}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF555555),
                        lineHeight = 22.sp,
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 6-box OTP input
                BasicTextField(
                    value = otpValue,
                    onValueChange = onOtpChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            repeat(6) { index ->
                                val char = otpValue.getOrNull(index)
                                val isFocused = otpValue.length == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
                                        .background(White, RoundedCornerShape(14.dp))
                                        .border(
                                            width = if (isFocused) 2.dp else 1.dp,
                                            color = if (isFocused) GoldenMarigold else Color(0xFFDCC3B2),
                                            shape = RoundedCornerShape(14.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = char?.toString() ?: "",
                                        style = TextStyle(
                                            fontFamily = AlbertSansFamily,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF222222),
                                            textAlign = TextAlign.Center,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Verify Button
                Button(
                    onClick = onVerify,
                    enabled = otpValue.length == 6 && !uiState.isLoading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldenMarigold,
                        contentColor = White,
                        disabledContainerColor = GoldenMarigold.copy(alpha = 0.5f),
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
                            text = "Verify",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Didn't receive a code?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF555555)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Resend",
                        color = Color(0xFFC76B0B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(onClick = onResend)
                            .padding(4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun VerificationScreenPreview() {
    MobInTheme {
        VerificationContent(
            uiState = AuthUiState(),
            email = "user@example.com",
            otpValue = "123",
            snackbarHostState = SnackbarHostState(),
            onOtpChange = {},
            onVerify = {},
            onResend = {},
            onBack = {},
        )
    }
}
