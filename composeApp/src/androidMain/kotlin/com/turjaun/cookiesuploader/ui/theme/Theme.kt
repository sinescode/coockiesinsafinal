package com.turjaun.cookiesuploader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF22c55e),
    secondary = Color(0xFFf97316),
    tertiary = Color(0xFF3b82f6),
    background = Color(0xFF0f172a),
    surface = Color(0xFF111827),
    onPrimary = Color(0xFF0b1220),
    onSecondary = Color(0xFF0b1220),
    onBackground = Color(0xFFe5e7eb),
    onSurface = Color(0xFFe5e7eb),
    surfaceVariant = Color(0xFF1f2937),
    outline = Color(0xFF334155)
)

@Composable
fun CookiesUploaderTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}