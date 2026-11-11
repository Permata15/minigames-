package com.permata.neonamp.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.permata.neonamp.data.LyricLine
import com.permata.neonamp.data.Source

@Composable
fun NowPlayingScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val lyrics by vm.lyrics.collectAsStateWithLifecycle()
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val library by vm.library.collectAsStateWithLifecycle()
    val track = ui.track ?: return
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 visual, 1 lirik, 2 antrean

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0B22), Neon.Bg, Neon.Bg)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.KeyboardArrowDown, "Tutup", tint = Neon.Text) }
            Text(
                "NOW PLAYING", color = Neon.Dim, fontFamily = FontFamily.Monospace, letterSpacing = 4.sp,
                fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            val offline = track.isOffline || library.any { it.id == track.id }
            if (track.source == Source.YOUTUBE) {
                DownloadButton(
                    progress = downloads[track.id], offline = offline,
                    onDownload = { vm.download(track) },
                    onRetry = { vm.clearDownloadError(track); vm.download(track) },
                )
            } else Spacer(Modifier.size(48.dp))
        }

        LcdPanel(ui)

        Spacer(Modifier.height(14.dp))
        ModeSwitch(mode) { mode = it }
        Spacer(Modifier.height(10.dp))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (mode) {
                0 -> {
                    val scale by animateFloatAsState(if (ui.isPlaying) 1f else 0.92f, label = "art")
                    Artwork(
                        track.artworkUrl, 280.dp,
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1f)
                            .shadow(
                                elevation = (28 * scale).dp, shape = RoundedCornerShape(24.dp),
                                ambientColor = Neon.Cyan, spotColor = Neon.Magenta,
                            ),
                        corner = 24.dp,
                    )
                }
                1 -> LyricsView(lyrics, ui.positionMs, onSeek = vm::seekTo)
                else -> QueueView(ui, vm::playQueueIndex)
            }
        }

        SeekBar(ui, vm::seekTo)
        Controls(ui, vm)
        Spacer(Modifier.height(12.dp))
    }
}

/** Panel "LCD" ala Winamp: waktu besar bercahaya, judul berjalan, spektrum. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LcdPanel(ui: PlayerUi) {
    val t = ui.track ?: return
    val glow = TextStyle(shadow = Shadow(Neon.Cyan, blurRadius = 18f))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF05060C))
            .border(1.dp, Brush.linearGradient(listOf(Neon.Cyan.copy(0.6f), Neon.Magenta.copy(0.6f))), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (ui.isPlaying) "▶" else "❚❚", color = Neon.Lime, fontSize = 14.sp,
                fontFamily = FontFamily.Monospace, modifier = Modifier.padding(end = 10.dp),
            )
            Text(
                formatTime(ui.positionMs), color = Neon.Cyan, fontSize = 40.sp,
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = glow,
            )
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                val src = when {
                    t.source == Source.LOCAL -> "LOCAL"
                    t.isOffline -> "OFFLINE"
                    else -> "STREAM"
                }
                LcdTag(src, if (src == "STREAM") Neon.Magenta else Neon.Lime)
                Spacer(Modifier.height(4.dp))
                LcdTag(if (ui.shuffle) "SHUF" else "SEQ", Neon.Violet)
                Spacer(Modifier.height(4.dp))
                Text("-" + formatTime((ui.durationMs - ui.positionMs).coerceAtLeast(0)), color = Neon.Dim, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${t.artist.ifBlank { "Unknown" }} — ${t.title}   ★   ",
            color = Neon.Text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, maxLines = 1,
            modifier = Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE),
        )
        Spacer(Modifier.height(10.dp))
        SpectrumBars(ui.isPlaying, Modifier.fillMaxWidth().height(56.dp))
    }
}

@Composable
private fun LcdTag(text: String, color: Color) {
    Text(
        text, color = color, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Composable
private fun ModeSwitch(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Neon.Panel)
            .border(1.dp, Neon.Line, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        listOf("VISUAL", "LIRIK", "ANTREAN").forEachIndexed { i, label ->
            val bg by animateColorAsState(if (i == selected) Neon.Cyan.copy(alpha = 0.16f) else Color.Transparent, label = "m")
            Text(
                label,
                color = if (i == selected) Neon.Cyan else Neon.Dim,
                fontFamily = FontFamily.Monospace, fontSize = 12.sp, letterSpacing = 2.sp,
                textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onSelect(i) }
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun LyricsView(state: LyricsState, positionMs: Long, onSeek: (Long) -> Unit) {
    when (state) {
        LyricsState.Idle, LyricsState.Loading -> CircularProgressIndicator(color = Neon.Cyan)
        LyricsState.NotFound -> Text(
            "Lirik tidak ditemukan untuk lagu ini.", color = Neon.Dim, textAlign = TextAlign.Center,
        )
        is LyricsState.Ready -> {
            val lines = state.result.lines
            if (!state.result.synced) {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp)) {
                    itemsIndexed(lines) { _, l ->
                        Text(l.text, color = Neon.Text, fontSize = 17.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                return
            }
            val current = currentLine(lines, positionMs)
            val listState = rememberLazyListState()
            LaunchedEffect(current) {
                if (current >= 0) listState.animateScrollToItem((current - 2).coerceAtLeast(0))
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(lines) { i, l ->
                    val active = i == current
                    val color by animateColorAsState(
                        when {
                            active -> Neon.Cyan
                            i < current -> Neon.Dim.copy(alpha = 0.6f)
                            else -> Neon.Text.copy(alpha = 0.75f)
                        }, label = "lc"
                    )
                    Text(
                        l.text.ifBlank { "♪" },
                        color = color,
                        fontSize = if (active) 22.sp else 18.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        style = if (active) TextStyle(shadow = Shadow(Neon.Cyan, blurRadius = 16f)) else TextStyle.Default,
                        modifier = Modifier.fillMaxWidth().clickable { onSeek(l.timeMs) },
                    )
                }
            }
        }
    }
}

private fun currentLine(lines: List<LyricLine>, pos: Long): Int {
    var idx = -1
    for (i in lines.indices) {
        if (lines[i].timeMs <= pos + 250) idx = i else break
    }
    return idx
}

@Composable
private fun QueueView(ui: PlayerUi, onPlay: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) { if (ui.queueIndex > 0) listState.scrollToItem(ui.queueIndex) }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        itemsIndexed(ui.queue) { i, t ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onPlay(i) }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "%02d".format(i + 1), color = if (i == ui.queueIndex) Neon.Cyan else Neon.Dim,
                    fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp),
                )
                Text(
                    t.title, color = if (i == ui.queueIndex) Neon.Cyan else Neon.Text,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                )
                Text(formatTime(t.durationMs), color = Neon.Dim, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SeekBar(ui: PlayerUi, onSeek: (Long) -> Unit) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    val dur = ui.durationMs.coerceAtLeast(1)
    val value = if (dragging) dragValue else (ui.positionMs.toFloat() / dur).coerceIn(0f, 1f)
    Column {
        Slider(
            value = value,
            onValueChange = { dragging = true; dragValue = it },
            onValueChangeFinished = { onSeek((dragValue * dur).toLong()); dragging = false },
            colors = SliderDefaults.colors(
                thumbColor = Neon.Cyan,
                activeTrackColor = Neon.Cyan,
                inactiveTrackColor = Neon.Line,
            ),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(formatTime(if (dragging) (dragValue * dur).toLong() else ui.positionMs), color = Neon.Dim, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(formatTime(ui.durationMs), color = Neon.Dim, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Controls(ui: PlayerUi, vm: PlayerViewModel) {
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = vm::toggleShuffle) {
            Icon(Icons.Rounded.Shuffle, "Acak", tint = if (ui.shuffle) Neon.Cyan else Neon.Dim)
        }
        IconButton(onClick = vm::previous, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Rounded.SkipPrevious, "Sebelumnya", tint = Neon.Text, modifier = Modifier.size(36.dp))
        }
        Box(
            Modifier
                .size(76.dp)
                .shadow(24.dp, CircleShape, ambientColor = Neon.Cyan, spotColor = Neon.Magenta)
                .clip(CircleShape)
                .background(Neon.accent)
                .clickable(onClick = vm::togglePlay),
            contentAlignment = Alignment.Center,
        ) {
            if (ui.buffering) CircularProgressIndicator(Modifier.size(30.dp), color = Neon.Bg, strokeWidth = 3.dp)
            else Icon(
                if (ui.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, "Play/Pause",
                tint = Neon.Bg, modifier = Modifier.size(42.dp),
            )
        }
        IconButton(onClick = vm::next, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Rounded.SkipNext, "Berikutnya", tint = Neon.Text, modifier = Modifier.size(36.dp))
        }
        IconButton(onClick = vm::cycleRepeat) {
            Icon(
                if (ui.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                "Ulangi", tint = if (ui.repeatMode == Player.REPEAT_MODE_OFF) Neon.Dim else Neon.Cyan,
            )
        }
    }
}
