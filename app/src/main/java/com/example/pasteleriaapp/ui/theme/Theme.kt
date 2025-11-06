package com.example.pasteleriaapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.pasteleriaapp.ui.theme.Typography // Added this import

private val PasteleriaColorScheme = lightColorScheme(
    primary = SaddleBrown,       // Main accent for buttons, borders
    onPrimary = Color.White,         // Text on primary color
    secondary = PastelPink,      // Secondary accent
    onSecondary = DarkBrown,       // Text on secondary color
    background = Cream,          // App background
    onBackground = DarkBrown,      // Main text color
    surface = Cream,             // Card backgrounds, surfaces
    onSurface = DarkBrown,         // Text on surfaces
    tertiary = DarkBrown         // Other accents
)

@Composable
fun PasteleriaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // We can ignore dark theme for now to keep the brand consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PasteleriaColorScheme,
        typography = Typography,
        content = content
    )
}