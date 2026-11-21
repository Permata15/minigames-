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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Pilihan 5 mode tampilan. Tema langsung berubah saat diketuk dan diingat. */
@Composable
fun ThemePicker(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Neon.Panel,
        title = {
            Text("Mode Tampilan", color = Neon.Text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Palettes.forEach { p ->
                    val selected = p.key == Neon.palette.key
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(HudShape(14.dp))
                            .background(Brush.verticalGradient(listOf(p.panelHi, p.bg)))
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) p.primary else p.line,
                                HudShape(14.dp),
                            )
                            .clickable { Neon.select(context, p) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Mini pratinjau: bar spektrum dengan warna tema
                        Row(
                            Modifier.size(width = 44.dp, height = 30.dp)
                                .clip(HudShape(6.dp)).background(p.lcd).padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            listOf(0.5f, 0.9f, 0.65f, 1f, 0.4f).forEach { h ->
                                Box(
                                    Modifier.width(5.dp).height((22 * h).dp).clip(HudShape(1.dp))
                                        .background(Brush.verticalGradient(listOf(p.secondary, p.tertiary, p.primary)))
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, color = p.primary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(p.tagline, color = p.dim, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf(p.primary, p.tertiary, p.secondary).forEach {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(it))
                            }
                        }
                        if (selected) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Rounded.CheckCircle, null, tint = p.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Selesai", color = Neon.Cyan) }
        },
    )
}
