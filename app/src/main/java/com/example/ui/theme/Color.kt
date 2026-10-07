package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.NoteColorPalette

// Light Palette — Warm Alabaster, Travertine & Terracotta Ink
val PaperLightBackground = Color(0xFFFAF7F2)
val PaperLightSurface = Color(0xFFF3EFE6)
val PaperLightSurfaceElevated = Color(0xFFFFFFFF)
val PaperLightSurfaceVariant = Color(0xFFEBE4D8)
val InkLightPrimary = Color(0xFF1E1B18)
val InkLightSecondary = Color(0xFF5C554D)
val InkLightMuted = Color(0xFF8A8177)
val TerracottaPrimaryLight = Color(0xFFC0522A)
val TerracottaContainerLight = Color(0xFFF9E2D8)
val OnTerracottaContainerLight = Color(0xFF421506)
val SageSecondaryLight = Color(0xFF3E6353)
val SageContainerLight = Color(0xFFD9EBE2)
val OnSageContainerLight = Color(0xFF0E291E)
val OchreTertiaryLight = Color(0xFFB57C1E)
val OchreContainerLight = Color(0xFFFCEBC8)
val OnOchreContainerLight = Color(0xFF382300)
val OutlineLight = Color(0xFFD6CEC2)
val OutlineVariantLight = Color(0xFFE5DFD3)

// Dark Palette — Obsidian Ink, Warm Charcoal & Luminous Terracotta
val PaperDarkBackground = Color(0xFF141210)
val PaperDarkSurface = Color(0xFF1E1B18)
val PaperDarkSurfaceElevated = Color(0xFF282420)
val PaperDarkSurfaceVariant = Color(0xFF332E29)
val InkDarkPrimary = Color(0xFFF4EFE6)
val InkDarkSecondary = Color(0xFFB8B0A4)
val InkDarkMuted = Color(0xFF857D73)
val TerracottaPrimaryDark = Color(0xFFE57C56)
val TerracottaContainerDark = Color(0xFF5C2410)
val OnTerracottaContainerDark = Color(0xFFFFDBCF)
val SageSecondaryDark = Color(0xFF88B3A0)
val SageContainerDark = Color(0xFF233D31)
val OnSageContainerDark = Color(0xFFD9EBE2)
val OchreTertiaryDark = Color(0xFFE8B45A)
val OchreContainerDark = Color(0xFF4F3405)
val OnOchreContainerDark = Color(0xFFFCEBC8)
val OutlineDark = Color(0xFF453F39)
val OutlineVariantDark = Color(0xFF302B27)

// Note Paper Swatches (Light & Dark adaptive)
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
                border = Color(0xFFE5DFD3),
                accentBar = Color(0xFFC0522A),
                swatchPreview = Color(0xFFFAF7F2)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF1E1B18),
                border = Color(0xFF332E29),
                accentBar = Color(0xFFE57C56),
                swatchPreview = Color(0xFF282420)
            )
        }

        NoteColorPalette.WARM_SAND -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFF6EFE2),
                border = Color(0xFFE0D4BE),
                accentBar = Color(0xFFB57C1E),
                swatchPreview = Color(0xFFEEDFC3)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF29231B),
                border = Color(0xFF42382B),
                accentBar = Color(0xFFE8B45A),
                swatchPreview = Color(0xFF3D3326)
            )
        }

        NoteColorPalette.TERRACOTTA -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFFBF0EC),
                border = Color(0xFFEBD2C8),
                accentBar = Color(0xFFC0522A),
                swatchPreview = Color(0xFFF4D6C9)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF2B1E1A),
                border = Color(0xFF4A312A),
                accentBar = Color(0xFFE57C56),
                swatchPreview = Color(0xFF4A2B22)
            )
        }

        NoteColorPalette.SAGE -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFEEF4F0),
                border = Color(0xFFCEE0D5),
                accentBar = Color(0xFF3E6353),
                swatchPreview = Color(0xFFD4E6DC)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF1A2420),
                border = Color(0xFF2C3E36),
                accentBar = Color(0xFF88B3A0),
                swatchPreview = Color(0xFF263830)
            )
        }

        NoteColorPalette.OCHRE -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFFAF3E3),
                border = Color(0xFFE8D8B5),
                accentBar = Color(0xFFC4861C),
                swatchPreview = Color(0xFFF5E3B8)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF282215),
                border = Color(0xFF453A22),
                accentBar = Color(0xFFE8B45A),
                swatchPreview = Color(0xFF3D321C)
            )
        }

        NoteColorPalette.SLATE -> if (!isDark) {
            NoteSurfaceColors(
                container = Color(0xFFEDF0F4),
                border = Color(0xFFD0D8E2),
                accentBar = Color(0xFF475B73),
                swatchPreview = Color(0xFFD6DFEB)
            )
        } else {
            NoteSurfaceColors(
                container = Color(0xFF1B2026),
                border = Color(0xFF2E3742),
                accentBar = Color(0xFF8CA5C2),
                swatchPreview = Color(0xFF28313B)
            )
        }
    }
}

fun resolveFolderAccentColor(accentKey: String, isDark: Boolean): Color {
    return when (accentKey) {
        "terracotta" -> if (isDark) TerracottaPrimaryDark else TerracottaPrimaryLight
        "sage" -> if (isDark) SageSecondaryDark else SageSecondaryLight
        "ochre" -> if (isDark) OchreTertiaryDark else OchreTertiaryLight
        "slate" -> if (isDark) Color(0xFF8CA5C2) else Color(0xFF475B73)
        "plum" -> if (isDark) Color(0xFFC494B8) else Color(0xFF7A496E)
        else -> if (isDark) TerracottaPrimaryDark else TerracottaPrimaryLight
    }
}
