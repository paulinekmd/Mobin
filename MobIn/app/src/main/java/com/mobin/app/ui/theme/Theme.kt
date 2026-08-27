package com.mobin.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MobInColorScheme = lightColorScheme(
    primary          = GoldenMarigold,
    onPrimary        = ChestnutBark,
    primaryContainer = AmberSpice,
    onPrimaryContainer = White,
    secondary        = AmberSpice,
    onSecondary      = White,
    background       = PeachSand,
    onBackground     = OliveBronze,
    surface          = White,
    onSurface        = OliveBronze,
    surfaceVariant   = LightGray,
    onSurfaceVariant = MediumGray,
    error            = ErrorRed,
    onError          = White,
)

@Composable
fun MobInTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MobInColorScheme,
        typography  = Typography,
        content     = content,
    )
}