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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.permata.neonamp.data.DownloadManager
import com.permata.neonamp.data.Kind
import com.permata.neonamp.data.Track
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.Tune
import com.permata.neonamp.data.YouTubeRepo

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

    Box(Modifier.fillMaxSize().background(Neon.Bg).heroBackdrop()) {
        Scaffold(
            containerColor = Color.Transparent,
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
                Header(ui.isPlaying)
                when (tab) {
                    0 -> SearchTab(vm, ui)
                    1 -> AudioTab(vm, ui)
                    2 -> VideoTab(vm, ui)
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
private fun Header(playing: Boolean) {
    var picker by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    if (picker) ThemePicker(onDismiss = { picker = false })
    if (settings) QualityDialog(onDismiss = { settings = false })
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Mascot(playing, Modifier.size(46.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Row {
                Text(
                    "NEON",
                    style = TextStyle(
                        brush = Neon.accent, fontSize = 24.sp, fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace, letterSpacing = 3.sp,
                    )
                )
                Text(
                    "//AMP", color = Neon.Text, fontSize = 24.sp, fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace, letterSpacing = 3.sp,
                )
            }
            val unit = if (Neon.palette.hero != null) "HERO MODE" else "UNIT-01"
            StatusDot(if (playing) "$unit · MEMUTAR" else "$unit · ONLINE")
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { settings = true }) {
            Icon(Icons.Rounded.Tune, "Kualitas audio & video", tint = Neon.Cyan)
        }
        IconButton(onClick = { picker = true }) {
            Icon(Icons.Rounded.Palette, "Ganti tema", tint = Neon.Cyan)
        }
    }
}

@Composable
private fun BottomNav(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("Cari", Icons.Rounded.Search, 0),
        Triple("Audio", Icons.Rounded.MusicNote, 1),
        Triple("Video", Icons.Rounded.VideoLibrary, 2),
        Triple("Di HP", Icons.Rounded.Smartphone, 3),
    )
    NavigationBar(containerColor = Neon.Panel, tonalElevation = 0.dp) {
        items.forEach { (label, icon, i) ->
            NavigationBarItem(
                selected = selected == i,
                onClick = { onSelect(i) },
                icon = { Icon(icon, label) },
                label = { Text(label.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold) },
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
            .clip(HudShape(16.dp))
            .background(Neon.panelGlow)
            .border(1.dp, Neon.Line, HudShape(16.dp))
            .hudCorners(Neon.Cyan.copy(alpha = 0.8f), len = 8.dp, inset = 3.dp)
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
    val audioLib by vm.audioLibrary.collectAsStateWithLifecycle()
    val videoLib by vm.videoLibrary.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()
    val filter by vm.searchFilter.collectAsStateWithLifecycle()
    val hasMore by vm.hasMore.collectAsStateWithLifecycle()
    val loadingMore by vm.loadingMore.collectAsStateWithLifecycle()
    val audioQ by vm.audioQuality.collectAsStateWithLifecycle()
    val videoMax by vm.videoMax.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val audioIds = remember(audioLib) { audioLib.map { it.id }.toSet() }
    val videoIds = remember(videoLib) { videoLib.map { it.id }.toSet() }
    var focused by remember { mutableStateOf(false) }

    // Gulir mendekati akhir → muat halaman hasil berikutnya otomatis.
    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd, results.size, hasMore) { if (nearEnd && hasMore) vm.loadMore() }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = vm::onQueryChange,
            placeholder = { Text("Cari lagu, artis, album, atau tempel link…", color = Neon.Dim) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Neon.Cyan) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { vm.onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, null, tint = Neon.Dim)
                }
            },
            singleLine = true,
            shape = HudShape(14.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .onFocusChanged { focused = it.isFocused },
        )

        // Saran kata kunci dari YouTube
        if (focused && suggestions.isNotEmpty()) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .clip(HudShape(10.dp))
                    .background(Neon.PanelHi)
                    .border(1.dp, Neon.Line, HudShape(10.dp))
            ) {
                suggestions.forEach { s ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { keyboard?.hide(); vm.pickSuggestion(s) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Search, null, tint = Neon.Dim, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(s, color = Neon.Text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.NorthWest, "Pakai", tint = Neon.Dim, modifier = Modifier.size(16.dp)
                            .clickable { vm.onQueryChange("$s ") })
                    }
                }
            }
        }

        // Sumber pencarian
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YouTubeRepo.Filter.entries.forEach { f ->
                HudChip(
                    when (f) {
                        YouTubeRepo.Filter.SONGS -> "♪ LAGU · YT MUSIC"
                        YouTubeRepo.Filter.VIDEOS -> "▶ VIDEO"
                        YouTubeRepo.Filter.MUSIC_VIDEOS -> "◉ VIDEO MUSIK"
                        YouTubeRepo.Filter.ALL -> "✦ SEMUA"
                    },
                    selected = f == filter,
                    onClick = { vm.setFilter(f) },
                )
            }
        }
        Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            HudLabel("SYS://YOUTUBE", Neon.Cyan.copy(alpha = 0.7f))
            Spacer(Modifier.weight(1f))
            HudLabel("AUDIO ${audioQ.label.uppercase()} · VIDEO ≤${videoMax}p", Neon.Lime.copy(alpha = 0.8f))
        }

        if (searching) {
            LinearProgressIndicator(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                color = Neon.Cyan, trackColor = Neon.Line,
            )
        }
        if (results.isEmpty() && !searching) {
            EmptyState(
                Icons.Rounded.Search, "Siap mencari, Kapten",
                "Pilih \"Lagu · YT Music\" untuk rekaman resmi berkualitas tinggi, atau \"Video\" untuk semua video. Gulir ke bawah untuk hasil lebih banyak.",
            )
        }
        LazyColumn(state = listState, contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(results, key = { i, t -> "$i-${t.id}" }) { index, track ->
                val aState = downloads[DownloadManager.key(Kind.AUDIO, track.id)]
                val vState = downloads[DownloadManager.key(Kind.VIDEO, track.id)]
                TrackRow(
                    track = track,
                    active = ui.track?.id == track.id,
                    onClick = { vm.playList(results, index) },
                    badges = {
                        if (track.id in audioIds) Badge("AUDIO ✓", Neon.Lime)
                        if (track.id in videoIds) Badge("VIDEO ✓", Neon.Lime)
                        if (aState != null) Badge(progressLabel("AUDIO", aState), if (aState < 0) Neon.Magenta else Neon.Cyan)
                        if (vState != null) Badge(progressLabel("VIDEO", vState), if (vState < 0) Neon.Magenta else Neon.Cyan)
                    },
                ) {
                    IconButton(onClick = { vm.playList(results, index, video = true) }) {
                        Icon(Icons.Rounded.SmartDisplay, "Tonton video", tint = Neon.Magenta)
                    }
                    TrackMenu(
                        track = track,
                        audioSaved = track.id in audioIds,
                        videoSaved = track.id in videoIds,
                        audioState = aState,
                        videoState = vState,
                        vm = vm,
                    )
                }
            }
            if (results.isNotEmpty()) item(key = "footer") {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    when {
                        loadingMore || hasMore -> CircularProgressIndicator(Modifier.size(24.dp), color = Neon.Cyan, strokeWidth = 2.dp)
                        else -> HudLabel("— ${results.size} HASIL · AKHIR DATA —")
                    }
                }
            }
        }
    }
}

internal fun progressLabel(kind: String, p: Float) = when {
    p < 0f -> "$kind GAGAL"
    p == 0f -> "$kind …"
    else -> "$kind ${(p * 100).toInt()}%"
}

@Composable
internal fun TrackMenu(
    track: Track,
    audioSaved: Boolean,
    videoSaved: Boolean,
    audioState: Float?,
    videoState: Float?,
    vm: PlayerViewModel,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        val busy = (audioState != null && audioState >= 0f) || (videoState != null && videoState >= 0f)
        IconButton(onClick = { open = true }) {
            if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Neon.Cyan, strokeWidth = 2.dp)
            else Icon(Icons.Rounded.Download, "Unduh & lainnya", tint = Neon.Cyan)
        }
        DropdownMenu(open, onDismissRequest = { open = false }, containerColor = Neon.PanelHi) {
            MenuRow(Icons.Rounded.PlaylistAdd, "Tambah ke antrean") { open = false; vm.addToQueue(track) }
            MenuRow(
                Icons.Rounded.MusicNote,
                when {
                    audioSaved -> "Audio sudah offline"
                    audioState != null && audioState < 0 -> "Ulangi unduh audio"
                    else -> "Unduh audio (ringan)"
                },
                enabled = !audioSaved && (audioState == null || audioState < 0),
            ) {
                open = false
                if (audioState != null) vm.retryDownload(track, Kind.AUDIO) else vm.download(track, Kind.AUDIO)
            }
            MenuRow(
                Icons.Rounded.VideoLibrary,
                when {
                    videoSaved -> "Video sudah offline"
                    videoState != null && videoState < 0 -> "Ulangi unduh video"
                    else -> "Unduh video 720p"
                },
                enabled = !videoSaved && (videoState == null || videoState < 0),
            ) {
                open = false
                if (videoState != null) vm.retryDownload(track, Kind.VIDEO) else vm.download(track, Kind.VIDEO)
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = if (enabled) Neon.Text else Neon.Dim) },
        leadingIcon = { Icon(icon, null, tint = if (enabled) Neon.Cyan else Neon.Dim) },
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
fun Badge(text: String, color: Color) {
    Text(
        text, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(end = 6.dp)
            .border(1.dp, color.copy(alpha = 0.6f), HudShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}

/** Tombol unduh kecil di layar Now Playing. */
@Composable
fun DownloadButton(progress: Float?, offline: Boolean, icon: ImageVector, onDownload: () -> Unit, onRetry: () -> Unit) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        when {
            offline -> Icon(Icons.Rounded.CheckCircle, "Tersimpan offline", tint = Neon.Lime)
            progress == null -> IconButton(onClick = onDownload) {
                Icon(icon, "Unduh", tint = Neon.Cyan)
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

// ---------------- Tab: Video offline ----------------
@Composable
private fun VideoTab(vm: PlayerViewModel, ui: PlayerUi) {
    val library by vm.videoLibrary.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        SectionBar("${library.size} video offline", enabled = library.isNotEmpty(),
            onPlayAll = { vm.playList(library, 0, video = true) },
            onShuffle = { vm.playList(library.shuffled(), 0, video = true) })
        if (library.isEmpty()) {
            EmptyState(Icons.Rounded.VideoLibrary, "Belum ada video offline", "Di tab Cari, ketuk ikon unduh → \"Unduh video\". Video & audio disimpan terpisah supaya pemutaran lebih mulus.")
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(library, key = { _, t -> t.id }) { index, t ->
                VideoCard(
                    t, active = ui.track?.id == t.id && ui.track?.wantVideo == true,
                    onClick = { vm.playList(library, index, video = true) },
                    onAudioOnly = { vm.playList(library, index, video = false) },
                    onDelete = { vm.deleteOffline(t, Kind.VIDEO) },
                )
            }
        }
    }
}

@Composable
private fun VideoCard(t: Track, active: Boolean, onClick: () -> Unit, onAudioOnly: () -> Unit, onDelete: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HudShape(16.dp))
            .background(Neon.panelGlow)
            .border(1.dp, if (active) Neon.Cyan else Neon.Line, HudShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            Artwork(t.artworkUrl, null, Modifier.fillMaxSize(), corner = 0.dp)
            Box(
                Modifier.align(Alignment.Center).size(54.dp).clip(HexShape).background(Neon.Bg.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.PlayArrow, null, tint = Neon.Cyan, modifier = Modifier.size(34.dp)) }
            Text(
                formatTime(t.durationMs), color = Neon.Text, fontFamily = FontFamily.Monospace, fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    .clip(HudShape(4.dp)).background(Neon.Bg.copy(alpha = 0.75f)).padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(t.title, color = if (active) Neon.Cyan else Neon.Text, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text(t.artist, color = Neon.Dim, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onAudioOnly) { Icon(Icons.Rounded.Headphones, "Putar audio saja", tint = Neon.Dim) }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, "Hapus", tint = Neon.Dim) }
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
        TrackList(tracks) { index, track ->
            TrackRow(track, ui.track?.id == track.id, onClick = { vm.playList(tracks, index) }) {}
        }
    }
}

@Composable
internal fun SectionBar(
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
            Modifier.size(40.dp).clip(HexShape)
                .background(if (enabled) Neon.accent else Brush.linearGradient(listOf(Neon.Line, Neon.Line)))
                .clickable(enabled = enabled, onClick = onPlayAll),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.PlayArrow, "Putar semua", tint = Neon.Bg) }
    }
}

@Composable
internal fun TrackList(
    tracks: List<Track>,
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
    badges: @Composable () -> Unit = {},
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
            if (active) Box(Modifier.size(52.dp).border(1.5.dp, Neon.Cyan, HudShape(10.dp)))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title, color = if (active) Neon.Cyan else Neon.Text, maxLines = 2,
                overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium, fontSize = 15.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                SourceTag(track)
                badges()
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
        track.wantVideo -> "VIDEO" to Neon.Magenta
        track.isOffline -> "OFFLINE" to Neon.Lime
        else -> "YT" to Neon.Magenta
    }
    Text(
        label, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(end = 6.dp)
            .border(1.dp, color.copy(alpha = 0.6f), HudShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}

@Composable
internal fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Mascot(false, Modifier.size(84.dp))
            Icon(icon, null, tint = Neon.Magenta, modifier = Modifier.size(22.dp))
        }
        Text(title, color = Neon.Text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Text(subtitle, color = Neon.Dim, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
