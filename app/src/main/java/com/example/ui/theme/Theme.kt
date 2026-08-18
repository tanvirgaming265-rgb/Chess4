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

private val DarkColorScheme = darkColorScheme(
  primary = ChessGold,
  onPrimary = Color(0xFF1E1B4B),
  primaryContainer = Color(0xFF334155),
  onPrimaryContainer = ChessGoldLight,
  secondary = ChessEmerald,
  onSecondary = Color.White,
  background = Color(0xFF090D16),
  surface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFF1E293B),
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFFB45309),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFEF3C7),
  onPrimaryContainer = Color(0xFF78350F),
  secondary = Color(0xFF059669),
  onSecondary = Color.White,
  background = Color(0xFFF8FAFC),
  surface = Color(0xFFFFFFFF),
  surfaceVariant = Color(0xFFE2E8F0),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek grandmaster dark theme for premier chess look
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
