package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.NoteColorPalette

// Clean Minimal Monochrome Light Palette — Soft Off-White Canvas & Pure White Cards
val PaperLightBackground = Color(0xFFF5F5F7)
val PaperLightSurface = Color(0xFFFFFFFF)
val PaperLightSurfaceElevated = Color(0xFFFFFFFF)
val PaperLightSurfaceVariant = Color(0xFFEEEEF0)
val InkLightPrimary = Color(0xFF111113)
val InkLightSecondary = Color(0xFF48484A)
val InkLightMuted = Color(0xFF8E8E93)

val MonochromePrimaryLight = Color(0xFF141416)
val MonochromeContainerLight = Color(0xFFEBEBED)
val OnMonochromeContainerLight = Color(0xFF111113)

val MonochromeSecondaryLight = Color(0xFF2C2C2E)
val MonochromeSecondaryContainerLight = Color(0xFFE5E5EA)
val OnMonochromeSecondaryContainerLight = Color(0xFF111113)

val MonochromeTertiaryLight = Color(0xFF636366)
val MonochromeTertiaryContainerLight = Color(0xFFF0F0F2)
val OnMonochromeTertiaryContainerLight = Color(0xFF1C1C1E)

val OutlineLight = Color(0xFFD1D1D6)
val OutlineVariantLight = Color(0xFFE5E5EA)

// Clean Minimal Monochrome Dark Palette — Pure Obsidian Canvas & Elevated Charcoal Cards
val PaperDarkBackground = Color(0xFF0A0A0C)
val PaperDarkSurface = Color(0xFF161618)
val PaperDarkSurfaceElevated = Color(0xFF1C1C1E)
val PaperDarkSurfaceVariant = Color(0xFF242426)
val InkDarkPrimary = Color(0xFFFAFAFA)
val InkDarkSecondary = Color(0xFFAEAEB2)
val InkDarkMuted = Color(0xFF8E8E93)

val MonochromePrimaryDark = Color(0xFFFAFAFA)
val MonochromeContainerDark = Color(0xFF262629)
val OnMonochromeContainerDark = Color(0xFFFAFAFA)

val MonochromeSecondaryDark = Color(0xFFD1D1D6)
val MonochromeSecondaryContainerDark = Color(0xFF2C2C2E)
val OnMonochromeSecondaryContainerDark = Color(0xFFF2F2F7)

val MonochromeTertiaryDark = Color(0xFF98989D)
val MonochromeTertiaryContainerDark = Color(0xFF242426)
val OnMonochromeTertiaryContainerDark = Color(0xFFE5E5EA)

val OutlineDark = Color(0xFF3A3A3C)
val OutlineVariantDark = Color(0xFF28282A)

// Monochrome Grayscale Note Paper Stocks (Strictly Black, White & Grayscale)
data class NoteSurfaceColors(
    val container: Color,
    val border: Color,
    val accentBar: Color,
    val swatchPreview: Color
)

@Composable
fun NoteColorPalette.resolveColors(isDark: Boolean): NoteSurfaceColors {
    return when (this) {
        NoteColorPalette.DEFAULT -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFFFFFFF),
                border = Color(0xFFEAEAEF),
                accentBar = Color(0xFF141416),
                swatchPreview = Color(0xFFFFFFFF)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF161618),
                border = Color(0xFF28282A),
                accentBar = Color(0xFFFAFAFA),
                swatchPreview = Color(0xFF161618)
            )
        }

        NoteColorPalette.WARM_SAND -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFFFFFFF),
                border = Color(0xFFE5E5EA),
                accentBar = Color(0xFF2C2C2E),
                swatchPreview = Color(0xFFF5F5F7)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF1A1A1C),
                border = Color(0xFF2C2C2E),
                accentBar = Color(0xFFE5E5EA),
                swatchPreview = Color(0xFF1C1C1E)
            )
        }

        NoteColorPalette.TERRACOTTA -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFFAFAFC),
                border = Color(0xFFE0E0E5),
                accentBar = Color(0xFF1C1C1E),
                swatchPreview = Color(0xFFEBEBF0)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF1E1E20),
                border = Color(0xFF343438),
                accentBar = Color(0xFFD1D1D6),
                swatchPreview = Color(0xFF262629)
            )
        }

        NoteColorPalette.SAGE -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFF4F4F6),
                border = Color(0xFFD8D8DD),
                accentBar = Color(0xFF111113),
                swatchPreview = Color(0xFFE0E0E5)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF222225),
                border = Color(0xFF3A3A3C),
                accentBar = Color(0xFFC7C7CC),
                swatchPreview = Color(0xFF303034)
            )
        }

        NoteColorPalette.OCHRE -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFEEEEF0),
                border = Color(0xFFCCCCD2),
                accentBar = Color(0xFF000000),
                swatchPreview = Color(0xFFD1D1D6)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF28282C),
                border = Color(0xFF48484A),
                accentBar = Color(0xFFFFFFFF),
                swatchPreview = Color(0xFF3A3A3C)
            )
        }

        NoteColorPalette.SLATE -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFE6E6EA),
                border = Color(0xFFAEAEB2),
                accentBar = Color(0xFF000000),
                swatchPreview = Color(0xFFBCBCC0)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF303034),
                border = Color(0xFF545458),
                accentBar = Color(0xFFFFFFFF),
                swatchPreview = Color(0xFF48484A)
            )
        }
    }
}

fun resolveFolderAccentColor(accentKey: String, isDark: Boolean): Color {
    return when (accentKey) {
        "ink_100", "terracotta" -> if (isDark) Color(0xFFFAFAFA) else Color(0xFF141416)
        "ink_80", "sage" -> if (isDark) Color(0xFFD9D9D9) else Color(0xFF2C2C2E)
        "ink_60", "ochre" -> if (isDark) Color(0xFFB8B8B8) else Color(0xFF48484A)
        "ink_40", "slate" -> if (isDark) Color(0xFF9E9E9E) else Color(0xFF636366)
        "ink_20", "plum" -> if (isDark) Color(0xFF858585) else Color(0xFF8E8E93)
        else -> if (isDark) Color(0xFFFAFAFA) else Color(0xFF141416)
    }
}
