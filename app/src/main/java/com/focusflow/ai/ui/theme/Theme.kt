package com.focusflow.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.focusflow.ai.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = IndigoDark,
    secondary = SlatePrimary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SlateContainerLight,
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF171B2C),
    tertiary = VioletTertiary,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = VioletContainerLight,
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF2C1223),
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorLight
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    onPrimary = IndigoDark,
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFDDE1FF),
    secondary = androidx.compose.ui.graphics.Color(0xFFC3C5DD),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF2C2F42),
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDFE1F9),
    tertiary = androidx.compose.ui.graphics.Color(0xFFE8B6D4),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF45263C),
    tertiaryContainer = VioletContainerDark,
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFD8EE),
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorDark
)

@Composable
fun FocusFlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = FocusFlowTypography,
        content = content
    )
}
