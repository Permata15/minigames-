package com.permata.neonamp.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.permata.neonamp.MainActivity
import com.permata.neonamp.data.NetDownloader
import com.permata.neonamp.data.YouTubeRepo
import com.permata.neonamp.data.Prefs
import com.permata.neonamp.data.StreamStability
import androidx.media3.exoplayer.DefaultLoadControl
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.permata.neonamp.data.Track

/**
 * Service pemutar di background (notifikasi, lock screen, tombol headset).
 * URI "yt://<videoId>" di-resolve menjadi URL audio YouTube tepat sebelum diputar.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null
    private val stabilizer = VolumeStabilizer()
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val http = OkHttpDataSource.Factory(NetDownloader.client)
            .setUserAgent(NetDownloader.USER_AGENT)
        // Stream YouTube diambil per potongan (seperti NewPipe) agar video tidak macet.
        val upstream = DefaultDataSource.Factory(this, ChunkedDataSource.Factory(http))
        val resolving = ResolvingDataSource.Factory(upstream) { spec: DataSpec ->
            val uri = spec.uri
            when (uri.scheme) {
                "yt" -> spec.withUri(android.net.Uri.parse(resolve("audio") {
                    YouTubeRepo.resolveAudioBlocking(uri.host ?: "").url
                }))
                "ytv" -> spec.withUri(android.net.Uri.parse(resolve("video") {
                    YouTubeRepo.resolveVideoBlocking(uri.host ?: "").url
                }))
                else -> spec
            }
        }

        // Penstabil streaming: buffer besar agar tidak putus-putus di sinyal lemah.
        val loadControl = if (Prefs.stability.value == StreamStability.STABLE) {
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(30_000, 120_000, 2_000, 5_000)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        } else DefaultLoadControl()

        val player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(AvMediaSourceFactory(DefaultMediaSourceFactory(resolving), resolving))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        // Penstabil volume (bisa dinyalakan/dimatikan dari Pengaturan)
        stabilizer.attach(player.audioSessionId)
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) = stabilizer.attach(audioSessionId)
        })
        scope.launch { Prefs.volumeStabilizer.collect { stabilizer.setEnabled(it) } }

        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        session = MediaSession.Builder(this, player)
            .setSessionActivity(openApp)
            .setCallback(object : MediaSession.Callback {
                // MediaController tidak membawa URI lintas proses; pulihkan dari requestMetadata.
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>
                ): ListenableFuture<MutableList<MediaItem>> {
                    val fixed = mediaItems.map { item ->
                        val uri = item.requestMetadata.mediaUri
                        if (item.localConfiguration == null && uri != null) {
                            item.buildUpon().setUri(uri).build()
                        } else item
                    }.toMutableList()
                    return Futures.immediateFuture(fixed)
                }
            })
            .build()
    }

    private inline fun resolve(what: String, block: () -> String): String = try {
        block()
    } catch (e: Exception) {
        throw java.io.IOException("Gagal mengambil $what YouTube: ${e.message}", e)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        stabilizer.release()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}

/**
 * Video dan audio diputar dari sumber TERPISAH lalu digabung (MergingMediaSource):
 * - stream: video-only ≤720p H.264 + audio m4a terbaik → mulai cepat, buffer ringan
 * - offline: file video tanpa suara + file audio
 * Sumber audio diletakkan pertama supaya metadata lagu (judul, lirik, dst.) tetap terbaca.
 */
@OptIn(UnstableApi::class)
private class AvMediaSourceFactory(
    private val base: DefaultMediaSourceFactory,
    private val dataSource: DataSource.Factory,
) : MediaSource.Factory by base {
    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        val audio = base.createMediaSource(mediaItem)
        val videoUri = Track.fromMediaItem(mediaItem)?.videoUri() ?: return audio
        val video = ProgressiveMediaSource.Factory(dataSource)
            .createMediaSource(MediaItem.fromUri(videoUri))
        return MergingMediaSource(audio, video)
    }
}
