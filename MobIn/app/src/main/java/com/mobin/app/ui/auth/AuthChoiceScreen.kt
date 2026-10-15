package com.mobin.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.mobin.app.R
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobin.app.ui.theme.*

@Composable
fun AuthChoiceScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    // Tilted diagonal ombre gradient matching the app's auth screens
    val tiltedOmbreBrush = Brush.linearGradient(
        0.0f to Color(0xFFF9D490),
        0.22f to Color(0xFFFAE0B0),
        0.50f to Color(0xFFFFFFFF),
        0.75f to Color(0xFFFFF7EA),
        1.0f to Color(0xFFFDE4B7),
        start = Offset(0f, 0f),
        end = Offset(1200f, 2200f),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tiltedOmbreBrush)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.9f))

            // ── App Brand Icon ──────────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "Mob'in Logo",
                modifier = Modifier
                    .width(180.dp)
                    .height(128.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── App Title ───────────────────────────────────────────────────
            Text(
                text = "Mob'in",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = ComfortaaFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    color = Color(0xFF1E1E1E),
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Slogan ──────────────────────────────────────────────────────
            Text(
                text = "Stop Roaming, Start Mob'in",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AlbertSansFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 15.sp,
                    color = OliveBronze.copy(alpha = 0.85f),
                ),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Description ─────────────────────────────────────────────────
            Text(
                text = "Discover dormitories, apartments, and boarding houses that fit your lifestyle and budget.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AlbertSansFamily,
                    fontSize = 13.5.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.weight(1.0f))

            // ── Log In Button (Primary Pill) ────────────────────────────────
            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldenMarigold,
                    contentColor = White,
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
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AlbertSansFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Register Button (Outlined/White Pill) ───────────────────────
            Button(
                onClick = onNavigateToRegister,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White.copy(alpha = 0.95f),
                    contentColor = ChestnutBark,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = Color(0x1F000000),
                        spotColor = Color(0x1F000000),
                    )
                    .border(
                        width = 1.8.dp,
                        color = GoldenMarigold,
                        shape = RoundedCornerShape(50),
                    ),
            ) {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AlbertSansFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChestnutBark,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun AuthChoiceScreenPreview() {
    MobInTheme {
        AuthChoiceScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {},
        )
    }
}
