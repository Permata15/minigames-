package com.permata.sinemabebas.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFFE50914)
val Bg = Color(0xFF0E0E12)
val Surface = Color(0xFF1A1A20)

@Composable
fun SinemaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Color.White,
            secondary = Color(0xFFFFB4A9),
            background = Bg,
            surface = Surface,
            surfaceVariant = Color(0xFF26262E),
            onBackground = Color(0xFFECECF1),
            onSurface = Color(0xFFECECF1),
        ),
        content = content,
    )
}
