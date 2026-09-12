package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    OBSIDIAN, VELVET_NOIR, EMPIRE, CARBON, WARM_PAPER
}

private val ObsidianColorScheme = darkColorScheme(
    primary = ObsidianPrimary,
    background = ObsidianBackground,
    surface = ObsidianSurface,
    onPrimary = Color.White,
    onBackground = ObsidianText,
    onSurface = ObsidianText
)

private val VelvetNoirColorScheme = darkColorScheme(
    primary = VelvetNoirPrimary,
    background = VelvetNoirBackground,
    surface = VelvetNoirSurface,
    onPrimary = Color.Black,
    onBackground = VelvetNoirText,
    onSurface = VelvetNoirText
)

private val EmpireColorScheme = darkColorScheme(
    primary = EmpirePrimary,
    background = EmpireBackground,
    surface = EmpireSurface,
    onPrimary = Color.Black,
    onBackground = EmpireText,
    onSurface = EmpireText
)

private val CarbonColorScheme = darkColorScheme(
    primary = CarbonPrimary,
    background = CarbonBackground,
    surface = CarbonSurface,
    onPrimary = Color.Black,
    onBackground = CarbonText,
    onSurface = CarbonText
)

private val WarmPaperColorScheme = darkColorScheme(
    primary = WarmPaperPrimary,
    background = WarmPaperBackground,
    surface = WarmPaperSurface,
    onPrimary = Color(0xFF1E1A16),
    onBackground = WarmPaperText,
    onSurface = WarmPaperText,
    primaryContainer = Color(0xFF3B322A),
    onPrimaryContainer = WarmPaperPrimary
)

private val ObsidianLightColorScheme = lightColorScheme(
    primary = ObsidianLightPrimary,
    background = ObsidianLightBackground,
    surface = ObsidianLightSurface,
    onPrimary = Color.White,
    onBackground = ObsidianLightText,
    onSurface = ObsidianLightText
)

private val VelvetNoirLightColorScheme = lightColorScheme(
    primary = VelvetNoirLightPrimary,
    background = VelvetNoirLightBackground,
    surface = VelvetNoirLightSurface,
    onPrimary = Color.White,
    onBackground = VelvetNoirLightText,
    onSurface = VelvetNoirLightText
)

private val EmpireLightColorScheme = lightColorScheme(
    primary = EmpireLightPrimary,
    background = EmpireLightBackground,
    surface = EmpireLightSurface,
    onPrimary = Color.White,
    onBackground = EmpireLightText,
    onSurface = EmpireLightText
)

private val CarbonLightColorScheme = lightColorScheme(
    primary = CarbonLightPrimary,
    background = CarbonLightBackground,
    surface = CarbonLightSurface,
    onPrimary = Color.White,
    onBackground = CarbonLightText,
    onSurface = CarbonLightText
)

private val WarmPaperLightColorScheme = lightColorScheme(
    primary = WarmPaperLightPrimary,
    background = WarmPaperLightBackground,
    surface = WarmPaperLightSurface,
    onPrimary = Color.White,
    onBackground = WarmPaperLightText,
    onSurface = WarmPaperLightText,
    primaryContainer = Color(0xFFF1E3D3),
    onPrimaryContainer = WarmPaperLightPrimary
)

@Composable
fun WorksheetStudioTheme(
    themeStyle: AppTheme = AppTheme.OBSIDIAN,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        isDarkMode -> when (themeStyle) {
            AppTheme.OBSIDIAN -> ObsidianColorScheme
            AppTheme.VELVET_NOIR -> VelvetNoirColorScheme
            AppTheme.EMPIRE -> EmpireColorScheme
            AppTheme.CARBON -> CarbonColorScheme
            AppTheme.WARM_PAPER -> WarmPaperColorScheme
        }
        else -> when (themeStyle) {
            AppTheme.OBSIDIAN -> ObsidianLightColorScheme
            AppTheme.VELVET_NOIR -> VelvetNoirLightColorScheme
            AppTheme.EMPIRE -> EmpireLightColorScheme
            AppTheme.CARBON -> CarbonLightColorScheme
            AppTheme.WARM_PAPER -> WarmPaperLightColorScheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
