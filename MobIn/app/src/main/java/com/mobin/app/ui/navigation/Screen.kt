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
    object Search          : Screen("search")
    object CategoryList    : Screen("category_list/{categoryName}") {
        fun createRoute(categoryName: String): String {
            val encoded = URLEncoder.encode(categoryName.trim(), StandardCharsets.UTF_8.toString())
            return "category_list/$encoded"
        }
    }
    object PropertyDetails : Screen("property_details/{propertyId}") {
        fun createRoute(propertyId: String): String = "property_details/$propertyId"
    }
    object LandlordProfile : Screen("landlord_profile/{propertyId}") {
        fun createRoute(propertyId: String): String = "landlord_profile/$propertyId"
    }
    object Chat            : Screen("chat/{chatId}?initialMessage={initialMessage}") {
        fun createRoute(chatId: String, initialMessage: String? = null): String {
            return if (!initialMessage.isNullOrBlank()) {
                val encodedMsg = URLEncoder.encode(initialMessage.trim(), StandardCharsets.UTF_8.toString())
                "chat/$chatId?initialMessage=$encodedMsg"
            } else {
                "chat/$chatId?initialMessage="
            }
        }
    }
    object Main            : Screen("main")
    object ReviewInfo      : Screen("review_info")
    object ChangePassword  : Screen("change_password")
}
