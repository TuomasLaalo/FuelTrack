package com.laatu.fueltank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForestGlow,
    secondary = Apricot,
    background = NightInk,
    surface = ColorTokens.DarkSurface,
    surfaceContainer = ColorTokens.DarkSurfaceContainer,
    surfaceContainerLow = ColorTokens.DarkSurfaceContainerLow,
    surfaceContainerHigh = ColorTokens.DarkSurfaceContainerHigh,
    surfaceContainerLowest = ColorTokens.DarkSurfaceContainerLowest,
    onPrimary = NightInk,
    onSecondary = NightInk,
    onBackground = WarmSurface,
    onSurface = WarmSurface,
    onSurfaceVariant = ColorTokens.DarkOnSurfaceVariant,
    outlineVariant = WarmOutline.copy(alpha = 0.18f),
)

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    primaryContainer = ForestGlow,
    secondary = Apricot,
    secondaryContainer = MintTint,
    background = WarmSurface,
    surface = WarmSurface,
    surfaceContainer = ColorTokens.LightSurfaceContainer,
    surfaceContainerLow = WarmSurfaceLow,
    surfaceContainerHigh = WarmSurfaceHigh,
    surfaceContainerLowest = WarmPaper,
    surfaceBright = WarmPaper,
    onPrimary = WarmPaper,
    onSecondary = NightInk,
    onBackground = WarmText,
    onSurface = WarmText,
    onSurfaceVariant = WarmTextMuted,
    outlineVariant = WarmOutline.copy(alpha = 0.2f),
)

object ColorTokens {
    val LightSurfaceContainer = Color(0xFFF0EDE9)
    val DarkSurface = Color(0xFF161C19)
    val DarkSurfaceContainer = Color(0xFF1E2622)
    val DarkSurfaceContainerLow = Color(0xFF1A221E)
    val DarkSurfaceContainerHigh = Color(0xFF27312C)
    val DarkSurfaceContainerLowest = Color(0xFF222B26)
    val DarkOnSurfaceVariant = Color(0xFFB7C0BA)
}

@Composable
fun FuelTankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = FuelTankTypography,
        shapes = FuelTankShapes,
    ) {
        CompositionLocalProvider(
            LocalFuelTankSpacing provides FuelTankSpacing(),
            content = content,
        )
    }
}
