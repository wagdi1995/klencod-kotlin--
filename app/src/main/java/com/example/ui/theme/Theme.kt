package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardBg,
    primaryContainer = LightBlue,
    onPrimaryContainer = PrimaryDarkBlue,
    secondary = DeepNavy,
    onSecondary = CardBg,
    secondaryContainer = LightBlueHeader,
    onSecondaryContainer = DeepNavy,
    tertiary = PositiveGreen,
    onTertiary = CardBg,
    tertiaryContainer = PositiveGreenLight,
    onTertiaryContainer = PositiveGreenDark,
    error = NegativeRed,
    onError = CardBg,
    errorContainer = NegativeRedLight,
    onErrorContainer = NegativeRedDark,
    background = AppBg,
    onBackground = NeutralDark,
    surface = CardBg,
    onSurface = NeutralDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = NeutralTextSecondary,
    outline = NeutralBorder,
    outlineVariant = LightGrayDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardBg,
    primaryContainer = DeepNavy,
    onPrimaryContainer = LightBlue,
    secondary = LightBlue,
    onSecondary = DeepNavy,
    background = DeepNavy,
    onBackground = CardBg,
    surface = DeepNavy,
    onSurface = CardBg,
    surfaceVariant = NeutralDark,
    onSurfaceVariant = NeutralTextSecondary,
    outline = NeutralBorder
)

@Composable
fun HisabatiTheme(
    darkTheme: Boolean = false, // Light mode is default as per requirements
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AppBg.toArgb()
                window.navigationBarColor = AppBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
