package com.turjaun.cookiesuploader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern Dark Color Scheme - Flutter-like depth
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3b82f6),        // Bright blue
    secondary = Color(0xFF22c55e),      // Success green
    tertiary = Color(0xFFf97316),       // Accent orange
    background = Color(0xFF0f172a),     // Deep navy background
    surface = Color(0xFF1e293b),        // Card surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFf1f5f9),
    onSurface = Color(0xFFf1f5f9),
    error = Color(0xFFef4444)
)

// Modern color extensions
object ModernColors {
    val surfaceVariant = Color(0xFF334155)
    val surfaceElevated = Color(0xFF475569)
    val textPrimary = Color(0xFFf8fafc)
    val textSecondary = Color(0xFF94a3b8)
    val textDisabled = Color(0xFF64748b)
    val border = Color(0xFF334155)
    val borderFocused = Color(0xFF3b82f6)
    val success = Color(0xFF22c55e)
    val warning = Color(0xFFfbbf24)
    val error = Color(0xFFef4444)
    val info = Color(0xFF22d3ee)
    
    // Gradient backgrounds
    val gradientStart = Color(0xFF0f172a)
    val gradientEnd = Color(0xFF1e293b)
}

@Composable
fun CookiesUploaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = androidx.compose.material3.Typography(
            // Add custom typography if needed
        ),
        content = content
    )
}