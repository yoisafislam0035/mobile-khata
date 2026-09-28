package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.util.AppLanguage

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    CUSTOM
}

@Composable
fun MobiKhataTheme(
    language: AppLanguage = AppLanguage.ENGLISH,
    themeMode: ThemeMode = ThemeMode.LIGHT,
    customPrimaryColor: Color = EmeraldPrimary,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.CUSTOM -> false // Custom color with clean high-contrast light theme
        ThemeMode.SYSTEM -> systemInDark
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode == ThemeMode.SYSTEM -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> {
            darkColorScheme(
                primary = if (themeMode == ThemeMode.CUSTOM) customPrimaryColor else Color(0xFF62DCA8),
                onPrimary = Color(0xFF003825),
                primaryContainer = Color(0xFF005137),
                onPrimaryContainer = Color(0xFF80F9C3),
                secondary = AmberSecondary,
                onSecondary = Color(0xFF451A03),
                secondaryContainer = AmberSecondaryContainer,
                onSecondaryContainer = AmberOnSecondaryContainer,
                background = SurfaceDark,
                surface = SurfaceDark,
                surfaceVariant = SurfaceContainerDark,
                onBackground = OnSurfaceDark,
                onSurface = OnSurfaceDark,
                outline = Color(0xFF8A938D)
            )
        }
        else -> {
            val primary = if (themeMode == ThemeMode.CUSTOM) customPrimaryColor else EmeraldPrimary
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.15f),
                onPrimaryContainer = primary,
                secondary = AmberSecondary,
                onSecondary = Color.White,
                secondaryContainer = AmberSecondaryContainer,
                onSecondaryContainer = AmberOnSecondaryContainer,
                background = SurfaceLight,
                surface = Color.White,
                surfaceVariant = SurfaceContainerLight,
                onBackground = OnSurfaceLight,
                onSurface = OnSurfaceLight,
                outline = Color(0xFF707974)
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getAppTypography(language),
        content = content
    )
}
