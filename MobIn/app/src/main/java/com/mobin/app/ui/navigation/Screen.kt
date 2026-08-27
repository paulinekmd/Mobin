package com.mobin.app.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Splash          : Screen("splash")
    object Onboarding      : Screen("onboarding")
    object Login           : Screen("login")
    object ForgotPassword  : Screen("forgot_password")
    object Verification    : Screen("verification/{email}") {
        fun createRoute(email: String): String {
            val encoded = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8.toString())
            return "verification/$encoded"
        }
    }
    object ResetPassword   : Screen("reset_password/{email}") {
        fun createRoute(email: String): String {
            val encoded = URLEncoder.encode(email.trim(), StandardCharsets.UTF_8.toString())
            return "reset_password/$encoded"
        }
    }
    object Main            : Screen("main")
    object ReviewInfo      : Screen("review_info")
    object ChangePassword  : Screen("change_password")
}
