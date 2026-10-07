package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val EditorialSerifFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val TactileSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val TabularMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium)
)

fun createEditorialTypography(scale: Float = 1.0f): Typography {
    val s = scale.coerceIn(0.85f, 1.35f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (38 * s).sp,
            lineHeight = (46 * s).sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (32 * s).sp,
            lineHeight = (40 * s).sp,
            letterSpacing = (-0.25).sp
        ),
        displaySmall = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (26 * s).sp,
            lineHeight = (34 * s).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * s).sp,
            lineHeight = (32 * s).sp,
            letterSpacing = (-0.2).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (21 * s).sp,
            lineHeight = (28 * s).sp,
            letterSpacing = (-0.15).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (18 * s).sp,
            lineHeight = (25 * s).sp
        ),
        titleLarge = TextStyle(
            fontFamily = EditorialSerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (19 * s).sp,
            lineHeight = (26 * s).sp,
            letterSpacing = (-0.1).sp
        ),
        titleMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * s).sp,
            lineHeight = (22 * s).sp,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * s).sp,
            lineHeight = (26 * s).sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (21 * s).sp,
            letterSpacing = 0.15.sp
        ),
        bodySmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12.5f * s).sp,
            lineHeight = (18 * s).sp,
            letterSpacing = 0.2.sp
        ),
        labelLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13.5f * s).sp,
            lineHeight = (18 * s).sp,
            letterSpacing = 0.2.sp
        ),
        labelMedium = TextStyle(
            fontFamily = TabularMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5f * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.3.sp
        ),
        labelSmall = TextStyle(
            fontFamily = TabularMonoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (10.5f * s).sp,
            lineHeight = (14 * s).sp,
            letterSpacing = 0.4.sp
        )
    )
}

val Typography = createEditorialTypography(1.0f)
