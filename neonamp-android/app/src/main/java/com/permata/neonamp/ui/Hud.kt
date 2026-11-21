package com.permata.neonamp.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.sin

/** Bentuk panel ala armor robot: sudut kiri-atas & kanan-bawah terpotong. */
fun HudShape(cut: Dp) = CutCornerShape(topStart = cut, topEnd = 0.dp, bottomEnd = cut, bottomStart = 0.dp)

/** Heksagon untuk tombol utama (play). */
object HexShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width; val h = size.height
        val p = Path().apply {
            moveTo(w * 0.25f, 0f); lineTo(w * 0.75f, 0f); lineTo(w, h / 2)
            lineTo(w * 0.75f, h); lineTo(w * 0.25f, h); lineTo(0f, h / 2); close()
        }
        return Outline.Generic(p)
    }
}

/** Siku bingkai HUD di keempat sudut. */
fun Modifier.hudCorners(color: Color, len: Dp = 12.dp, stroke: Dp = 2.dp, inset: Dp = 0.dp) = drawWithContent {
    drawContent()
    val l = len.toPx(); val s = stroke.toPx(); val i = inset.toPx()
    val w = size.width; val h = size.height
    fun seg(a: Offset, b: Offset) = drawLine(color, a, b, s, cap = StrokeCap.Square)
    seg(Offset(i, i), Offset(i + l, i)); seg(Offset(i, i), Offset(i, i + l))
    seg(Offset(w - i, i), Offset(w - i - l, i)); seg(Offset(w - i, i), Offset(w - i, i + l))
    seg(Offset(i, h - i), Offset(i + l, h - i)); seg(Offset(i, h - i), Offset(i, h - i - l))
    seg(Offset(w - i, h - i), Offset(w - i - l, h - i)); seg(Offset(w - i, h - i), Offset(w - i, h - i - l))
}

/** Garis scan halus seperti layar monitor robot. */
fun Modifier.scanlines(color: Color = Color.White, alpha: Float = 0.045f, gap: Dp = 3.dp) = drawWithContent {
    drawContent()
    val g = gap.toPx()
    var y = 0f
    while (y < size.height) {
        drawLine(color.copy(alpha = alpha), Offset(0f, y), Offset(size.width, y), 1f)
        y += g
    }
}

/** Label sistem kecil, mis. "SYS://AUDIO". */
@Composable
fun HudLabel(text: String, color: Color = Neon.Dim, modifier: Modifier = Modifier) {
    Text(
        text, color = color, fontFamily = FontFamily.Monospace, fontSize = 10.sp,
        letterSpacing = 2.sp, fontWeight = FontWeight.Bold, modifier = modifier,
    )
}

/** Indikator status berkedip + teks, mis. "● ONLINE". */
@Composable
fun StatusDot(text: String, color: Color = Neon.Lime) {
    val t = rememberInfiniteTransition(label = "dot")
    val a by t.animateFloat(1f, 0.25f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(6.dp)) { drawCircle(color.copy(alpha = a)) }
        Spacer(Modifier.width(5.dp))
        HudLabel(text, color.copy(alpha = 0.85f))
    }
}

/**
 * Maskot kepala robot NeonAmp: headphone, antena berkedip, visor dengan mata yang berkedip,
 * dan mulut berupa equalizer yang bergerak saat musik diputar.
 */
@Composable
fun RobotHead(playing: Boolean, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "robot")
    val phase by tr.animateFloat(
        0f, 6.2832f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "ph"
    )
    val blink by tr.animateFloat(
        1f, 1f,
        infiniteRepeatable(keyframes {
            durationMillis = 3600
            1f at 0; 1f at 3300; 0.1f at 3420; 1f at 3560
        }), label = "blink"
    )
    val glow by tr.animateFloat(0.4f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "g")
    val c1 = Neon.Cyan; val c2 = Neon.Magenta; val c3 = Neon.Violet
    val panel = Neon.PanelHi; val lcd = Neon.Lcd

    Canvas(modifier) {
        val u = size.minDimension / 100f
        fun o(x: Float, y: Float) = Offset(x * u, y * u)
        fun sz(w: Float, h: Float) = Size(w * u, h * u)

        // Bando headphone
        drawArc(c3, 200f, 140f, false, o(14f, 14f), sz(72f, 66f), style = Stroke(5f * u, cap = StrokeCap.Round))
        // Antena + lampu
        drawLine(c1, o(50f, 22f), o(50f, 9f), 2.5f * u)
        drawCircle(c2.copy(alpha = glow), 4.5f * u, o(50f, 7f))
        drawCircle(c2.copy(alpha = 0.25f * glow), 8f * u, o(50f, 7f))
        // Kepala (armor bersudut)
        val head = Path().apply {
            moveTo(30f * u, 22f * u); lineTo(78f * u, 22f * u); lineTo(84f * u, 28f * u)
            lineTo(84f * u, 80f * u); lineTo(78f * u, 88f * u); lineTo(22f * u, 88f * u)
            lineTo(16f * u, 82f * u); lineTo(16f * u, 36f * u); close()
        }
        drawPath(head, panel)
        drawPath(head, Brush.linearGradient(listOf(c1, c3, c2), o(16f, 22f), o(84f, 88f)), style = Stroke(2.5f * u))
        // Earcup headphone
        drawRoundRect(c2, o(6f, 44f), sz(9f, 26f), CornerRadius(3f * u))
        drawRoundRect(c2, o(85f, 44f), sz(9f, 26f), CornerRadius(3f * u))
        // Visor
        drawRoundRect(lcd, o(23f, 34f), sz(54f, 22f), CornerRadius(4f * u))
        drawRoundRect(c1.copy(alpha = 0.6f), o(23f, 34f), sz(54f, 22f), CornerRadius(4f * u), style = Stroke(1.2f * u))
        // Mata (berkedip)
        val eh = 8f * blink
        drawRoundRect(c1, o(31f, 45f - eh / 2), sz(13f, eh), CornerRadius(2f * u))
        drawRoundRect(c1, o(56f, 45f - eh / 2), sz(13f, eh), CornerRadius(2f * u))
        drawRoundRect(c1.copy(alpha = 0.18f), o(27f, 39f), sz(46f, 12f), CornerRadius(4f * u))
        // Mulut equalizer
        for (i in 0 until 7) {
            val amp = if (playing) 0.3f + 0.7f * abs(sin(phase + i * 0.9f)) else 0.18f
            val h = 14f * amp
            val x = 29f + i * 6.4f
            val col = when { i < 2 -> c1; i < 5 -> c3; else -> c2 }
            drawRoundRect(col, o(x, 80f - h), sz(4f, h), CornerRadius(1f * u))
        }
    }
}

/** Chip filter bergaya HUD. */
@Composable
fun HudChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = HudShape(7.dp)
    Text(
        text,
        color = if (selected) Neon.Bg else Neon.Text,
        fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        maxLines = 1,
        modifier = modifier
            .clip(shape)
            .background(if (selected) Neon.accent else Brush.linearGradient(listOf(Neon.Panel, Neon.Panel)))
            .border(1.dp, if (selected) Color.Transparent else Neon.Line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
