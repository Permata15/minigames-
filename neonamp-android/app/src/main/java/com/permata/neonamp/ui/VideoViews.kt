package com.permata.neonamp.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/** Permukaan video ExoPlayer (tanpa kontrol bawaan; kontrol dibuat sendiri). */
@OptIn(UnstableApi::class)
@Composable
fun VideoSurface(player: Player?, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(android.graphics.Color.BLACK)
                setKeepContentOnPlayerReset(true)
                keepScreenOn = true
            }
        },
        update = { it.player = player },
        onRelease = { it.player = null },
        modifier = modifier.background(Color.Black),
    )
}

/** Video di dalam layar Now Playing (16:9) dengan tombol layar penuh. */
@Composable
fun InlineVideo(player: Player?, buffering: Boolean, onFullscreen: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
    ) {
        VideoSurface(player, Modifier.fillMaxSize())
        if (buffering) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Neon.Cyan)
        IconButton(
            onClick = onFullscreen,
            modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                .clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
        ) { Icon(Icons.Rounded.Fullscreen, "Layar penuh", tint = Color.White) }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Mode layar penuh: landscape, bar sistem disembunyikan, ketuk untuk memunculkan kontrol. */
@Composable
fun FullscreenVideo(vm: PlayerViewModel, ui: PlayerUi, player: Player?, onExit: () -> Unit) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val window = activity?.window
        val insets = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        insets?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insets?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            insets?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    BackHandler(onBack = onExit)

    var controls by remember { mutableStateOf(true) }
    LaunchedEffect(controls, ui.isPlaying) {
        if (controls && ui.isPlaying) { delay(3500); controls = false }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(remember { MutableInteractionSource() }, indication = null) { controls = !controls }
    ) {
        VideoSurface(player, Modifier.fillMaxSize())
        if (ui.buffering) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Neon.Cyan)

        AnimatedVisibility(controls, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                listOf(Color.Black.copy(0.6f), Color.Transparent, Color.Black.copy(0.75f))
            ))) {
                Text(
                    ui.track?.title ?: "", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
                )
                Row(
                    Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = vm::previous) { Icon(Icons.Rounded.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                    Box(
                        Modifier.size(68.dp).clip(CircleShape).background(Neon.accent).clickable(onClick = vm::togglePlay),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(if (ui.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Neon.Bg, modifier = Modifier.size(40.dp))
                    }
                    IconButton(onClick = vm::next) { Icon(Icons.Rounded.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
                }
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                    var dragging by remember { mutableStateOf(false) }
                    var dragValue by remember { mutableFloatStateOf(0f) }
                    val dur = ui.durationMs.coerceAtLeast(1)
                    Slider(
                        value = if (dragging) dragValue else (ui.positionMs.toFloat() / dur).coerceIn(0f, 1f),
                        onValueChange = { dragging = true; dragValue = it },
                        onValueChangeFinished = { vm.seekTo((dragValue * dur).toLong()); dragging = false },
                        colors = SliderDefaults.colors(thumbColor = Neon.Cyan, activeTrackColor = Neon.Cyan, inactiveTrackColor = Color.White.copy(0.3f)),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${formatTime(ui.positionMs)} / ${formatTime(ui.durationMs)}",
                            color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onExit) { Icon(Icons.Rounded.FullscreenExit, "Keluar layar penuh", tint = Color.White) }
                    }
                }
            }
        }
    }
}
