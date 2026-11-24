package com.permata.sinemabebas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.permata.sinemabebas.data.ArchiveRepository
import com.permata.sinemabebas.data.Languages
import com.permata.sinemabebas.data.Prefs
import com.permata.sinemabebas.data.Title

@Composable
fun HomeScreen(vm: CatalogViewModel, history: List<Title>, onOpen: (Title) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val cats = ArchiveRepository.categories
    Column(Modifier.fillMaxSize()) {
        Text(
            "Sinema Bebas", color = Accent, fontSize = 26.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        )
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp, containerColor = Bg) {
            cats.forEachIndexed { i, c -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(c.label) }) }
        }
        val list = vm.category(cats[tab].key)
        PosterGrid(
            items = list.items, loading = list.loading, error = list.error,
            onLoadMore = { vm.loadMore(list) }, onRetry = { vm.retry(list) }, onOpen = onOpen,
            showHeader = history.isNotEmpty() && tab == 0,
            header = {
                Column {
                    Text("Lanjutkan menonton", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(history, key = { "h" + it.id }) { PosterCard(it, { onOpen(it) }, Modifier.width(100.dp)) }
                    }
                    Text("Populer", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                }
            },
        )
    }
}

@Composable
fun SearchScreen(vm: CatalogViewModel, onOpen: (Title) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = vm.query, onValueChange = vm::onQuery,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Cari film, serial, drama…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
        )
        val list = vm.searchList
        if (list == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ketik judul untuk mencari", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
            }
        } else if (!list.loading && list.error == null && list.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tidak ada hasil") }
        } else {
            PosterGrid(list.items, list.loading, list.error, { vm.loadMore(list) }, { vm.retry(list) }, onOpen)
        }
    }
}

@Composable
fun SettingsScreen(prefs: Prefs) {
    var sub by remember { mutableStateOf(prefs.subtitleLang) }
    var audio by remember { mutableStateOf(prefs.audioLang) }
    var saver by remember { mutableStateOf(prefs.dataSaver) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pengaturan", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Bahasa subtitle utama", fontWeight = FontWeight.SemiBold)
        (Languages.options + ("off" to "Mati")).forEach { (code, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = sub == code, onClick = { sub = code; prefs.subtitleLang = code })
                Text(label)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Bahasa audio utama", fontWeight = FontWeight.SemiBold)
        (listOf("auto" to "Asli (bawaan film)") + Languages.options).forEach { (code, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = audio == code, onClick = { audio = code; prefs.audioLang = code })
                Text(label)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hemat kuota", fontWeight = FontWeight.SemiBold)
                Text("Batasi kualitas video ke 480p", fontSize = 12.sp)
            }
            Switch(checked = saver, onCheckedChange = { saver = it; prefs.dataSaver = it })
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Saat menonton, ketuk ikon CC untuk mengganti subtitle, ikon ⚙ untuk mengganti audio & kecepatan, " +
                "dan tombol \"+ Subtitle\" untuk memuat file .srt/.vtt dari HP.",
            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Katalog berasal dari Internet Archive (archive.org): film domain publik & berlisensi bebas yang legal " +
                "ditonton gratis. Di \"Sumber Saya\" tambahkan hanya tautan yang Anda berhak putar.",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f),
        )
    }
}
