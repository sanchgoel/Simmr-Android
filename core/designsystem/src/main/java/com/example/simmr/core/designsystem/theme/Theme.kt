package com.example.simmr.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SimmrColorScheme = lightColorScheme(
    primary = SimmrColors.Coral,
    onPrimary = Color.White,
    primaryContainer = SimmrColors.Tint,
    onPrimaryContainer = SimmrColors.TextDark,
    secondary = SimmrColors.Amber,
    onSecondary = SimmrColors.TextDark,
    background = SimmrColors.CreamBackground,
    onBackground = SimmrColors.TextDark,
    surface = SimmrColors.CreamCard,
    onSurface = SimmrColors.TextDark,
    surfaceVariant = SimmrColors.Tint,
    onSurfaceVariant = SimmrColors.TextMuted,
    outline = SimmrColors.Border,
    outlineVariant = SimmrColors.Border,
)

/**
 * The app intentionally keeps its warm iOS palette in both system modes.
 * Dynamic color is disabled so the Android and iOS experiences stay identical.
 */
@Composable
fun SimmrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SimmrColorScheme,
        typography = SimmrTypography,
        content = content,
    )
}
