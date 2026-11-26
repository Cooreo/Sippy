package com.sippy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class FriendColors(
    val primary: Color,
    val secondary: Color,
    val light: Color,
    val ink: Color,
    val glow: Color
)

val AquaColors = FriendColors(
    Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFE0F2FE), Color(0xFF0C4A6E), Color(0xFF7DD3FC)
)
val CoralColors = FriendColors(
    Color(0xFFEF4444), Color(0xFFFB923C), Color(0xFFFFEDD5), Color(0xFF7F1D1D), Color(0xFFFCA5A5)
)
val VioletColors = FriendColors(
    Color(0xFF7C3AED), Color(0xFFC084FC), Color(0xFFEDE9FE), Color(0xFF4C1D95), Color(0xFFD8B4FE)
)
val MintColors = FriendColors(
    Color(0xFF059669), Color(0xFF34D399), Color(0xFFD1FAE5), Color(0xFF064E3B), Color(0xFF6EE7B7)
)

val LocalFriendColors = compositionLocalOf { AquaColors }

object Accents {
    val names = listOf("aqua", "coral", "violet", "mint")
    val labels = listOf("Aqua", "Coral", "Violet", "Mint")
    fun colors(name: String) = when (name) {
        "coral" -> CoralColors
        "violet" -> VioletColors
        "mint" -> MintColors
        else -> AquaColors
    }
    fun scheme(name: String, dark: Boolean): ColorScheme {
        val c = colors(name)
        return if (dark) darkColorScheme(
            primary = c.secondary,
            onPrimary = Color(0xFF0B1220),
            primaryContainer = c.primary.copy(alpha = 0.55f),
            onPrimaryContainer = Color.White,
            secondary = c.glow,
            tertiary = c.light,
            background = Color(0xFF0B1220),
            surface = Color(0xFF111C2E),
            surfaceVariant = Color(0xFF1D2A3D),
            onSurface = Color(0xFFE8F1FA)
        ) else lightColorScheme(
            primary = c.primary,
            onPrimary = Color.White,
            primaryContainer = c.light,
            onPrimaryContainer = c.ink,
            secondary = c.secondary,
            tertiary = c.glow,
            background = Color(0xFFF7FBFF),
            surface = Color.White,
            surfaceVariant = c.light,
            onSurface = Color(0xFF10243A)
        )
    }
}

@Composable
fun SippyTheme(mode: String = "system", accent: String = "aqua", content: @Composable () -> Unit) {
    val dark = when (mode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalFriendColors provides Accents.colors(accent)) {
        MaterialTheme(colorScheme = Accents.scheme(accent, dark), content = content)
    }
}
