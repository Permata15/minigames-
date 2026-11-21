package com.permata.neonamp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.permata.neonamp.data.AudioQuality
import com.permata.neonamp.data.Prefs

/** Pengaturan kualitas stream & unduhan. */
@Composable
fun QualityDialog(onDismiss: () -> Unit) {
    val audio by Prefs.audio.collectAsStateWithLifecycle()
    val video by Prefs.videoMax.collectAsStateWithLifecycle()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Neon.Panel,
        title = { Text("Kualitas", color = Neon.Text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HudLabel("AUDIO · STREAM & UNDUHAN", Neon.Cyan)
                AudioQuality.entries.forEach { q ->
                    Option(q.label, q.detail, q == audio) { Prefs.setAudio(q) }
                }
                Spacer(Modifier.width(1.dp))
                HudLabel("VIDEO · RESOLUSI MAKSIMUM", Neon.Cyan)
                Prefs.videoOptions.forEach { h ->
                    val detail = when (h) {
                        1080 -> "Full HD, paling tajam (±60–90 MB per lagu)"
                        720 -> "HD, seimbang (±20–40 MB per lagu)"
                        480 -> "Hemat (±10–20 MB per lagu)"
                        else -> "Paling hemat kuota"
                    }
                    Option("${h}p", detail, h == video) { Prefs.setVideoMax(h) }
                }
                Text(
                    "Video & audio diambil terpisah lalu digabung, jadi suara tetap kualitas terbaik walau video diturunkan.",
                    color = Neon.Dim, fontSize = 12.sp,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai", color = Neon.Cyan) } },
    )
}

@Composable
private fun Option(title: String, detail: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(HudShape(8.dp))
            .background(if (selected) Neon.Cyan.copy(alpha = 0.08f) else Neon.Panel)
            .border(1.dp, if (selected) Neon.Cyan else Neon.Line, HudShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (selected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked, null,
            tint = if (selected) Neon.Cyan else Neon.Dim,
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = Neon.Text, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Neon.Dim, fontSize = 12.sp)
        }
    }
}
