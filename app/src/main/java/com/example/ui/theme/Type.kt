package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val TactileSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

fun createEditorialTypography(scale: Float = 1.0f): Typography {
    val s = scale.coerceIn(0.85f, 1.35f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (34 * s).sp,
            lineHeight = (40 * s).sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * s).sp,
            lineHeight = (34 * s).sp,
            letterSpacing = (-0.4).sp
        ),
        displaySmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * s).sp,
            lineHeight = (32 * s).sp,
            letterSpacing = (-0.3).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * s).sp,
            lineHeight = (30 * s).sp,
            letterSpacing = (-0.3).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * s).sp,
            lineHeight = (26 * s).sp,
            letterSpacing = (-0.2).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * s).sp,
            lineHeight = (24 * s).sp,
            letterSpacing = (-0.15).sp
        ),
        titleLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (19 * s).sp,
            lineHeight = (25 * s).sp,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * s).sp,
            lineHeight = (22 * s).sp,
            letterSpacing = (-0.1).sp
        ),
        titleSmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15.5f * s).sp,
            lineHeight = (23 * s).sp,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14.5f * s).sp,
            lineHeight = (21 * s).sp,
            letterSpacing = 0.sp
        ),
        bodySmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13 * s).sp,
            lineHeight = (18 * s).sp,
            letterSpacing = 0.sp
        ),
        labelLarge = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13.5f * s).sp,
            lineHeight = (18 * s).sp,
            letterSpacing = 0.sp
        ),
        labelMedium = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12.5f * s).sp,
            lineHeight = (16 * s).sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = TactileSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5f * s).sp,
            lineHeight = (15 * s).sp,
            letterSpacing = 0.1.sp
        )
    )
}

val Typography = createEditorialTypography(1.0f)
