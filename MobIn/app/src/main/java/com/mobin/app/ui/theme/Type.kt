@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.mobin.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mobin.app.R

// ── Comfortaa (variable) — titles and headings ────────────────────────────────
val ComfortaaFamily = FontFamily(
    Font(resId = R.font.comfortaa_variable, weight = FontWeight.Normal,   variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resId = R.font.comfortaa_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(resId = R.font.comfortaa_variable, weight = FontWeight.Bold,     variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

// ── Albert Sans (variable) — body, labels, buttons, inputs ───────────────────
val AlbertSansFamily = FontFamily(
    Font(resId = R.font.albert_sans_variable, weight = FontWeight.Normal,   variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resId = R.font.albert_sans_variable, weight = FontWeight.Medium,   variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(resId = R.font.albert_sans_variable, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(resId = R.font.albert_sans_variable, weight = FontWeight.Bold,     variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

// ── Material3 Typography ──────────────────────────────────────────────────────
val Typography = Typography(
    displayLarge   = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.Bold,     fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium  = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.Bold,     fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall   = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.Bold,     fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.Bold,     fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge     = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = ComfortaaFamily, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = AlbertSansFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = AlbertSansFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = AlbertSansFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = AlbertSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = AlbertSansFamily, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = AlbertSansFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)
