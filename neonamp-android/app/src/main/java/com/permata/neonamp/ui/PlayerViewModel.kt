package com.permata.neonamp.ui

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.permata.neonamp.NeonAmpApp
import com.permata.neonamp.data.Kind
import com.permata.neonamp.data.LocalMusic
import com.permata.neonamp.data.Lyrics
import com.permata.neonamp.data.LyricsResult
import com.permata.neonamp.data.Track
import com.permata.neonamp.data.YouTubeRepo
import com.permata.neonamp.playback.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

data class PlayerUi(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val buffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val hasNext: Boolean = false,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1,
)

sealed interface LyricsState {
    data object Idle : LyricsState
    data object Loading : LyricsState
    data object NotFound : LyricsState
    data class Ready(val result: LyricsResult) : LyricsState
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val neon = app as NeonAmpApp
    val audioLibrary = neon.audioLib.tracks
    val videoLibrary = neon.videoLib.tracks
    val downloads = neon.downloads.progress

    private var controller: MediaController? = null
    private val _player = MutableStateFlow<Player?>(null)
    /** Untuk menempelkan layar video (PlayerView). */
    val player: StateFlow<Player?> = _player.asStateFlow()

    private val _ui = MutableStateFlow(PlayerUi())
    val ui: StateFlow<PlayerUi> = _ui.asStateFlow()

    private val _lyrics = MutableStateFlow<LyricsState>(LyricsState.Idle)
    val lyrics: StateFlow<LyricsState> = _lyrics.asStateFlow()

    val query = MutableStateFlow("")
    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private val _deviceTracks = MutableStateFlow<List<Track>>(emptyList())
    val deviceTracks: StateFlow<List<Track>> = _deviceTracks.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var lyricsJob: Job? = null
    private var lyricsFor: String? = null

    init {
        viewModelScope.launch {
            val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
            val c = MediaController.Builder(app, token).buildAsync().await()
            controller = c
            _player.value = c
            c.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) = sync()
                override fun onPlayerError(error: PlaybackException) {
                    _message.value = "Gagal memutar: ${error.cause?.message ?: error.errorCodeName}"
                }
            })
            sync()
            while (true) {
                controller?.let { p ->
                    _ui.update { it.copy(positionMs = p.currentPosition, durationMs = duration(p)) }
                }
                delay(250)
            }
        }
    }

    private fun duration(p: Player): Long {
        val d = p.duration
        return if (d > 0) d else _ui.value.track?.durationMs ?: 0
    }

    private fun sync() {
        val p = controller ?: return
        val queue = (0 until p.mediaItemCount).mapNotNull { Track.fromMediaItem(p.getMediaItemAt(it)) }
        val track = Track.fromMediaItem(p.currentMediaItem)
        _ui.value = PlayerUi(
            track = track,
            isPlaying = p.isPlaying,
            buffering = p.playbackState == Player.STATE_BUFFERING,
            positionMs = p.currentPosition,
            durationMs = duration(p),
            shuffle = p.shuffleModeEnabled,
            repeatMode = p.repeatMode,
            hasNext = p.hasNextMediaItem(),
            queue = queue,
            queueIndex = p.currentMediaItemIndex,
        )
        if (track?.id != lyricsFor) loadLyrics(track)
    }

    private fun loadLyrics(track: Track?) {
        lyricsFor = track?.id
        lyricsJob?.cancel()
        if (track == null) {
            _lyrics.value = LyricsState.Idle; return
        }
        _lyrics.value = LyricsState.Loading
        lyricsJob = viewModelScope.launch {
            val res = Lyrics.load(track, neon.audioLib.lyricsDir)
            _lyrics.value = if (res == null || res.lines.isEmpty()) LyricsState.NotFound else LyricsState.Ready(res)
        }
    }

    // ---------- Kontrol ----------
    /** Versi terbaik untuk diputar: file offline bila ada, sesuai mode audio/video. */
    private fun resolveForPlay(track: Track, video: Boolean): Track {
        if (track.source != com.permata.neonamp.data.Source.YOUTUBE) return track.copy(wantVideo = false)
        return if (video) {
            neon.videoLib.get(track.id) ?: track.copy(wantVideo = true, videoPath = null)
        } else {
            neon.audioLib.get(track.id)
                // Video offline juga punya file audio sendiri → bisa diputar audio saja tanpa internet.
                ?: neon.videoLib.get(track.id)?.copy(wantVideo = false)
                ?: track.copy(wantVideo = false, videoPath = null, localPath = null)
        }
    }

    fun playList(list: List<Track>, index: Int, video: Boolean = false) {
        val c = controller ?: return
        val items: List<MediaItem> = list.map { resolveForPlay(it, video).toMediaItem() }
        c.setMediaItems(items, index, 0L)
        c.prepare()
        c.play()
    }

    fun playQueueIndex(index: Int) {
        controller?.run { seekTo(index, 0L); play() }
    }

    fun addToQueue(track: Track) {
        val c = controller ?: return
        c.addMediaItem(resolveForPlay(track, false).toMediaItem())
        if (c.mediaItemCount == 1) { c.prepare(); c.play() }
        _message.value = "Ditambahkan ke antrean"
    }

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    /** Ganti lagu yang sedang diputar antara mode video ⇄ audio saja, posisi tetap. */
    fun toggleVideoMode() {
        val c = controller ?: return
        val t = Track.fromMediaItem(c.currentMediaItem) ?: return
        if (t.source != com.permata.neonamp.data.Source.YOUTUBE) return
        val index = c.currentMediaItemIndex
        val pos = c.currentPosition
        val wasPlaying = c.playWhenReady
        c.replaceMediaItem(index, resolveForPlay(t, !t.wantVideo).toMediaItem())
        c.seekTo(index, pos)
        c.playWhenReady = wasPlaying
    }

    fun next() { controller?.seekToNext() }
    fun previous() { controller?.seekToPrevious() }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }
    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
    fun cycleRepeat() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    // ---------- YouTube ----------
    fun search() {
        val q = query.value.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            _searching.value = true
            runCatching {
                val url = YouTubeRepo.extractUrl(q)
                if (url != null) listOf(YouTubeRepo.fromUrl(url)) else YouTubeRepo.search(q)
            }.onSuccess { _results.value = it }
                .onFailure { _message.value = "Pencarian gagal: ${it.message ?: "periksa koneksi"}" }
            _searching.value = false
        }
    }

    fun handleSharedText(text: String) {
        val url = YouTubeRepo.extractUrl(text) ?: return
        query.value = url
        search()
    }

    fun download(track: Track, kind: Kind) {
        neon.downloads.download(track, kind)
        _message.value = (if (kind == Kind.VIDEO) "Mengunduh video" else "Mengunduh audio") + " \"${track.title}\""
    }

    fun retryDownload(track: Track, kind: Kind) {
        neon.downloads.clearError(track, kind)
        neon.downloads.download(track, kind)
    }

    fun deleteOffline(track: Track, kind: Kind) {
        neon.downloads.library(kind).remove(track)
        _message.value = if (kind == Kind.VIDEO) "Video dihapus" else "Audio dihapus"
    }

    // ---------- Musik di HP ----------
    fun scanDevice() {
        viewModelScope.launch {
            runCatching { LocalMusic.scan(getApplication()) }
                .onSuccess {
                    _deviceTracks.value = it
                    if (it.isEmpty()) _message.value = "Tidak ada lagu ditemukan di HP"
                }
                .onFailure { _message.value = "Gagal membaca musik: ${it.message}" }
        }
    }

    fun consumeMessage() { _message.value = null }

    override fun onCleared() {
        controller?.release()
        super.onCleared()
    }
}
