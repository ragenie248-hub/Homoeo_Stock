package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class StockStatusColors(
    val healthyBg: Color,
    val healthyText: Color,
    val lowBg: Color,
    val lowText: Color,
    val criticalBg: Color,
    val criticalText: Color,
)

val LocalStockStatusColors = staticCompositionLocalOf {
    StockStatusColors(
        healthyBg = StatusHealthyBg,
        healthyText = StatusHealthyText,
        lowBg = StatusLowBg,
        lowText = StatusLowText,
        criticalBg = StatusCriticalBg,
        criticalText = StatusCriticalText,
    )
}

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = OnForestPrimary,
    primaryContainer = EucalyptusContainer,
    onPrimaryContainer = OnEucalyptusContainer,
    secondary = TerracottaSecondary,
    onSecondary = OnTerracottaSecondary,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = OnWarmAmberContainer,
    tertiary = SageTertiary,
    onTertiary = OnSageTertiary,
    tertiaryContainer = MistTertiaryContainer,
    onTertiaryContainer = OnMistTertiaryContainer,
    background = ParchmentBackground,
    onBackground = OnParchment,
    surface = LinenSurface,
    onSurface = OnParchment,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = OnWarmSurfaceVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LinenSurface,
    surfaceContainer = Color(0xFFF3EFE6),
    surfaceContainerHigh = WarmSurfaceVariant,
    outline = OutlineStone,
    outlineVariant = OutlineSubtle,
    error = StatusCriticalText,
    onError = Color.White,
    errorContainer = StatusCriticalBg,
    onErrorContainer = StatusCriticalText,
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = OnForestPrimaryDark,
    primaryContainer = EucalyptusContainerDark,
    onPrimaryContainer = OnEucalyptusContainerDark,
    secondary = TerracottaSecondaryDark,
    onSecondary = OnTerracottaSecondaryDark,
    secondaryContainer = WarmAmberContainerDark,
    onSecondaryContainer = OnWarmAmberContainerDark,
    tertiary = SageTertiaryDark,
    onTertiary = OnSageTertiaryDark,
    tertiaryContainer = MistTertiaryContainerDark,
    onTertiaryContainer = OnMistTertiaryContainerDark,
    background = DeepForestBackground,
    onBackground = OnDeepForest,
    surface = DarkCardSurface,
    onSurface = OnDeepForest,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0C120F),
    surfaceContainerLow = DarkCardSurface,
    surfaceContainer = Color(0xFF1D2721),
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = OutlineDark,
    outlineVariant = Color(0xFF2B3831),
    error = StatusCriticalTextDark,
    onError = Color(0xFF4A0D0B),
    errorContainer = StatusCriticalBgDark,
    onErrorContainer = StatusCriticalTextDark,
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HomeoStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val statusColors = if (darkTheme) {
        StockStatusColors(
            healthyBg = StatusHealthyBgDark,
            healthyText = StatusHealthyTextDark,
            lowBg = StatusLowBgDark,
            lowText = StatusLowTextDark,
            criticalBg = StatusCriticalBgDark,
            criticalText = StatusCriticalTextDark,
        )
    } else {
        StockStatusColors(
            healthyBg = StatusHealthyBg,
            healthyText = StatusHealthyText,
            lowBg = StatusLowBg,
            lowText = StatusLowText,
            criticalBg = StatusCriticalBg,
            criticalText = StatusCriticalText,
        )
    }

    CompositionLocalProvider(LocalStockStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
