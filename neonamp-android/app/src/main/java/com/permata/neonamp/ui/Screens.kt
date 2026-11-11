package com.permata.neonamp.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.permata.neonamp.data.Source
import com.permata.neonamp.data.Track

@Composable
fun NeonAmpRoot(vm: PlayerViewModel) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snack.showSnackbar(it)
            vm.consumeMessage()
        }
    }
    BackHandler(showPlayer) { showPlayer = false }

    Box(Modifier.fillMaxSize().background(Neon.Bg)) {
        Scaffold(
            containerColor = Neon.Bg,
            snackbarHost = { SnackbarHost(snack) },
            bottomBar = {
                Column {
                    if (ui.track != null) {
                        MiniPlayer(ui, onOpen = { showPlayer = true }, onToggle = vm::togglePlay, onNext = vm::next)
                    }
                    BottomNav(tab) { tab = it }
                }
            },
        ) { pad ->
            Column(Modifier.padding(pad).fillMaxSize()) {
                Header()
                when (tab) {
                    0 -> SearchTab(vm, ui)
                    1 -> OfflineTab(vm, ui)
                    else -> DeviceTab(vm, ui)
                }
            }
        }

        AnimatedVisibility(
            visible = showPlayer && ui.track != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            NowPlayingScreen(vm, onClose = { showPlayer = false })
        }
    }
}

@Composable
private fun Header() {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "NEON",
            style = TextStyle(
                brush = Neon.accent, fontSize = 26.sp, fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace, letterSpacing = 3.sp,
            )
        )
        Text(
            "//AMP", color = Neon.Text, fontSize = 26.sp, fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace, letterSpacing = 3.sp,
        )
    }
}

@Composable
private fun BottomNav(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("Cari", Icons.Rounded.Search, 0),
        Triple("Offline", Icons.Rounded.WifiOff, 1),
        Triple("Di HP", Icons.Rounded.Smartphone, 2),
    )
    NavigationBar(containerColor = Neon.Panel, tonalElevation = 0.dp) {
        items.forEach { (label, icon, i) ->
            NavigationBarItem(
                selected = selected == i,
                onClick = { onSelect(i) },
                icon = { Icon(icon, label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Neon.Cyan,
                    selectedTextColor = Neon.Cyan,
                    indicatorColor = Neon.Cyan.copy(alpha = 0.12f),
                    unselectedIconColor = Neon.Dim,
                    unselectedTextColor = Neon.Dim,
                ),
            )
        }
    }
}

@Composable
private fun MiniPlayer(ui: PlayerUi, onOpen: () -> Unit, onToggle: () -> Unit, onNext: () -> Unit) {
    val t = ui.track ?: return
    val progress = if (ui.durationMs > 0) ui.positionMs.toFloat() / ui.durationMs else 0f
    Column(
        Modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Neon.panelGlow)
            .border(1.dp, Neon.Line, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Artwork(t.artworkUrl, 44.dp, corner = 10.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(t.title, color = Neon.Text, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(t.artist, color = Neon.Dim, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            }
            MiniWave(ui.isPlaying, Modifier.size(width = 22.dp, height = 18.dp))
            IconButton(onClick = onToggle) {
                if (ui.buffering) CircularProgressIndicator(Modifier.size(22.dp), color = Neon.Cyan, strokeWidth = 2.dp)
                else Icon(if (ui.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Neon.Text)
            }
            IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, null, tint = Neon.Text) }
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(Neon.Line)) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(2.dp).background(Neon.accent))
        }
    }
}

// ---------------- Tab: Cari (YouTube) ----------------
@Composable
private fun SearchTab(vm: PlayerViewModel, ui: PlayerUi) {
    val query by vm.query.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val searching by vm.searching.collectAsStateWithLifecycle()
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val library by vm.library.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val offlineIds = remember(library) { library.map { it.id }.toSet() }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            placeholder = { Text("Cari lagu di YouTube atau tempel link…", color = Neon.Dim) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Neon.Cyan) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { vm.query.value = "" }) {
                    Icon(Icons.Rounded.Close, null, tint = Neon.Dim)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); vm.search() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Neon.Cyan,
                unfocusedBorderColor = Neon.Line,
                focusedContainerColor = Neon.Panel,
                unfocusedContainerColor = Neon.Panel,
                cursorColor = Neon.Cyan,
                focusedTextColor = Neon.Text,
                unfocusedTextColor = Neon.Text,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        if (searching) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Neon.Cyan, trackColor = Neon.Line,
            )
        }
        if (results.isEmpty() && !searching) {
            EmptyState(Icons.Rounded.Search, "Cari lagu favoritmu", "Putar langsung (online) atau unduh untuk didengar offline.")
        }
        TrackList(results, ui) { index, track ->
            TrackRow(
                track = track,
                active = ui.track?.id == track.id,
                onClick = { vm.playList(results, index) },
            ) {
                IconButton(onClick = { vm.addToQueue(track) }) {
                    Icon(Icons.Rounded.PlaylistAdd, "Antrean", tint = Neon.Dim)
                }
                DownloadButton(
                    progress = downloads[track.id],
                    offline = track.id in offlineIds,
                    onDownload = { vm.download(track) },
                    onRetry = { vm.clearDownloadError(track); vm.download(track) },
                )
            }
        }
    }
}

@Composable
fun DownloadButton(progress: Float?, offline: Boolean, onDownload: () -> Unit, onRetry: () -> Unit) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        when {
            offline -> Icon(Icons.Rounded.CheckCircle, "Tersimpan offline", tint = Neon.Lime)
            progress == null -> IconButton(onClick = onDownload) {
                Icon(Icons.Rounded.Download, "Unduh", tint = Neon.Cyan)
            }
            progress < 0f -> IconButton(onClick = onRetry) {
                Icon(Icons.Rounded.ErrorOutline, "Gagal, coba lagi", tint = Neon.Magenta)
            }
            progress == 0f -> CircularProgressIndicator(Modifier.size(24.dp), color = Neon.Cyan, strokeWidth = 2.dp)
            else -> CircularProgressIndicator(
                progress = { progress }, modifier = Modifier.size(24.dp),
                color = Neon.Cyan, trackColor = Neon.Line, strokeWidth = 2.dp,
            )
        }
    }
}

// ---------------- Tab: Offline ----------------
@Composable
private fun OfflineTab(vm: PlayerViewModel, ui: PlayerUi) {
    val library by vm.library.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        SectionBar("${library.size} lagu tersimpan", enabled = library.isNotEmpty(),
            onPlayAll = { vm.playList(library, 0) },
            onShuffle = { vm.playList(library.shuffled(), 0) })
        if (library.isEmpty()) {
            EmptyState(Icons.Rounded.WifiOff, "Belum ada lagu offline", "Tekan ikon unduh di hasil pencarian YouTube.")
        }
        TrackList(library, ui) { index, track ->
            TrackRow(track, ui.track?.id == track.id, onClick = { vm.playList(library, index) }) {
                IconButton(onClick = { vm.deleteOffline(track) }) {
                    Icon(Icons.Rounded.Delete, "Hapus", tint = Neon.Dim)
                }
            }
        }
    }
}

// ---------------- Tab: Musik di HP ----------------
@Composable
private fun DeviceTab(vm: PlayerViewModel, ui: PlayerUi) {
    val tracks by vm.deviceTracks.collectAsStateWithLifecycle()
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) vm.scanDevice()
    }
    Column(Modifier.fillMaxSize()) {
        SectionBar("${tracks.size} lagu di perangkat", enabled = tracks.isNotEmpty(),
            onPlayAll = { vm.playList(tracks, 0) },
            onShuffle = { vm.playList(tracks.shuffled(), 0) },
            extra = { TextButton(onClick = { launcher.launch(permission) }) { Text("Pindai", color = Neon.Cyan) } })
        if (tracks.isEmpty()) {
            EmptyState(Icons.Rounded.LibraryMusic, "Musik di HP", "Tekan \"Pindai\" untuk menampilkan lagu MP3/FLAC/M4A yang ada di perangkat.")
        }
        TrackList(tracks, ui) { index, track ->
            TrackRow(track, ui.track?.id == track.id, onClick = { vm.playList(tracks, index) }) {}
        }
    }
}

@Composable
private fun SectionBar(
    title: String,
    enabled: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Neon.Dim, fontFamily = FontFamily.Monospace, fontSize = 13.sp, modifier = Modifier.weight(1f))
        extra()
        IconButton(onClick = onShuffle, enabled = enabled) { Icon(Icons.Rounded.Shuffle, "Acak", tint = if (enabled) Neon.Text else Neon.Line) }
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(if (enabled) Neon.accent else Brush.linearGradient(listOf(Neon.Line, Neon.Line)))
                .clickable(enabled = enabled, onClick = onPlayAll),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.PlayArrow, "Putar semua", tint = Neon.Bg) }
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    ui: PlayerUi,
    row: @Composable (Int, Track) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.fillMaxSize()) {
        itemsIndexed(tracks, key = { i, t -> "$i-${t.id}" }) { i, t -> row(i, t) }
    }
}

@Composable
fun TrackRow(
    track: Track,
    active: Boolean,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (active) Neon.Cyan.copy(alpha = 0.07f) else Color.Transparent)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Artwork(track.artworkUrl, 52.dp, corner = 10.dp)
            if (active) Box(Modifier.size(52.dp).border(1.5.dp, Neon.Cyan, RoundedCornerShape(10.dp)))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title, color = if (active) Neon.Cyan else Neon.Text, maxLines = 2,
                overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium, fontSize = 15.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                SourceTag(track)
                Text(
                    listOf(track.artist, formatTime(track.durationMs)).filter { it.isNotBlank() }.joinToString(" • "),
                    color = Neon.Dim, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing()
    }
}

@Composable
fun SourceTag(track: Track) {
    val (label, color) = when {
        track.source == Source.LOCAL -> "HP" to Neon.Violet
        track.isOffline -> "OFFLINE" to Neon.Lime
        else -> "YT" to Neon.Magenta
    }
    Text(
        label, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(end = 6.dp)
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = Neon.Cyan.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
        Text(title, color = Neon.Text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Text(subtitle, color = Neon.Dim, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
