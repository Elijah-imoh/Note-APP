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

private val EditorialLightColorScheme = lightColorScheme(
    primary = TerracottaPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainerLight,
    onPrimaryContainer = OnTerracottaContainerLight,
    secondary = SageSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SageContainerLight,
    onSecondaryContainer = OnSageContainerLight,
    tertiary = OchreTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = OchreContainerLight,
    onTertiaryContainer = OnOchreContainerLight,
    background = PaperLightBackground,
    onBackground = InkLightPrimary,
    surface = PaperLightSurface,
    onSurface = InkLightPrimary,
    surfaceVariant = PaperLightSurfaceVariant,
    onSurfaceVariant = InkLightSecondary,
    surfaceContainerLowest = PaperLightSurfaceElevated,
    surfaceContainerLow = PaperLightBackground,
    surfaceContainer = PaperLightSurface,
    surfaceContainerHigh = PaperLightSurfaceVariant,
    surfaceContainerHighest = OutlineVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val EditorialDarkColorScheme = darkColorScheme(
    primary = TerracottaPrimaryDark,
    onPrimary = Color(0xFF381104),
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = OnTerracottaContainerDark,
    secondary = SageSecondaryDark,
    onSecondary = Color(0xFF0E291E),
    secondaryContainer = SageContainerDark,
    onSecondaryContainer = OnSageContainerDark,
    tertiary = OchreTertiaryDark,
    onTertiary = Color(0xFF382300),
    tertiaryContainer = OchreContainerDark,
    onTertiaryContainer = OnOchreContainerDark,
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
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

val EditorialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun MyNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme
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
