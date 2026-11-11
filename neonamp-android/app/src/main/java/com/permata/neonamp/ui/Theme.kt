package com.permata.neonamp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Neon {
    val Bg = Color(0xFF07080F)
    val Panel = Color(0xFF10121F)
    val PanelHi = Color(0xFF181B2E)
    val Line = Color(0xFF262A45)
    val Cyan = Color(0xFF00F0FF)
    val Magenta = Color(0xFFFF2BD6)
    val Violet = Color(0xFFB14CFF)
    val Lime = Color(0xFF7CFF6B)
    val Text = Color(0xFFE6F1FF)
    val Dim = Color(0xFF7A86A8)

    val accent = Brush.horizontalGradient(listOf(Cyan, Violet, Magenta))
    val panelGlow = Brush.verticalGradient(listOf(PanelHi, Panel))
}

@Composable
fun NeonAmpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Neon.Cyan,
            secondary = Neon.Magenta,
            tertiary = Neon.Violet,
            background = Neon.Bg,
            surface = Neon.Panel,
            surfaceVariant = Neon.PanelHi,
            onPrimary = Neon.Bg,
            onBackground = Neon.Text,
            onSurface = Neon.Text,
            onSurfaceVariant = Neon.Dim,
            outline = Neon.Line,
        ),
        content = content,
    )
}
