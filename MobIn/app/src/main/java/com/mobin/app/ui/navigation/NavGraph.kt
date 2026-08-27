package com.mobin.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobin.app.ui.auth.ForgotPasswordScreen
import com.mobin.app.ui.auth.LoginScreen
import com.mobin.app.ui.auth.VerificationScreen
import com.mobin.app.ui.main.MainScreen
import com.mobin.app.ui.onboarding.OnboardingScreen
import com.mobin.app.ui.profile.ChangePasswordScreen
import com.mobin.app.ui.profile.ReviewInfoScreen
import com.mobin.app.ui.splash.SplashScreen

@Composable
fun MobInNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onOtpSent = { email ->
                    navController.navigate(Screen.Verification.createRoute(email))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Verification.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = try {
                java.net.URLDecoder.decode(rawEmail, java.nio.charset.StandardCharsets.UTF_8.toString())
            } catch (e: Exception) { rawEmail }
            VerificationScreen(
                email = email,
                onVerified = {
                    navController.navigate(Screen.ResetPassword.createRoute(email)) {
                        popUpTo(Screen.Login.route)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = try {
                java.net.URLDecoder.decode(rawEmail, java.nio.charset.StandardCharsets.UTF_8.toString())
            } catch (e: Exception) { rawEmail }
            ChangePasswordScreen(
                isResetMode = true,
                targetEmail = email,
                onBack = { navController.popBackStack() },
                onPasswordChanged = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToReviewInfo = { navController.navigate(Screen.ReviewInfo.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.ReviewInfo.route) {
            ReviewInfoScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                isResetMode = false,
                onBack = { navController.popBackStack() },
                onPasswordChanged = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
