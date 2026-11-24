package com.permata.sinemabebas.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.permata.sinemabebas.data.ArchiveRepository
import com.permata.sinemabebas.data.PlayItem
import com.permata.sinemabebas.data.Title
import com.permata.sinemabebas.data.TitleDetail

@Composable
fun DetailScreen(title: Title, onBack: () -> Unit, onPlay: (TitleDetail, List<PlayItem>, Int) -> Unit) {
    BackHandler(onBack = onBack)
    var detail by remember { mutableStateOf<TitleDetail?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(title.id, attempt) {
        error = null
        runCatching { ArchiveRepository.detail(title.id) }
            .onSuccess { detail = it }
            .onFailure { error = it.message ?: "Error" }
    }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillMaxWidth().height(260.dp)) {
                AsyncImage(
                    title.posterUrl, null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Bg))))
                IconButton(onClick = onBack, modifier = Modifier.padding(8.dp).background(Color(0x88000000), RoundedCornerShape(50))) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                val d = detail
                Text(d?.title?.name ?: title.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                val meta = listOfNotNull(
                    (d?.title?.year ?: title.year).takeIf { it.isNotBlank() },
                    d?.runtime?.takeIf { it.isNotBlank() },
                    d?.language?.takeIf { it.isNotBlank() },
                    d?.items?.size?.takeIf { it > 1 }?.let { "$it episode" },
                ).joinToString(" • ")
                if (meta.isNotEmpty()) Text(meta, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
                Spacer(Modifier.height(12.dp))
                when {
                    error != null -> Column {
                        Text("Gagal memuat detail: $error")
                        TextButton(onClick = { attempt++ }) { Text("Coba lagi") }
                    }
                    d == null -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    d.items.isEmpty() -> Text("Video tidak tersedia untuk judul ini.")
                    else -> {
                        Button(onClick = { onPlay(d, d.items, 0) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp))
                            Text(if (d.items.size > 1) "Putar Episode 1" else "Putar")
                        }
                        val subLangs = d.items.flatMap { it.subtitles }.map { it.label }.distinct()
                        Text(
                            if (subLangs.isEmpty()) "Subtitle: tambahkan sendiri dari HP saat menonton"
                            else "Subtitle tersedia: ${subLangs.joinToString()}",
                            fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f),
                        )
                    }
                }
                val desc = d?.title?.description ?: title.description
                if (desc.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        desc, fontSize = 14.sp, maxLines = if (expanded) Int.MAX_VALUE else 4,
                        modifier = Modifier.clickable { expanded = !expanded },
                    )
                }
                if ((d?.items?.size ?: 0) > 1) {
                    Text("Episode", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                }
            }
        }
        val d = detail
        if (d != null && d.items.size > 1) {
            itemsIndexed(d.items) { i, ep ->
                Row(
                    Modifier.fillMaxWidth().clickable { onPlay(d, d.items, i) }.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(Surface),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Text(ep.title, modifier = Modifier.weight(1f), maxLines = 2)
                    Icon(Icons.Default.PlayArrow, null)
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}
