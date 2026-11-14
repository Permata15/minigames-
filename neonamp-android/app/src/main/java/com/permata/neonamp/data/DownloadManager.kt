package com.permata.neonamp.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.permata.neonamp.playback.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

enum class Kind { AUDIO, VIDEO }

/**
 * Mengunduh dari YouTube agar bisa diputar offline.
 * - AUDIO: satu file audio (m4a) ke koleksi Audio.
 * - VIDEO: file video tanpa suara + file audio TERPISAH ke koleksi Video. Saat diputar,
 *   keduanya digabung oleh ExoPlayer (tanpa proses mux), jadi ringan dan mulus.
 *
 * Tahan gangguan: diunduh per potongan (Range), tiap potongan dicoba ulang, link YouTube
 * yang kedaluwarsa/ditolak (403) diambil ulang otomatis, dan unduhan melanjutkan dari
 * file .part yang sudah ada. Berjalan di foreground service supaya tidak dimatikan sistem.
 */
class DownloadManager(
    private val context: Context,
    private val audioLib: Library,
    private val videoLib: Library,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slots = Semaphore(2) // maks 2 unduhan bersamaan agar tidak dibatasi server

    /** key(kind, id) -> progres 0..1 (atau -1 jika gagal). */
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    /** Judul untuk notifikasi. */
    private val _titles = MutableStateFlow<Map<String, String>>(emptyMap())
    val titles: StateFlow<Map<String, String>> = _titles.asStateFlow()

    /** Pesan untuk ditampilkan ke pengguna (selesai / gagal + alasannya). */
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun library(kind: Kind) = if (kind == Kind.AUDIO) audioLib else videoLib

    fun activeCount() = _progress.value.count { it.value >= 0f }

    fun download(track: Track, kind: Kind) {
        val videoId = track.videoId ?: return
        val key = key(kind, track.id)
        val lib = library(kind)
        if (lib.get(track.id) != null || _progress.value[key]?.let { it >= 0f } == true) return
        _progress.update { it + (key to 0f) }
        _titles.update { it + (key to track.title) }
        startService()
        val report: (Float) -> Unit = { p -> _progress.update { if (key in it) it + (key to p) else it } }

        scope.launch {
            slots.withPermit {
                runCatching {
                    val aExt = YouTubeRepo.resolveAudioBlocking(videoId).format?.suffix ?: "m4a"
                    val audioFile = File(lib.dir, "$videoId.$aExt")
                    var videoFile: File? = null

                    if (kind == Kind.VIDEO) {
                        val first = YouTubeRepo.resolveVideoBlocking(videoId)
                        val audioLen = YouTubeRepo.resolveAudioBlocking(videoId).contentLength
                        val vFile = File(lib.dir, "$videoId.video.${first.format?.suffix ?: "mp4"}")
                        // Bobot progres sesuai ukuran file (video biasanya jauh lebih besar).
                        val vw = if (first.contentLength > 0 && audioLen > 0)
                            first.contentLength.toFloat() / (first.contentLength + audioLen) else 0.85f
                        fetchToFile(vFile, { refresh ->
                            if (refresh) YouTubeRepo.invalidate(videoId)
                            YouTubeRepo.resolveVideoBlocking(videoId).let { it.url to it.contentLength }
                        }) { report(it * vw) }
                        fetchToFile(audioFile, audioSource(videoId)) { report(vw + it * (1 - vw)) }
                        videoFile = vFile
                    } else {
                        fetchToFile(audioFile, audioSource(videoId), report)
                    }

                    // Sampul & lirik juga disimpan supaya lengkap saat offline.
                    val cover = track.artworkUrl?.let { saveCover(it, File(lib.dir, "$videoId.jpg")) }
                    runCatching { Lyrics.fetchAndCache(track, lib.lyricsDir) }

                    lib.add(
                        track.copy(
                            localPath = audioFile.absolutePath,
                            videoPath = videoFile?.absolutePath,
                            wantVideo = kind == Kind.VIDEO,
                            artworkUrl = cover ?: track.artworkUrl,
                        )
                    )
                }.onSuccess {
                    _progress.update { it - key }
                    _titles.update { it - key }
                    _events.tryEmit("✓ Tersimpan offline: ${track.title}")
                }.onFailure { e ->
                    _progress.update { it + (key to -1f) }
                    _events.tryEmit("Gagal unduh \"${track.title}\": ${friendly(e)}")
                }
            }
        }
    }

    private fun audioSource(videoId: String): (Boolean) -> Pair<String, Long> = { refresh ->
        if (refresh) YouTubeRepo.invalidate(videoId)
        YouTubeRepo.resolveAudioBlocking(videoId).let { it.url to it.contentLength }
    }

    fun clearError(track: Track, kind: Kind) {
        val key = key(kind, track.id)
        _progress.update { it - key }
        _titles.update { it - key }
    }

    private fun startService() {
        runCatching {
            ContextCompat.startForegroundService(context, Intent(context, DownloadService::class.java))
        }
    }

    private class HttpStatus(val code: Int) : IOException("HTTP $code")

    /**
     * Unduh ke [out] lewat file .part (bisa dilanjutkan).
     * [source] mengembalikan (url, panjang); argumen true = paksa ambil link baru.
     */
    private suspend fun fetchToFile(
        out: File,
        source: (Boolean) -> Pair<String, Long>,
        onProgress: (Float) -> Unit,
    ) {
        if (out.exists() && out.length() > 0) { onProgress(1f); return }
        val tmp = File(out.parentFile, out.name + ".part")
        var (url, total) = source(false)
        var pos = tmp.length()
        val chunk = 2L shl 20 // 2 MB
        var failures = 0

        RandomAccessFile(tmp, "rw").use { raf ->
            while (total <= 0 || pos < total) {
                val end = if (total > 0) minOf(pos + chunk, total) - 1 else pos + chunk - 1
                try {
                    val req = Request.Builder()
                        .url(url)
                        .header("User-Agent", NetDownloader.USER_AGENT)
                        .header("Accept", "*/*")
                        .header("Accept-Encoding", "identity")
                        .header("Range", "bytes=$pos-$end")
                        .build()
                    var finished = false
                    NetDownloader.client.newCall(req).execute().use { resp ->
                        when {
                            resp.code == 416 -> finished = true // sudah sampai akhir file
                            resp.code == 206 || resp.code == 200 -> {
                                if (resp.code == 200 && pos > 0) {
                                    // Server mengabaikan Range: mulai ulang dari awal.
                                    pos = 0; raf.setLength(0)
                                }
                                if (total <= 0) {
                                    total = resp.header("Content-Range")?.substringAfter('/')?.toLongOrNull()
                                        ?: if (resp.code == 200) resp.body?.contentLength() ?: -1 else -1
                                }
                                raf.seek(pos)
                                val input = resp.body?.byteStream() ?: throw IOException("Respons kosong")
                                val buf = ByteArray(64 * 1024)
                                var got = 0L
                                while (true) {
                                    val n = input.read(buf)
                                    if (n < 0) break
                                    raf.write(buf, 0, n)
                                    pos += n; got += n
                                }
                                if (got == 0L) finished = true
                                if (resp.code == 200) finished = true
                            }
                            else -> throw HttpStatus(resp.code)
                        }
                    }
                    failures = 0
                    if (total > 0) onProgress((pos.toFloat() / total).coerceIn(0f, 1f))
                    if (finished) break
                } catch (e: IOException) {
                    failures++
                    if (failures > 6) throw e
                    // Link YouTube kedaluwarsa / ditolak → ambil link baru, lanjut dari posisi sekarang.
                    if (e is HttpStatus && (e.code == 403 || e.code == 410 || e.code == 404) || failures >= 3) {
                        runCatching { source(true) }.onSuccess { (u, t) -> url = u; if (t > 0) total = t }
                    }
                    delay(1000L shl minOf(failures - 1, 4)) // 1s, 2s, 4s, 8s, 16s
                }
            }
            raf.setLength(pos)
        }
        if (pos <= 0) throw IOException("File kosong")
        if (!tmp.renameTo(out)) throw IOException("Gagal menyimpan file")
    }

    private fun saveCover(url: String, file: File): String? = runCatching {
        if (url.startsWith("file:")) return url
        NetDownloader.client.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) return null
            file.writeBytes(r.body!!.bytes())
        }
        android.net.Uri.fromFile(file).toString()
    }.getOrNull()

    private fun friendly(e: Throwable): String {
        val msg = e.message ?: e.javaClass.simpleName
        return when {
            e is HttpStatus && e.code == 403 -> "ditolak server YouTube (403). Coba lagi nanti."
            e is java.net.UnknownHostException -> "tidak ada koneksi internet"
            e is java.net.SocketTimeoutException -> "koneksi terlalu lambat (timeout)"
            msg.contains("ENOSPC") || msg.contains("No space", true) -> "memori HP penuh"
            msg.contains("bot", true) || msg.contains("sign in", true) -> "YouTube meminta verifikasi, coba lagi nanti"
            else -> msg.take(120)
        }
    }

    companion object {
        fun key(kind: Kind, id: String) = "${kind.name}:$id"
    }
}
