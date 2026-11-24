package com.permata.sinemabebas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.permata.sinemabebas.data.*
import kotlinx.coroutines.launch

/**
 * The user's own streams: direct links (MP4, HLS .m3u8, DASH .mpd) or an M3U playlist
 * imported from a URL. Only for content the user is allowed to play.
 */
@Composable
fun SourcesScreen(prefs: Prefs, onPlay: (List<PlayItem>, Int) -> Unit) {
    val sources = remember { mutableStateListOf<PlayItem>().apply { addAll(prefs.sources()) } }
    var showAdd by remember { mutableStateOf(false) }
    fun persist() = prefs.saveSources(sources.toList())

    Scaffold(
        containerColor = Bg,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Tambah") })
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Text("Sumber Saya", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            if (sources.isEmpty()) {
                Text(
                    "Tambahkan tautan video (MP4, HLS .m3u8, DASH .mpd) atau impor playlist M3U " +
                        "dari layanan yang Anda berhak tonton. Subtitle Indonesia/Inggris/Jerman bisa ditambahkan per video.",
                    modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
                )
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
                itemsIndexed(sources, key = { i, s -> "$i${s.url}" }) { i, s ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onPlay(sources.toList(), i) }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Accent)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.title, fontWeight = FontWeight.Medium, maxLines = 1)
                            Text(
                                s.url + if (s.subtitles.isNotEmpty()) "  • CC: " + s.subtitles.joinToString { it.lang.uppercase() } else "",
                                fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f),
                            )
                        }
                        IconButton(onClick = { sources.removeAt(i); persist() }) { Icon(Icons.Default.Delete, "Hapus") }
                    }
                }
            }
        }
    }

    if (showAdd) AddSourceDialog(
        onDismiss = { showAdd = false },
        onAdd = { items -> sources.addAll(items); persist(); showAdd = false },
    )
}

@Composable
private fun AddSourceDialog(onDismiss: () -> Unit, onAdd: (List<PlayItem>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val subs = remember { mutableStateMapOf<String, String>() }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah sumber") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(name, { name = it }, label = { Text("Judul") }, singleLine = true)
                OutlinedTextField(url, { url = it }, label = { Text("URL video / playlist M3U") }, singleLine = true)
                Text("Subtitle (opsional, URL .srt/.vtt)", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                Languages.options.forEach { (code, label) ->
                    OutlinedTextField(subs[code] ?: "", { subs[code] = it }, label = { Text(label) }, singleLine = true)
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && url.startsWith("http"), onClick = {
                val u = url.trim()
                val subtitles = subs.filterValues { it.isNotBlank() }.map { (lang, s) -> Subtitle(s.trim(), lang, Languages.label(lang)) }
                val single = PlayItem(name.ifBlank { u.substringAfterLast('/').substringBefore('?') }, u, subtitles)
                val path = u.substringBefore('?').lowercase()
                if (path.endsWith(".m3u") || path.endsWith(".m3u8")) {
                    // Could be a channel/video list rather than a single HLS stream: check.
                    busy = true; error = null
                    scope.launch {
                        val text = runCatching { Http.get(u) }.getOrNull()
                        busy = false
                        if (text != null && M3u.isPlaylist(text)) {
                            val list = M3u.parse(text)
                            if (list.isEmpty()) error = "Playlist kosong" else onAdd(list)
                        } else onAdd(listOf(single))
                    }
                } else onAdd(listOf(single))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
