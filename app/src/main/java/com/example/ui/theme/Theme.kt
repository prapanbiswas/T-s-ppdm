package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MandirLightColorScheme = lightColorScheme(
    primary = MandirCrimson,
    onPrimary = AppTextInverse,
    primaryContainer = MandirCrimsonLight,
    onPrimaryContainer = MandirCrimsonDark,
    secondary = MandirGold,
    onSecondary = AppTextInverse,
    secondaryContainer = MandirGoldLight,
    onSecondaryContainer = MandirGoldDark,
    tertiary = BrandSuccess,
    onTertiary = AppTextInverse,
    tertiaryContainer = BrandSuccessLight,
    onTertiaryContainer = BrandSuccess,
    error = BrandError,
    onError = AppTextInverse,
    errorContainer = BrandErrorLight,
    onErrorContainer = BrandError,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppSurfaceSubtle,
    onSurfaceVariant = AppTextSecondary,
    surfaceContainerLowest = M3SurfaceContainerLowest,
    surfaceContainerLow = M3SurfaceContainerLow,
    surfaceContainer = M3SurfaceContainer,
    surfaceContainerHigh = M3SurfaceContainerHigh,
    surfaceContainerHighest = M3SurfaceContainerHighest,
    surfaceDim = M3SurfaceDim,
    surfaceBright = M3SurfaceBright,
    outline = AppBorder,
    outlineVariant = AppBorderStrong
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MandirLightColorScheme,
        typography = Typography,
        content = content
    )
}
