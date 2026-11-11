package com.permata.neonamp.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

/**
 * Mengunduh audio YouTube ke penyimpanan aplikasi agar bisa diputar offline.
 * Diunduh per potongan (Range) karena server YouTube membatasi kecepatan request besar.
 */
class DownloadManager(private val context: Context, private val library: Library) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** id lagu -> progres 0..1 (atau -1 jika gagal). */
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    fun download(track: Track) {
        val videoId = track.videoId ?: return
        if (library.get(track.id) != null || _progress.value[track.id]?.let { it >= 0f } == true) return
        _progress.update { it + (track.id to 0f) }

        scope.launch {
            runCatching {
                val audio = YouTubeRepo.resolveAudioBlocking(videoId)
                val ext = audio.format?.suffix ?: "m4a"
                val out = File(library.musicDir, "$videoId.$ext")
                val tmp = File(library.musicDir, "$videoId.$ext.part")
                fetchRanged(audio.url, audio.contentLength, tmp) { p ->
                    _progress.update { it + (track.id to p) }
                }
                tmp.renameTo(out)

                // Simpan sampul & lirik juga supaya lengkap saat offline.
                val cover = track.artworkUrl?.let { saveCover(it, videoId) }
                runCatching { Lyrics.fetchAndCache(track, library.lyricsDir) }

                library.add(
                    track.copy(
                        localPath = out.absolutePath,
                        artworkUrl = cover ?: track.artworkUrl,
                    )
                )
            }.onSuccess {
                _progress.update { it - track.id }
            }.onFailure {
                _progress.update { it + (track.id to -1f) }
            }
        }
    }

    fun clearError(id: String) = _progress.update { it - id }

    private fun fetchRanged(url: String, knownLength: Long, out: File, onProgress: (Float) -> Unit) {
        val chunk = 1L shl 20 // 1 MB
        var total = knownLength
        var pos = 0L
        RandomAccessFile(out, "rw").use { raf ->
            raf.setLength(0)
            while (total < 0 || pos < total) {
                val end = pos + chunk - 1
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", NetDownloader.USER_AGENT)
                    .header("Range", "bytes=$pos-$end")
                    .build()
                NetDownloader.client.newCall(req).execute().use { resp ->
                    if (resp.code == 416) return
                    if (!resp.isSuccessful) error("HTTP ${resp.code}")
                    if (total < 0) {
                        total = resp.header("Content-Range")?.substringAfter('/')?.toLongOrNull()
                            ?: resp.body!!.contentLength()
                    }
                    val bytes = resp.body!!.bytes()
                    if (bytes.isEmpty()) return
                    raf.write(bytes)
                    pos += bytes.size
                    // Server tanpa dukungan Range mengirim seluruh file sekaligus.
                    if (resp.code == 200) return
                }
                if (total > 0) onProgress((pos.toFloat() / total).coerceIn(0f, 1f))
            }
        }
    }

    private fun saveCover(url: String, videoId: String): String? = runCatching {
        val file = File(library.musicDir, "$videoId.jpg")
        NetDownloader.client.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) return null
            file.writeBytes(r.body!!.bytes())
        }
        android.net.Uri.fromFile(file).toString()
    }.getOrNull()
}
