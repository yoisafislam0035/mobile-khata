package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.util.AppLanguage

val AppFontFamily = FontFamily.Default

fun getAppTypography(language: AppLanguage): Typography {
    val isUrdu = language == AppLanguage.URDU
    return Typography(
        displayLarge = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            lineHeight = if (isUrdu) 76.sp else 64.sp
        ),
        displayMedium = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            lineHeight = if (isUrdu) 62.sp else 52.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = if (isUrdu) 46.sp else 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = if (isUrdu) 40.sp else 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = if (isUrdu) 36.sp else 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = if (isUrdu) 34.sp else 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = if (isUrdu) 26.sp else 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = if (isUrdu) 22.sp else 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = if (isUrdu) 26.sp else 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = if (isUrdu) 23.sp else 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = if (isUrdu) 19.sp else 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = if (isUrdu) 22.sp else 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = if (isUrdu) 19.sp else 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = if (isUrdu) 18.sp else 16.sp
        )
    )
}

val Typography = getAppTypography(AppLanguage.ENGLISH)
