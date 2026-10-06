package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SamsungDarkColorScheme =
  darkColorScheme(
    primary = SamsungGreenAccent,
    onPrimary = Color.White,
    primaryContainer = SamsungGreenContainer,
    onPrimaryContainer = SamsungGreenAccent,
    secondary = SamsungGreenAccent,
    onSecondary = Color.White,
    secondaryContainer = SamsungSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = SamsungOrangeAccent,
    background = SamsungDarkBackground,
    onBackground = SamsungTextPrimary,
    surface = SamsungSurface,
    onSurface = SamsungTextPrimary,
    surfaceVariant = SamsungSurfaceVariant,
    onSurfaceVariant = SamsungTextSecondary,
    outline = SamsungDivider,
    outlineVariant = SamsungDivider
  )

private val SamsungLightColorScheme =
  lightColorScheme(
    primary = SamsungGreenAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F5E2),
    onPrimaryContainer = Color(0xFF0F4D25),
    secondary = SamsungGreenAccent,
    onSecondary = Color.White,
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFEBEBF0),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFFE5E5EA),
    outlineVariant = Color(0xFFE5E5EA)
  )

@Composable
fun RotaryMeasureTheme(
  darkTheme: Boolean = true, // Samsung Calculator aesthetic is iconic in pure dark mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) SamsungDarkColorScheme else SamsungLightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
