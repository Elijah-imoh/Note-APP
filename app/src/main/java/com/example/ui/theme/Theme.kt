package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MonochromeLightColorScheme = lightColorScheme(
    primary = MonochromePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = MonochromeContainerLight,
    onPrimaryContainer = OnMonochromeContainerLight,
    secondary = MonochromeSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = MonochromeSecondaryContainerLight,
    onSecondaryContainer = OnMonochromeSecondaryContainerLight,
    tertiary = MonochromeTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = MonochromeTertiaryContainerLight,
    onTertiaryContainer = OnMonochromeTertiaryContainerLight,
    background = PaperLightBackground,
    onBackground = InkLightPrimary,
    surface = PaperLightSurface,
    onSurface = InkLightPrimary,
    surfaceVariant = PaperLightSurfaceVariant,
    onSurfaceVariant = InkLightSecondary,
    surfaceContainerLowest = PaperLightSurfaceElevated,
    surfaceContainerLow = PaperLightSurface,
    surfaceContainer = PaperLightSurface,
    surfaceContainerHigh = PaperLightSurfaceVariant,
    surfaceContainerHighest = OutlineVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = Color(0xFF1A1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFE5E5E5),
    onErrorContainer = Color(0xFF0A0A0A)
)

private val MonochromeDarkColorScheme = darkColorScheme(
    primary = MonochromePrimaryDark,
    onPrimary = Color(0xFF0A0A0A),
    primaryContainer = MonochromeContainerDark,
    onPrimaryContainer = OnMonochromeContainerDark,
    secondary = MonochromeSecondaryDark,
    onSecondary = Color(0xFF0A0A0A),
    secondaryContainer = MonochromeSecondaryContainerDark,
    onSecondaryContainer = OnMonochromeSecondaryContainerDark,
    tertiary = MonochromeTertiaryDark,
    onTertiary = Color(0xFF0A0A0A),
    tertiaryContainer = MonochromeTertiaryContainerDark,
    onTertiaryContainer = OnMonochromeTertiaryContainerDark,
    background = PaperDarkBackground,
    onBackground = InkDarkPrimary,
    surface = PaperDarkSurface,
    onSurface = InkDarkPrimary,
    surfaceVariant = PaperDarkSurfaceVariant,
    onSurfaceVariant = InkDarkSecondary,
    surfaceContainerLowest = PaperDarkBackground,
    surfaceContainerLow = PaperDarkSurface,
    surfaceContainer = PaperDarkSurfaceElevated,
    surfaceContainerHigh = PaperDarkSurfaceVariant,
    surfaceContainerHighest = OutlineDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = Color(0xFFE5E5E5),
    onError = Color(0xFF0A0A0A),
    errorContainer = Color(0xFF2E2E2E),
    onErrorContainer = Color(0xFFFAFAFA)
)

val EditorialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun MyNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MonochromeDarkColorScheme else MonochromeLightColorScheme
    val typography = createEditorialTypography(fontScale)

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = EditorialShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyNotesTheme(darkTheme = darkTheme, content = content)
}
