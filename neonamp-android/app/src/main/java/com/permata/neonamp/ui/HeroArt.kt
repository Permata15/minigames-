package com.permata.neonamp.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Tema superhero. Lambangnya digambar sendiri (bukan logo resmi), terinspirasi gaya tiap pahlawan.
 */
enum class Hero { WEB, IRON, GAMMA, STAR, BAT, STEEL }

/** Lambang sesuai tema: lambang pahlawan untuk tema superhero, kepala robot untuk tema lainnya. */
@Composable
fun Mascot(playing: Boolean, modifier: Modifier = Modifier) {
    val hero = Neon.palette.hero
    if (hero == null) RobotHead(playing, modifier) else HeroEmblem(hero, playing, modifier)
}

@Composable
fun HeroEmblem(hero: Hero, playing: Boolean, modifier: Modifier = Modifier, palette: Palette = Neon.palette) {
    val t = rememberInfiniteTransition(label = "hero")
    val pulse by t.animateFloat(
        0.55f, 1f, infiniteRepeatable(tween(if (playing) 450 else 1400), RepeatMode.Reverse), label = "p"
    )
    Canvas(modifier) { drawEmblem(hero, palette, pulse) }
}

private fun DrawScope.p(u: Float, x: Float, y: Float) = Offset(x * u, y * u)

/** Gambar lambang dalam kotak 100×100 unit. */
fun DrawScope.drawEmblem(hero: Hero, pal: Palette, pulse: Float = 1f) {
    val u = size.minDimension / 100f
    val c = Offset(50f * u, 50f * u)
    when (hero) {
        Hero.WEB -> {
            // Lingkaran merah, jaring putih, laba-laba hitam di tengah
            drawCircle(pal.primary, 47f * u, c)
            drawCircle(pal.secondary, 47f * u, c, style = Stroke(3f * u))
            val web = Color.White.copy(alpha = 0.85f)
            for (i in 0 until 8) {
                val a = i * PI / 4
                drawLine(web, c, Offset(c.x + cos(a).toFloat() * 46f * u, c.y + sin(a).toFloat() * 46f * u), 1.4f * u)
            }
            for (r in listOf(14f, 26f, 38f)) {
                val path = Path()
                for (i in 0..8) {
                    val a = i * PI / 4
                    val pt = Offset(c.x + cos(a).toFloat() * r * u, c.y + sin(a).toFloat() * r * u)
                    if (i == 0) path.moveTo(pt.x, pt.y) else {
                        val mid = (i - 0.5) * PI / 4
                        val ctrl = Offset(c.x + cos(mid).toFloat() * r * 0.82f * u, c.y + sin(mid).toFloat() * r * 0.82f * u)
                        path.quadraticTo(ctrl.x, ctrl.y, pt.x, pt.y)
                    }
                }
                drawPath(path, web, style = Stroke(1.3f * u))
            }
            // laba-laba
            val body = Color(0xFF0A0A0F)
            for (s in listOf(-1f, 1f)) for ((i, dy) in listOf(-9f, -3f, 3f, 9f).withIndex()) {
                val kx = 50f + s * (14f + (if (i == 1 || i == 2) 4f else 0f))
                drawLine(body, p(u, 50f, 50f + dy * 0.4f), p(u, kx, 44f + dy), 2.2f * u, StrokeCap.Round)
                drawLine(body, p(u, kx, 44f + dy), p(u, kx + s * 6f, 50f + dy * 1.5f), 2.2f * u, StrokeCap.Round)
            }
            drawOval(body, p(u, 44f, 36f), Size(12f * u, 11f * u))
            drawOval(body, p(u, 42.5f, 44f), Size(15f * u, 22f * u))
        }
        Hero.IRON -> {
            // Reaktor: cincin emas, segmen biru menyala, inti berdenyut
            drawCircle(pal.secondary, 47f * u, c)
            drawCircle(pal.primary, 47f * u, c, style = Stroke(4f * u))
            drawCircle(Color(0xFF14181F), 36f * u, c)
            for (i in 0 until 10) {
                rotate(i * 36f, c) {
                    drawRoundRectCompat(pal.tertiary.copy(alpha = 0.55f + 0.45f * pulse), p(u, 46f, 17f), Size(8f * u, 13f * u))
                }
            }
            drawCircle(pal.tertiary.copy(alpha = 0.25f * pulse), 22f * u, c)
            drawCircle(Brush.radialGradient(listOf(Color.White, pal.tertiary, Color.Transparent), c, 20f * u), 20f * u, c)
            drawCircle(Color.White, 8f * u * (0.85f + 0.15f * pulse), c)
        }
        Hero.GAMMA -> {
            // Heksagon hijau retak dengan simbol radiasi
            val hex = Path().apply {
                for (i in 0..5) {
                    val a = PI / 6 + i * PI / 3
                    val x = c.x + cos(a).toFloat() * 47f * u; val y = c.y + sin(a).toFloat() * 47f * u
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(hex, Color(0xFF0E1A10))
            drawPath(hex, pal.primary, style = Stroke(4f * u))
            val glow = pal.primary.copy(alpha = 0.6f + 0.4f * pulse)
            for (i in 0 until 3) {
                drawArc(glow, -90f + i * 120f - 30f, 60f, true, p(u, 22f, 22f), Size(56f * u, 56f * u))
            }
            drawCircle(Color(0xFF0E1A10), 9f * u, c)
            drawCircle(glow, 6f * u, c)
            // retakan
            val crack = Path().apply {
                moveTo(10f * u, 30f * u); lineTo(22f * u, 36f * u); lineTo(18f * u, 44f * u); lineTo(30f * u, 52f * u)
                moveTo(90f * u, 64f * u); lineTo(78f * u, 62f * u); lineTo(82f * u, 72f * u); lineTo(70f * u, 76f * u)
            }
            drawPath(crack, pal.tertiary, style = Stroke(2.2f * u, cap = StrokeCap.Round))
        }
        Hero.STAR -> {
            // Perisai bersudut: bagian atas biru berbintang, garis merah-putih di bawah
            val shield = Path().apply {
                moveTo(10f * u, 8f * u); lineTo(90f * u, 8f * u); lineTo(90f * u, 50f * u)
                quadraticTo(90f * u, 82f * u, 50f * u, 96f * u); quadraticTo(10f * u, 82f * u, 10f * u, 50f * u); close()
            }
            clipPath(shield) {
                drawRect(Color.White)
                val stripes = listOf(pal.secondary, Color.White, pal.secondary, Color.White, pal.secondary)
                stripes.forEachIndexed { i, col -> drawRect(col, p(u, 10f + i * 16f, 40f), Size(16f * u, 60f * u)) }
                drawRect(pal.primary, p(u, 0f, 0f), Size(100f * u, 42f * u))
                drawStar(Color.White, Offset(50f * u, 25f * u), 13f * u * (0.92f + 0.08f * pulse))
            }
            drawPath(shield, pal.tertiary, style = Stroke(3f * u))
        }
        Hero.BAT -> {
            // Bulan kuning dengan siluet kelelawar terbang
            drawCircle(pal.primary.copy(alpha = 0.18f * pulse), 49f * u, c)
            drawCircle(pal.primary, 42f * u, c)
            drawPath(batPath(u, 50f, 52f, 80f), Color(0xFF07070A))
        }
        Hero.STEEL -> {
            // Lambang wajik merah-kuning dengan petir
            val diamond = Path().apply {
                moveTo(22f * u, 14f * u); lineTo(78f * u, 14f * u); lineTo(94f * u, 32f * u)
                lineTo(50f * u, 92f * u); lineTo(6f * u, 32f * u); close()
            }
            drawPath(diamond, pal.tertiary)
            drawPath(diamond, pal.secondary, style = Stroke(5f * u))
            val bolt = Path().apply {
                moveTo(56f * u, 20f * u); lineTo(32f * u, 52f * u); lineTo(48f * u, 52f * u)
                lineTo(40f * u, 82f * u); lineTo(70f * u, 42f * u); lineTo(53f * u, 42f * u); lineTo(62f * u, 20f * u); close()
            }
            drawPath(bolt, pal.secondary)
            drawPath(bolt, Color.White.copy(alpha = 0.35f * pulse), style = Stroke(1.5f * u))
        }
    }
}

private fun DrawScope.drawRoundRectCompat(color: Color, topLeft: Offset, size: Size) =
    drawRoundRect(color, topLeft, size, androidx.compose.ui.geometry.CornerRadius(size.width / 3))

private fun DrawScope.drawStar(color: Color, center: Offset, r: Float) {
    val path = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.42f
        val a = -PI / 2 + i * PI / 5
        val x = center.x + cos(a).toFloat() * rad; val y = center.y + sin(a).toFloat() * rad
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/** Siluet kelelawar terbang (bentuk sendiri). w = lebar sayap dalam unit. */
private fun batPath(u: Float, cx: Float, cy: Float, w: Float): Path {
    val s = w / 80f
    fun x(v: Float) = (cx + v * s) * u
    fun y(v: Float) = (cy + v * s) * u
    return Path().apply {
        moveTo(x(0f), y(-6f))
        lineTo(x(-3f), y(-11f)); lineTo(x(-4f), y(-5f))             // telinga kiri
        quadraticTo(x(-14f), y(-12f), x(-40f), y(-10f))              // sayap atas kiri
        quadraticTo(x(-30f), y(-4f), x(-32f), y(6f))                 // ujung sayap
        quadraticTo(x(-25f), y(0f), x(-19f), y(5f))                  // lekuk 1
        quadraticTo(x(-14f), y(0f), x(-9f), y(8f))                   // lekuk 2
        quadraticTo(x(-4f), y(3f), x(0f), y(13f))                    // ekor
        quadraticTo(x(4f), y(3f), x(9f), y(8f))
        quadraticTo(x(14f), y(0f), x(19f), y(5f))
        quadraticTo(x(25f), y(0f), x(32f), y(6f))
        quadraticTo(x(30f), y(-4f), x(40f), y(-10f))
        quadraticTo(x(14f), y(-12f), x(4f), y(-5f))
        lineTo(x(3f), y(-11f)); close()
    }
}

/** Pola latar halus khas tiap pahlawan, digambar di belakang layar (hanya tema superhero). */
fun Modifier.heroBackdrop() = drawBehind {
    val pal = Neon.palette
    if (pal.hero != null) drawHeroBackdrop(pal)
}

fun DrawScope.drawHeroBackdrop(pal: Palette) {
    val hero = pal.hero ?: return
    run {
        val w = size.width; val h = size.height
        val line = pal.primary.copy(alpha = 0.09f)
        when (hero) {
            Hero.WEB -> {
                // jaring dari sudut kanan atas
                val o = Offset(w, 0f)
                val maxR = maxOf(w, h) * 1.2f
                for (i in 0..9) {
                    val a = PI / 2 + i * (PI / 2) / 9
                    drawLine(line, o, Offset(o.x + cos(a).toFloat() * maxR, o.y + sin(a).toFloat() * maxR), 2f)
                }
                var r = 90f
                while (r < maxR) {
                    drawArc(line, 90f, 90f, false, Offset(o.x - r, o.y - r), Size(r * 2, r * 2), style = Stroke(2f))
                    r *= 1.45f
                }
            }
            Hero.IRON -> {
                // pelat armor heksagonal
                val s = 46f
                val dx = s * 1.732f
                var row = 0
                var yy = 0f
                while (yy < h + s) {
                    var xx = if (row % 2 == 0) 0f else dx / 2
                    while (xx < w + dx) {
                        val path = Path()
                        for (i in 0..5) {
                            val a = PI / 6 + i * PI / 3
                            val px = xx + cos(a).toFloat() * s; val py = yy + sin(a).toFloat() * s
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        drawPath(path, line, style = Stroke(2f))
                        xx += dx
                    }
                    yy += s * 1.5f; row++
                }
                drawCircle(Brush.radialGradient(listOf(pal.tertiary.copy(alpha = 0.12f), Color.Transparent), Offset(w / 2, h * 0.35f), w * 0.7f), w * 0.7f, Offset(w / 2, h * 0.35f))
            }
            Hero.GAMMA -> {
                // retakan tanah + cahaya gamma
                val rnd = Random(7)
                repeat(9) {
                    val path = Path()
                    var x = rnd.nextFloat() * w; var y = rnd.nextFloat() * h
                    path.moveTo(x, y)
                    repeat(6) {
                        x += (rnd.nextFloat() - 0.5f) * 160f; y += (rnd.nextFloat() - 0.3f) * 120f
                        path.lineTo(x, y)
                    }
                    drawPath(path, pal.primary.copy(alpha = 0.12f), style = Stroke(3f))
                }
                drawCircle(Brush.radialGradient(listOf(pal.primary.copy(alpha = 0.10f), Color.Transparent), Offset(w * 0.2f, h), w), w, Offset(w * 0.2f, h))
            }
            Hero.STAR -> {
                // garis diagonal + taburan bintang
                var x = -h
                while (x < w) {
                    drawLine(pal.secondary.copy(alpha = 0.06f), Offset(x, h), Offset(x + h, 0f), 18f)
                    x += 90f
                }
                val rnd = Random(3)
                repeat(22) { drawStar(Color.White.copy(alpha = 0.07f), Offset(rnd.nextFloat() * w, rnd.nextFloat() * h), 10f + rnd.nextFloat() * 14f) }
            }
            Hero.BAT -> {
                // cahaya bulan di atas, siluet kota di bawah, hujan tipis
                drawCircle(Brush.radialGradient(listOf(pal.primary.copy(alpha = 0.14f), Color.Transparent), Offset(w * 0.8f, h * 0.08f), w * 0.6f), w * 0.6f, Offset(w * 0.8f, h * 0.08f))
                val rnd = Random(11)
                repeat(70) {
                    val x = rnd.nextFloat() * w; val y = rnd.nextFloat() * h
                    drawLine(Color.White.copy(alpha = 0.05f), Offset(x, y), Offset(x - 8f, y + 34f), 1.5f)
                }
                var bx = 0f
                val city = Path().apply { moveTo(0f, h) }
                while (bx < w) {
                    val bw = 40f + rnd.nextFloat() * 70f
                    val bh = h * (0.08f + rnd.nextFloat() * 0.16f)
                    city.lineTo(bx, h - bh); city.lineTo(bx + bw, h - bh)
                    bx += bw
                }
                city.lineTo(w, h); city.close()
                drawPath(city, pal.primary.copy(alpha = 0.05f))
            }
            Hero.STEEL -> {
                // sinar dari atas tengah
                val o = Offset(w / 2, -h * 0.15f)
                for (i in 0..22) {
                    val a = PI * 0.15 + i * (PI * 0.7) / 22
                    drawLine(
                        (if (i % 2 == 0) pal.primary else pal.secondary).copy(alpha = 0.07f),
                        o, Offset(o.x + cos(a).toFloat() * h * 2, o.y + sin(a).toFloat() * h * 2), 10f,
                    )
                }
            }
        }
    }
}
