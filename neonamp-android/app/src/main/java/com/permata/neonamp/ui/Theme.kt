package com.permata.neonamp.ui

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Satu set warna tema. primary/secondary/tertiary = tiga warna gradasi utama. */
data class Palette(
    val key: String,
    val name: String,
    val tagline: String,
    val bg: Color,
    val bgTop: Color,
    val panel: Color,
    val panelHi: Color,
    val line: Color,
    val lcd: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val ok: Color,
    val text: Color,
    val dim: Color,
    /** Bila diisi, tema ini bergaya superhero (lambang & pola latar khusus). */
    val hero: Hero? = null,
)

val Palettes = listOf(
    // ---------- Tema superhero (lambang orisinal, terinspirasi) ----------
    Palette(
        "web", "Web Slinger", "Terinspirasi Spider-Man · merah & biru jaring",
        bg = Color(0xFF0B0A1A), bgTop = Color(0xFF2A0B16), panel = Color(0xFF15132A),
        panelHi = Color(0xFF1E1B3A), line = Color(0xFF33305A), lcd = Color(0xFF07061A),
        primary = Color(0xFFFF2D3B), secondary = Color(0xFF2E6BFF), tertiary = Color(0xFFFF7A8A),
        ok = Color(0xFF7FD8FF), text = Color(0xFFF2F4FF), dim = Color(0xFF9096B8), hero = Hero.WEB,
    ),
    Palette(
        "iron", "Iron Armor", "Terinspirasi Iron Man · merah, emas & reaktor",
        bg = Color(0xFF110708), bgTop = Color(0xFF3A0B0B), panel = Color(0xFF1C0E0F),
        panelHi = Color(0xFF2A1416), line = Color(0xFF4A2326), lcd = Color(0xFF0A0405),
        primary = Color(0xFFFFC53D), secondary = Color(0xFFD62828), tertiary = Color(0xFF7DF9FF),
        ok = Color(0xFF7DF9FF), text = Color(0xFFFFF4E6), dim = Color(0xFFB8968C), hero = Hero.IRON,
    ),
    Palette(
        "gamma", "Gamma Giant", "Terinspirasi Hulk · hijau gamma & ungu",
        bg = Color(0xFF06100A), bgTop = Color(0xFF0E2A12), panel = Color(0xFF0E1A12),
        panelHi = Color(0xFF15261A), line = Color(0xFF244030), lcd = Color(0xFF040B06),
        primary = Color(0xFF5CFF3B), secondary = Color(0xFF9B4DFF), tertiary = Color(0xFFB6FF00),
        ok = Color(0xFFB6FF00), text = Color(0xFFEFFFEA), dim = Color(0xFF86A88C), hero = Hero.GAMMA,
    ),
    Palette(
        "star", "Star Captain", "Terinspirasi Captain America · biru, merah & bintang",
        bg = Color(0xFF070B18), bgTop = Color(0xFF0E1A3D), panel = Color(0xFF0F1630),
        panelHi = Color(0xFF17213F), line = Color(0xFF2A3863), lcd = Color(0xFF050817),
        primary = Color(0xFF4D8BFF), secondary = Color(0xFFE63946), tertiary = Color(0xFFF1F5FF),
        ok = Color(0xFFF1F5FF), text = Color(0xFFF1F5FF), dim = Color(0xFF8E9BC0), hero = Hero.STAR,
    ),
    Palette(
        "bat", "Dark Knight", "Terinspirasi Batman · hitam pekat & cahaya bulan",
        bg = Color(0xFF060608), bgTop = Color(0xFF15151C), panel = Color(0xFF101014),
        panelHi = Color(0xFF18181F), line = Color(0xFF2A2A33), lcd = Color(0xFF030304),
        primary = Color(0xFFFFD21F), secondary = Color(0xFF8A96A8), tertiary = Color(0xFFFFE680),
        ok = Color(0xFFFFD21F), text = Color(0xFFECEEF2), dim = Color(0xFF7F8592), hero = Hero.BAT,
    ),
    Palette(
        "steel", "Steel Hero", "Terinspirasi Superman · biru, merah & kuning",
        bg = Color(0xFF060A1A), bgTop = Color(0xFF0B1C4A), panel = Color(0xFF0C1430),
        panelHi = Color(0xFF13204A), line = Color(0xFF243670), lcd = Color(0xFF040817),
        primary = Color(0xFF2F7BFF), secondary = Color(0xFFE8202A), tertiary = Color(0xFFFFD500),
        ok = Color(0xFFFFD500), text = Color(0xFFF0F4FF), dim = Color(0xFF8C9AC4), hero = Hero.STEEL,
    ),
    // ---------- Tema robot / neon ----------
    Palette(
        "mecha", "Mecha Unit", "Armor baja & visor oranye robot",
        bg = Color(0xFF0A0C0F), bgTop = Color(0xFF1A1F26), panel = Color(0xFF13171C),
        panelHi = Color(0xFF1C2229), line = Color(0xFF2E3640), lcd = Color(0xFF06080A),
        primary = Color(0xFFFF7A1A), secondary = Color(0xFFFF2E4D), tertiary = Color(0xFFFFC400),
        ok = Color(0xFF39FF88), text = Color(0xFFE8EDF2), dim = Color(0xFF8C97A3),
    ),
    Palette(
        "neon", "Neon Cyber", "Cyan & magenta futuristik",
        bg = Color(0xFF07080F), bgTop = Color(0xFF0D0B22), panel = Color(0xFF10121F),
        panelHi = Color(0xFF181B2E), line = Color(0xFF262A45), lcd = Color(0xFF05060C),
        primary = Color(0xFF00F0FF), secondary = Color(0xFFFF2BD6), tertiary = Color(0xFFB14CFF),
        ok = Color(0xFF7CFF6B), text = Color(0xFFE6F1FF), dim = Color(0xFF7A86A8),
    ),
    Palette(
        "synthwave", "Synthwave Sunset", "Senja retro 80-an",
        bg = Color(0xFF120818), bgTop = Color(0xFF2A0B2E), panel = Color(0xFF1E0F27),
        panelHi = Color(0xFF2A1535), line = Color(0xFF3D2350), lcd = Color(0xFF0B0410),
        primary = Color(0xFFFF9E3D), secondary = Color(0xFFFF3D81), tertiary = Color(0xFF9D4DFF),
        ok = Color(0xFFFFE14D), text = Color(0xFFFFEFF6), dim = Color(0xFFA88AA6),
    ),
    Palette(
        "aurora", "Aurora", "Hijau-biru cahaya kutub",
        bg = Color(0xFF051012), bgTop = Color(0xFF08221F), panel = Color(0xFF0B1B1E),
        panelHi = Color(0xFF12282C), line = Color(0xFF1E3D42), lcd = Color(0xFF030A0B),
        primary = Color(0xFF3DFFB4), secondary = Color(0xFF4DA3FF), tertiary = Color(0xFF9B7BFF),
        ok = Color(0xFFC6FF4D), text = Color(0xFFE6FFF8), dim = Color(0xFF7FA5A0),
    ),
    Palette(
        "classic", "Winamp Classic", "LCD hijau & amber klasik",
        bg = Color(0xFF16161F), bgTop = Color(0xFF23233A), panel = Color(0xFF232336),
        panelHi = Color(0xFF2E2E48), line = Color(0xFF45456A), lcd = Color(0xFF000000),
        primary = Color(0xFF00E639), secondary = Color(0xFFFFB000), tertiary = Color(0xFF7FE0FF),
        ok = Color(0xFF00FF66), text = Color(0xFFE8E8F0), dim = Color(0xFF8A8AA8),
    ),
    Palette(
        "sakura", "Sakura Night", "Pink lembut & lavender",
        bg = Color(0xFF140C12), bgTop = Color(0xFF2B1222), panel = Color(0xFF21141D),
        panelHi = Color(0xFF2D1B28), line = Color(0xFF44283B), lcd = Color(0xFF0B0609),
        primary = Color(0xFFFF8FC7), secondary = Color(0xFFFFB38A), tertiary = Color(0xFFC7A3FF),
        ok = Color(0xFF9CFFD9), text = Color(0xFFFFF0F7), dim = Color(0xFFB08CA3),
    ),
)

/**
 * Warna aktif. Disimpan sebagai snapshot state, jadi semua layar & gambar
 * otomatis diperbarui saat tema diganti.
 */
object Neon {
    var palette by mutableStateOf(Palettes.first { it.key == "neon" })

    val Bg get() = palette.bg
    val BgTop get() = palette.bgTop
    val Panel get() = palette.panel
    val PanelHi get() = palette.panelHi
    val Line get() = palette.line
    val Lcd get() = palette.lcd
    val Cyan get() = palette.primary
    val Magenta get() = palette.secondary
    val Violet get() = palette.tertiary
    val Lime get() = palette.ok
    val Text get() = palette.text
    val Dim get() = palette.dim

    val accent get() = Brush.horizontalGradient(listOf(Cyan, Violet, Magenta))
    val panelGlow get() = Brush.verticalGradient(listOf(PanelHi, Panel))

    private const val PREF = "neonamp_prefs"

    fun load(context: Context) {
        val key = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("theme", null)
        Palettes.firstOrNull { it.key == key }?.let { palette = it }
    }

    fun select(context: Context, p: Palette) {
        palette = p
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString("theme", p.key).apply()
    }
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
            surfaceContainer = Neon.Panel,
            surfaceContainerHigh = Neon.PanelHi,
            onPrimary = Neon.Bg,
            onBackground = Neon.Text,
            onSurface = Neon.Text,
            onSurfaceVariant = Neon.Dim,
            outline = Neon.Line,
        ),
        content = content,
    )
}
