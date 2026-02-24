package com.turjaun.cookiesuploader.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3b82f6),
    secondary = Color(0xFF22c55e),
    tertiary = Color(0xFFf97316),
    background = Color(0xFF0f172a),
    surface = Color(0xFF1e293b),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFf1f5f9),
    onSurface = Color(0xFFf1f5f9),
    error = Color(0xFFef4444),
    surfaceVariant = Color(0xFF334155),
    outline = Color(0xFF475569)
)

// Simple color helpers - accessible everywhere
object AppColors {
    val primary = DarkColorScheme.primary
    val surface = DarkColorScheme.surface
    val background = DarkColorScheme.background
    val onSurface = DarkColorScheme.onSurface
    val success = Color(0xFF22c55e)
    val error = Color(0xFFef4444)
    val warning = Color(0xFFfbbf24)
    val info = Color(0xFF22d3ee)
    val textPrimary = Color(0xFFf8fafc)
    val textSecondary = Color(0xFF94a3b8)
}