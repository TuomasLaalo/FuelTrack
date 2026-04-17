package com.laatu.fueltank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class FuelTrackModalColors(
    val background: Color,
    val fieldSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val placeholderText: Color,
    val toggleContainer: Color,
    val toggleSelected: Color,
    val switchCheckedThumb: Color,
    val switchCheckedTrack: Color,
    val switchUncheckedThumb: Color,
    val switchUncheckedTrack: Color,
)

@Composable
fun rememberFuelTrackModalColors(
    modalStyle: Boolean,
): FuelTrackModalColors {
    val colorScheme = MaterialTheme.colorScheme
    val darkTheme = isSystemInDarkTheme()

    if (!modalStyle) {
        return FuelTrackModalColors(
            background = colorScheme.background,
            fieldSurface = colorScheme.surfaceContainerLowest,
            primaryText = colorScheme.onSurface,
            secondaryText = colorScheme.onSurfaceVariant,
            mutedText = colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            placeholderText = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            toggleContainer = colorScheme.surface.copy(alpha = 0.55f),
            toggleSelected = colorScheme.surface,
            switchCheckedThumb = colorScheme.surfaceBright,
            switchCheckedTrack = colorScheme.primary,
            switchUncheckedThumb = colorScheme.surface,
            switchUncheckedTrack = colorScheme.surfaceContainerHighest,
        )
    }

    return if (darkTheme) {
        FuelTrackModalColors(
            background = colorScheme.background,
            fieldSurface = colorScheme.surfaceContainerHigh,
            primaryText = colorScheme.onSurface,
            secondaryText = colorScheme.onSurfaceVariant,
            mutedText = colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            placeholderText = colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            toggleContainer = colorScheme.surfaceContainerHighest,
            toggleSelected = colorScheme.surfaceBright,
            switchCheckedThumb = colorScheme.surfaceBright,
            switchCheckedTrack = colorScheme.primary,
            switchUncheckedThumb = colorScheme.surfaceContainerLowest,
            switchUncheckedTrack = colorScheme.surfaceContainerHighest,
        )
    } else {
        FuelTrackModalColors(
            background = colorScheme.background,
            fieldSurface = ColorTokens.LightModalFieldSurface,
            primaryText = ColorTokens.LightModalPrimaryText,
            secondaryText = ColorTokens.LightModalSecondaryText,
            mutedText = ColorTokens.LightModalMutedText,
            placeholderText = ColorTokens.LightModalPlaceholderText,
            toggleContainer = ColorTokens.LightModalToggleContainer,
            toggleSelected = ColorTokens.LightModalToggleSelected,
            switchCheckedThumb = colorScheme.surfaceBright,
            switchCheckedTrack = colorScheme.primary,
            switchUncheckedThumb = ColorTokens.LightModalSwitchThumb,
            switchUncheckedTrack = ColorTokens.LightModalSwitchTrack,
        )
    }
}
