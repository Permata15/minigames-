package com.permata.neonamp.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val s = ms / 1000
    val h = s / 3600
    return if (h > 0) "%d:%02d:%02d".format(h, (s % 3600) / 60, s % 60)
    else "%02d:%02d".format(s / 60, s % 60)
}

/**
 * Spektrum bar ala Winamp. Bersifat dekoratif (animasi mengikuti status play),
 * sehingga tidak butuh izin mikrofon.
 */
@Composable
fun SpectrumBars(playing: Boolean, modifier: Modifier = Modifier, bars: Int = 28) {
    val levels = remember { FloatArray(bars) }
    val peaks = remember { FloatArray(bars) }
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(playing) {
        var t = 0.0
        while (true) {
            withFrameNanos { }
            t += 0.06
            for (i in 0 until bars) {
                val target = if (playing) {
                    val base = 0.35 + 0.35 * sin(t * (1.3 + i * 0.17) + i) * sin(t * 0.7 + i * 0.5)
                    val bassBoost = if (i < bars / 4) 0.25 else 0.0
                    (abs(base) + bassBoost + Random.nextDouble(0.0, 0.3)).toFloat().coerceIn(0.05f, 1f)
                } else 0.03f
                levels[i] += (target - levels[i]) * if (playing) 0.35f else 0.12f
                peaks[i] = if (levels[i] > peaks[i]) levels[i] else (peaks[i] - 0.012f).coerceAtLeast(levels[i])
            }
            tick++
        }
    }
    val brush = Brush.verticalGradient(listOf(Neon.Magenta, Neon.Violet, Neon.Cyan))
    Canvas(modifier) {
        if (tick < 0) return@Canvas // membaca state agar Canvas digambar ulang tiap frame
        val gap = 3.dp.toPx()
        val w = (size.width - gap * (bars - 1)) / bars
        for (i in 0 until bars) {
            val x = i * (w + gap)
            val h = size.height * levels[i]
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, size.height - h),
                size = Size(w, h),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
            val py = size.height - size.height * peaks[i] - 3.dp.toPx()
            drawRect(Neon.Text.copy(alpha = 0.85f), Offset(x, py), Size(w, 2.dp.toPx()))
        }
    }
}

/** Garis gelombang kecil di mini player. */
@Composable
fun MiniWave(playing: Boolean, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "wave")
    val phase by tr.animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "p"
    )
    Canvas(modifier) {
        val n = 5
        val w = size.width / (n * 2 - 1)
        for (i in 0 until n) {
            val amp = if (playing) (0.35f + 0.65f * abs(sin(phase + i * 0.9f))) else 0.2f
            val h = size.height * amp
            drawRoundRect(
                Neon.Cyan, Offset(i * 2 * w, (size.height - h) / 2), Size(w, h), CornerRadius(w / 2)
            )
        }
    }
}

@Composable
fun Artwork(url: String?, size: Dp, modifier: Modifier = Modifier, corner: Dp = 12.dp) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(listOf(Neon.PanelHi, Neon.Panel)))
            .border(1.dp, Neon.Line, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.MusicNote, null, tint = Neon.Dim)
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

val GlowCyan = Color(0x5500F0FF)
