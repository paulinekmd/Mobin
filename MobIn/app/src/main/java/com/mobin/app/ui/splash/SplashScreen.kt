package com.mobin.app.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.mobin.app.BuildConfig
import com.mobin.app.R
import com.mobin.app.data.remote.SupabaseClient
import com.mobin.app.ui.theme.*
import com.mobin.app.util.DataStoreManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private const val TAG = "SplashScreen"

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToAuthChoice: () -> Unit,
    onNavigateToMain: () -> Unit,
) {
    LaunchedEffect(Unit) {
        Log.d(TAG, "SplashScreen LaunchedEffect started")
        delay(1200)
        val shouldGoToMain = try {
            kotlinx.coroutines.withTimeoutOrNull(1500) {
                if (BuildConfig.SUPABASE_URL.contains("YOUR_PROJECT_ID") || BuildConfig.SUPABASE_ANON_KEY.contains("YOUR_ANON_KEY") || BuildConfig.SUPABASE_URL.isBlank()) {
                    false
                } else {
                    SupabaseClient.client.auth.currentSessionOrNull() != null
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Session check failed: ", e)
            false
        }

        Log.d(TAG, "shouldGoToMain = $shouldGoToMain")
        if (shouldGoToMain) {
            onNavigateToMain()
        } else {
            val done = try {
                kotlinx.coroutines.withTimeoutOrNull(1000) {
                    DataStoreManager.isOnboardingComplete().first()
                } ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Onboarding check failed: ", e)
                false
            }
            Log.d(TAG, "onboarding done = $done")
            if (done) onNavigateToAuthChoice() else onNavigateToOnboarding()
        }
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier.fillMaxSize().background(PeachSand),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "Mob'in Logo",
                modifier = Modifier
                    .width(180.dp)
                    .height(128.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Mob'in",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = ComfortaaFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = OliveBronze,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Stop Roaming, Start Mob'in",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = AlbertSansFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = OliveBronze.copy(alpha = 0.7f),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun SplashPreview() {
    MobInTheme { SplashContent() }
}
