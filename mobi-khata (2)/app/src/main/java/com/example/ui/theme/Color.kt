package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Mobi Khata Professional Brand Accent Palettes
val EmeraldPrimary = Color(0xFF0D5C46)
val EmeraldOnPrimary = Color(0xFFFFFFFF)
val EmeraldPrimaryContainer = Color(0xFFD0F0E4)
val EmeraldOnPrimaryContainer = Color(0xFF002016)

val AmberSecondary = Color(0xFFB45309)
val AmberSecondaryContainer = Color(0xFFFDE68A)
val AmberOnSecondaryContainer = Color(0xFF451A03)

val SurfaceLight = Color(0xFFF8FAF9)
val SurfaceContainerLight = Color(0xFFEFF4F1)
val OnSurfaceLight = Color(0xFF191C1B)

val SurfaceDark = Color(0xFF111A16)
val SurfaceContainerDark = Color(0xFF1A2621)
val OnSurfaceDark = Color(0xFFE1E3DF)

// Khata Status Colors (Standard financial convention)
val UdhaarGreen = Color(0xFF059669)
val UdhaarGreenBg = Color(0xFFECFDF5)
val JamaaRed = Color(0xFFDC2626)
val JamaaRedBg = Color(0xFFFEF2F2)
val GoldAccent = Color(0xFFD97706)
val BlueInfo = Color(0xFF0284C7)
val BlueInfoBg = Color(0xFFF0F9FF)

// Custom Accent Color Presets
data class PresetThemeColor(
    val name: String,
    val color: Color,
    val hexCode: Long
)

val PRESET_ACCENT_COLORS = listOf(
    PresetThemeColor("Emerald Green", Color(0xFF0D5C46), 0xFF0D5C46),
    PresetThemeColor("Royal Blue", Color(0xFF1E40AF), 0xFF1E40AF),
    PresetThemeColor("Deep Teal", Color(0xFF0F766E), 0xFF0F766E),
    PresetThemeColor("Indigo", Color(0xFF4F46E5), 0xFF4F46E5),
    PresetThemeColor("Maroon Red", Color(0xFF991B1B), 0xFF991B1B),
    PresetThemeColor("Amber Bronze", Color(0xFFB45309), 0xFFB45309),
    PresetThemeColor("Dark Slate", Color(0xFF334155), 0xFF334155)
)
