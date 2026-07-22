package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadioDarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.Black,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    tertiary = AmberSecondary,
    background = DeepSlateBg,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = TextSecondary
)

@Composable
fun QualcommFmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RadioDarkColorScheme,
        typography = Typography,
        content = content
    )
}

