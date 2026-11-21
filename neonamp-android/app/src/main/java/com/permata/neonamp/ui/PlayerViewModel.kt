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
import com.permata.neonamp.data.AudioQuality
import com.permata.neonamp.data.Prefs
import com.permata.neonamp.data.CaptionMode
import com.permata.neonamp.data.Cue
import com.permata.neonamp.data.Subtitles
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

private const val TAG = "NeonAmpTest"

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
        // Kabar unduhan selesai / gagal (beserta alasannya)
        viewModelScope.launch { neon.downloads.events.collect { _message.value = it } }
        viewModelScope.launch {
            val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
            val c = MediaController.Builder(app, token).buildAsync().await()
            controller = c
            _player.value = c
            c.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) = sync()
                override fun onPlayerError(error: PlaybackException) = recover(error)
            })
            sync()
            var tick = 0
            while (true) {
                controller?.let { p ->
                    _ui.update { it.copy(positionMs = p.currentPosition, durationMs = duration(p)) }
                    if (++tick % 20 == 0 && p.isPlaying) {
                        android.util.Log.i(TAG, "playing pos=${p.currentPosition} dur=${p.duration} video=${p.videoSize.width}x${p.videoSize.height} id=${p.currentMediaItem?.mediaId} cap=${Subtitles.at(_cues.value, p.currentPosition)?.replace('\n', ' ') ?: "-"}")
                    }
                }
                delay(250)
            }
        }
    }

    // ---------- Pemulihan otomatis saat gagal putar ----------
    private val attempts = HashMap<String, Int>()

    /**
     * 1) Link YouTube kedaluwarsa/ditolak → ambil link baru & lanjutkan dari posisi yang sama.
     * 2) Video tetap gagal (mis. format tidak didukung HP) → putar audionya saja.
     * 3) Masih gagal → lewati ke lagu berikutnya.
     */
    private fun recover(error: PlaybackException) {
        val c = controller ?: return
        val item = c.currentMediaItem ?: return
        val t = Track.fromMediaItem(item)
        val key = item.mediaId
        val n = attempts[key] ?: 0
        attempts[key] = n + 1
        android.util.Log.w(TAG, "error ${error.errorCodeName} on $key attempt=$n: ${error.cause?.message}")
        val pos = c.currentPosition
        val idx = c.currentMediaItemIndex
        when {
            t != null && t.source == com.permata.neonamp.data.Source.YOUTUBE && n == 0 && t.localPath == null -> {
                t.videoId?.let(YouTubeRepo::invalidate)
                _message.value = "Menyambung ulang…"
                c.seekTo(idx, pos); c.prepare(); c.play()
            }
            t != null && t.wantVideo && n <= 1 -> {
                _message.value = "Video ini tidak bisa diputar di HP ini — memutar audionya saja"
                c.replaceMediaItem(idx, resolveForPlay(t, false).toMediaItem())
                c.seekTo(idx, pos); c.prepare(); c.play()
            }
            c.hasNextMediaItem() -> {
                _message.value = "Gagal memutar \"${t?.title ?: "lagu"}\", lanjut ke berikutnya"
                c.seekToNextMediaItem(); c.prepare(); c.play()
            }
            else -> _message.value = "Gagal memutar: ${friendlyError(error)}"
        }
    }

    private fun friendlyError(e: PlaybackException): String = when (e.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "koneksi internet bermasalah"
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "ditolak server YouTube, coba lagi nanti"
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "format tidak didukung HP ini"
        else -> e.cause?.message ?: e.errorCodeName
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
        val subKey = track?.takeIf { it.wantVideo }?.id
        if (subKey != subsFor) loadSubs(track?.takeIf { it.wantVideo })
        val st = p.playbackState
        if (st != lastState) {
            lastState = st
            android.util.Log.i(TAG, "state=$st playing=${p.isPlaying} id=${p.currentMediaItem?.mediaId}")
        }
    }

    private var lastState = -1

    // ---------- Subtitle / lirik di video ----------
    private val _cues = MutableStateFlow<List<Cue>>(emptyList())
    val cues: StateFlow<List<Cue>> = _cues.asStateFlow()
    private var subsFor: String? = null
    private var subsJob: Job? = null
    val captionMode = Prefs.caption

    private fun loadSubs(track: Track?) {
        subsFor = track?.id
        subsJob?.cancel()
        _cues.value = emptyList()
        if (track == null) return
        subsJob = viewModelScope.launch {
            _cues.value = runCatching { Subtitles.load(track, getApplication<Application>().cacheDir) }.getOrDefault(emptyList())
            android.util.Log.i(TAG, "subtitles ${_cues.value.size} cues for ${track.id}")
        }
    }

    /** Tombol CC: Subtitle → Lirik → Mati → Subtitle … */
    fun cycleCaption() {
        val next = when (Prefs.caption.value) {
            CaptionMode.SUBS -> CaptionMode.LYRICS
            CaptionMode.LYRICS -> CaptionMode.OFF
            CaptionMode.OFF -> CaptionMode.SUBS
        }
        Prefs.setCaption(next)
        _message.value = when (next) {
            CaptionMode.SUBS -> if (_cues.value.isEmpty()) "Subtitle: video ini tidak punya subtitle" else "Subtitle: aktif"
            CaptionMode.LYRICS -> "Subtitle: lirik lagu"
            CaptionMode.OFF -> "Subtitle: mati"
        }
    }

    // ---------- Stabilizer ----------
    val volumeStabilizer = Prefs.volumeStabilizer
    val stability = Prefs.stability

    // ---------- Pilih ukuran unduhan ----------
    private val _picker = MutableStateFlow<Pair<Track, Kind>?>(null)
    val picker: StateFlow<Pair<Track, Kind>?> = _picker.asStateFlow()
    fun openDownloadPicker(track: Track, kind: Kind) { _picker.value = track to kind }
    fun closeDownloadPicker() { _picker.value = null }
    suspend fun downloadOptions(track: Track): YouTubeRepo.DownloadOptions? =
        track.videoId?.let { id -> runCatching { YouTubeRepo.downloadOptions(id) }.getOrNull() }

    private val _openPlayer = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val openPlayer: kotlinx.coroutines.flow.SharedFlow<Unit> = _openPlayer

    // ---------- Hook uji otomatis (dipakai workflow emulator) ----------
    fun testAction(url: String, video: Boolean, download: String?) {
        viewModelScope.launch {
            runCatching { YouTubeRepo.fromUrl(url) }
                .onSuccess { t ->
                    android.util.Log.i(TAG, "test resolved ${t.id} '${t.title}'")
                    when (download) {
                        "audio" -> download(t, Kind.AUDIO)
                        "video" -> download(t, Kind.VIDEO)
                        else -> { playList(listOf(t), 0, video); _openPlayer.tryEmit(Unit) }
                    }
                }
                .onFailure { android.util.Log.e(TAG, "test failed: $it") }
        }
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
    val searchFilter = MutableStateFlow(YouTubeRepo.Filter.SONGS)
    private var page: YouTubeRepo.SearchPage? = null
    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    private var suggestJob: Job? = null
    private var searchJob: Job? = null

    fun onQueryChange(q: String) {
        query.value = q
        suggestJob?.cancel()
        if (q.trim().length < 2 || YouTubeRepo.extractUrl(q) != null) {
            _suggestions.value = emptyList(); return
        }
        suggestJob = viewModelScope.launch {
            delay(280)
            _suggestions.value = YouTubeRepo.suggestions(q.trim())
        }
    }

    fun clearSuggestions() { suggestJob?.cancel(); _suggestions.value = emptyList() }

    fun pickSuggestion(s: String) {
        query.value = s
        search()
    }

    fun setFilter(f: YouTubeRepo.Filter) {
        if (searchFilter.value == f) return
        searchFilter.value = f
        if (query.value.isNotBlank()) search()
    }

    fun search() {
        val q = query.value.trim()
        if (q.isEmpty()) return
        clearSuggestions()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searching.value = true
            _results.value = emptyList()
            _hasMore.value = false
            runCatching {
                val url = YouTubeRepo.extractUrl(q)
                if (url != null) {
                    page = null
                    listOf(YouTubeRepo.fromUrl(url))
                } else {
                    val filter = searchFilter.value
                    val p = runCatching { YouTubeRepo.search(q, filter) }.getOrElse { e ->
                        // Katalog YouTube Music kadang tidak tersedia → pakai pencarian video biasa.
                        if (filter == YouTubeRepo.Filter.VIDEOS || e is kotlinx.coroutines.CancellationException) throw e
                        _message.value = "Sumber \"${filter.label}\" sedang tidak tersedia, memakai Video"
                        YouTubeRepo.search(q, YouTubeRepo.Filter.VIDEOS)
                    }
                    page = p
                    _hasMore.value = p.hasMore
                    p.tracks
                }
            }.onSuccess {
                _results.value = it
                if (it.isEmpty()) _message.value = "Tidak ada hasil untuk \"$q\""
            }.onFailure {
                if (it is kotlinx.coroutines.CancellationException) throw it
                _message.value = "Pencarian gagal: ${it.message ?: "periksa koneksi"}"
            }
            _searching.value = false
        }
    }

    /** Muat halaman hasil berikutnya saat daftar digulir ke bawah. */
    fun loadMore() {
        val p = page ?: return
        if (!p.hasMore || _loadingMore.value || _searching.value) return
        viewModelScope.launch {
            _loadingMore.value = true
            runCatching { YouTubeRepo.more(p) }
                .onSuccess { next ->
                    page = next
                    _hasMore.value = next.hasMore
                    val seen = _results.value.map { it.id }.toHashSet()
                    _results.value = _results.value + next.tracks.filter { seen.add(it.id) }
                }
                .onFailure { _hasMore.value = false }
            _loadingMore.value = false
        }
    }

    // ---------- Kualitas ----------
    val audioQuality = Prefs.audio
    val videoMax = Prefs.videoMax
    fun setAudioQuality(q: AudioQuality) = Prefs.setAudio(q)
    fun setVideoMax(h: Int) = Prefs.setVideoMax(h)

    // ---------- Grup (playlist) audio offline ----------
    val playlists = neon.playlists.playlists
    fun createPlaylist(name: String, track: Track? = null) {
        neon.playlists.create(name, track?.id)
        _message.value = if (track != null) "Grup \"$name\" dibuat & lagu ditambahkan" else "Grup \"$name\" dibuat"
    }
    fun renamePlaylist(id: String, name: String) = neon.playlists.rename(id, name)
    fun deletePlaylist(id: String) { neon.playlists.delete(id); _message.value = "Grup dihapus (lagunya tetap ada)" }
    fun togglePlaylist(id: String, track: Track) = neon.playlists.toggle(id, track.id)

    fun handleSharedText(text: String) {
        val url = YouTubeRepo.extractUrl(text) ?: return
        query.value = url
        search()
    }

    fun download(track: Track, kind: Kind, videoItag: Int? = null, audioItag: Int? = null) {
        _picker.value = null
        neon.downloads.download(track, kind, videoItag, audioItag)
        _message.value = (if (kind == Kind.VIDEO) "Mengunduh video" else "Mengunduh audio") + " \"${track.title}\""
    }

    fun retryDownload(track: Track, kind: Kind) {
        neon.downloads.clearError(track, kind)
        neon.downloads.download(track, kind)
    }

    fun deleteOffline(track: Track, kind: Kind) {
        neon.downloads.library(kind).remove(track)
        if (kind == Kind.AUDIO) neon.playlists.removeTrackEverywhere(track.id)
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
