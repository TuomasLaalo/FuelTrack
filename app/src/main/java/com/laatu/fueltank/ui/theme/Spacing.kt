package com.laatu.fueltank.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FuelTankSpacing(
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val regular: Dp = 16.dp,
    val large: Dp = 20.dp,
    val xLarge: Dp = 24.dp,
    val xxLarge: Dp = 32.dp,
)

val LocalFuelTankSpacing = staticCompositionLocalOf { FuelTankSpacing() }
