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

/**
 * Service pemutar di background (notifikasi, lock screen, tombol headset).
 * URI "yt://<videoId>" di-resolve menjadi URL audio YouTube tepat sebelum diputar.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val http = OkHttpDataSource.Factory(NetDownloader.client)
            .setUserAgent(NetDownloader.USER_AGENT)
        val upstream = DefaultDataSource.Factory(this, http)
        val resolving = ResolvingDataSource.Factory(upstream) { spec: DataSpec ->
            val uri = spec.uri
            if (uri.scheme == "yt") {
                val audio = try {
                    YouTubeRepo.resolveAudioBlocking(uri.host ?: "")
                } catch (e: Exception) {
                    throw java.io.IOException("Gagal mengambil audio YouTube: ${e.message}", e)
                }
                spec.withUri(android.net.Uri.parse(audio.url))
            } else spec
        }

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(resolving))
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

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
