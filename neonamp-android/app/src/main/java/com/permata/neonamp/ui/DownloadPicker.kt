package com.permata.neonamp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.permata.neonamp.data.Kind
import com.permata.neonamp.data.Track
import com.permata.neonamp.data.YouTubeRepo

fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> "± ? MB"
    bytes >= 1L shl 30 -> "%.1f GB".format(bytes / 1073741824.0)
    bytes >= 10L shl 20 -> "${bytes shr 20} MB"
    else -> "%.1f MB".format(bytes / 1048576.0)
}

/** Dialog pilih ukuran/kualitas sebelum mengunduh (hemat kuota & memori). */
@Composable
fun DownloadPickerDialog(vm: PlayerViewModel, track: Track, kind: Kind) {
    var options by remember(track.id) { mutableStateOf<YouTubeRepo.DownloadOptions?>(null) }
    var failed by remember(track.id) { mutableStateOf(false) }
    LaunchedEffect(track.id) {
        val o = vm.downloadOptions(track)
        if (o == null) failed = true else options = o
    }
    AlertDialog(
        onDismissRequest = vm::closeDownloadPicker,
        containerColor = Neon.Panel,
        title = {
            Column {
                HudLabel(if (kind == Kind.VIDEO) "UNDUH VIDEO · PILIH UKURAN" else "UNDUH AUDIO · PILIH KUALITAS", Neon.Cyan)
                Spacer(Modifier.size(4.dp))
                Text(track.title, color = Neon.Text, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        },
        text = {
            val o = options
            when {
                failed -> Text("Gagal mengambil daftar ukuran. Periksa koneksi lalu coba lagi.", color = Neon.Magenta)
                o == null -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Neon.Cyan)
                }
                else -> Column(
                    Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (kind == Kind.VIDEO) {
                        if (o.video.isEmpty()) Text("Tidak ada format video yang bisa diputar di HP ini.", color = Neon.Magenta)
                        val rec = o.video.firstOrNull { it.height <= 720 } ?: o.video.lastOrNull()
                        o.video.forEach { v ->
                            SizeRow(
                                title = "${v.height}p" + (if (v.fps > 30) " ${v.fps}fps" else ""),
                                detail = "${v.codec}" + (if (v.muxed) " · format gabungan" else " · audio terbaik terpisah") +
                                    when {
                                        v.height >= 1080 -> " · paling tajam"
                                        v.height <= 360 -> " · paling hemat"
                                        else -> ""
                                    },
                                size = formatSize(v.bytes),
                                recommended = v == rec,
                            ) { vm.download(track, Kind.VIDEO, videoItag = v.itag, audioItag = o.defaultAudioItag) }
                        }
                    } else {
                        if (o.audio.isEmpty()) Text("Tidak ada format audio.", color = Neon.Magenta)
                        o.audio.forEachIndexed { i, a ->
                            SizeRow(
                                title = a.label,
                                detail = when {
                                    i == 0 -> "Kualitas terbaik"
                                    a.kbps <= 70 -> "Hemat memori & kuota"
                                    else -> "Seimbang, paling kompatibel"
                                },
                                size = formatSize(a.bytes),
                                recommended = a.itag == o.defaultAudioItag,
                            ) { vm.download(track, Kind.AUDIO, audioItag = a.itag) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = vm::closeDownloadPicker) { Text("Batal", color = Neon.Dim) } },
    )
}

@Composable
private fun SizeRow(title: String, detail: String, size: String, recommended: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(HudShape(8.dp))
            .background(if (recommended) Neon.Cyan.copy(alpha = 0.08f) else Neon.PanelHi)
            .border(1.dp, if (recommended) Neon.Cyan else Neon.Line, HudShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Neon.Text, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                if (recommended) {
                    Spacer(Modifier.width(6.dp))
                    Badge("REKOMENDASI", Neon.Lime)
                }
            }
            Text(detail, color = Neon.Dim, fontSize = 12.sp)
        }
        Text(size, color = Neon.Cyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Rounded.Download, null, tint = Neon.Cyan, modifier = Modifier.size(20.dp))
    }
}
