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
    primaryContainer = DeepTeal,
    secondary = Apricot,
    secondaryContainer = ColorTokens.DarkSecondaryContainer,
    background = NightInk,
    surface = ColorTokens.DarkSurface,
    surfaceContainer = ColorTokens.DarkSurfaceContainer,
    surfaceContainerLow = ColorTokens.DarkSurfaceContainerLow,
    surfaceContainerHigh = ColorTokens.DarkSurfaceContainerHigh,
    surfaceContainerLowest = ColorTokens.DarkSurfaceContainerLowest,
    surfaceBright = ColorTokens.DarkSurfaceBright,
    onPrimary = NightInk,
    onPrimaryContainer = ColorTokens.DarkOnPrimaryContainer,
    onSecondary = NightInk,
    onSecondaryContainer = ColorTokens.DarkOnSurfaceVariant,
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
    val LightModalFieldSurface = Color(0xFFE2E6E0)
    val LightModalPrimaryText = Color(0xFF17352D)
    val LightModalSecondaryText = Color(0xFF305A4E)
    val LightModalMutedText = Color(0xFF3E6D60)
    val LightModalPlaceholderText = Color(0xFF64736F)
    val LightModalToggleContainer = Color(0xFFD1DDD7)
    val LightModalToggleSelected = Color(0xFFF5FBF7)
    val LightModalSwitchThumb = Color(0xFFE3E8E5)
    val LightModalSwitchTrack = Color(0xFFAAB7B1)
    val DarkSurface = Color(0xFF18211C)
    val DarkSurfaceContainer = Color(0xFF202B25)
    val DarkSurfaceContainerLow = Color(0xFF1B251F)
    val DarkSurfaceContainerHigh = Color(0xFF2A362F)
    val DarkSurfaceContainerLowest = Color(0xFF233028)
    val DarkSurfaceBright = Color(0xFF2E3A33)
    val DarkSecondaryContainer = Color(0xFF334A42)
    val DarkOnPrimaryContainer = Color(0xFFE8FFF4)
    val DarkOnSurfaceVariant = Color(0xFFC4CEC7)
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
