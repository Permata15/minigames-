package com.permata.neonamp.data

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

enum class Kind { AUDIO, VIDEO }

/**
 * Mengunduh dari YouTube agar bisa diputar offline.
 * - AUDIO: satu file audio (m4a) ke koleksi Audio.
 * - VIDEO: file video tanpa suara + file audio TERPISAH ke koleksi Video. Saat diputar,
 *   keduanya digabung oleh ExoPlayer (tanpa proses mux), jadi ringan dan mulus.
 * Diunduh per potongan (Range) karena server YouTube membatasi kecepatan request besar.
 */
class DownloadManager(private val audioLib: Library, private val videoLib: Library) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** key(kind, id) -> progres 0..1 (atau -1 jika gagal). */
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress.asStateFlow()

    fun library(kind: Kind) = if (kind == Kind.AUDIO) audioLib else videoLib

    fun download(track: Track, kind: Kind) {
        val videoId = track.videoId ?: return
        val key = key(kind, track.id)
        val lib = library(kind)
        if (lib.get(track.id) != null || _progress.value[key]?.let { it >= 0f } == true) return
        _progress.update { it + (key to 0f) }
        val report: (Float) -> Unit = { p -> _progress.update { it + (key to p) } }

        scope.launch {
            runCatching {
                val audio = YouTubeRepo.resolveAudioBlocking(videoId)
                val aExt = audio.format?.suffix ?: "m4a"
                val audioFile = File(lib.dir, "$videoId.$aExt")
                var videoFile: File? = null

                if (kind == Kind.VIDEO) {
                    val video = YouTubeRepo.resolveVideoBlocking(videoId)
                    val vFile = File(lib.dir, "$videoId.video.${video.format?.suffix ?: "mp4"}")
                    // Bobot progres sesuai ukuran file (video biasanya jauh lebih besar).
                    val vw = if (video.contentLength > 0 && audio.contentLength > 0)
                        video.contentLength.toFloat() / (video.contentLength + audio.contentLength) else 0.85f
                    fetchToFile(video.url, video.contentLength, vFile) { report(it * vw) }
                    fetchToFile(audio.url, audio.contentLength, audioFile) { report(vw + it * (1 - vw)) }
                    videoFile = vFile
                } else {
                    fetchToFile(audio.url, audio.contentLength, audioFile, report)
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
            }.onFailure {
                _progress.update { it + (key to -1f) }
            }
        }
    }

    fun clearError(track: Track, kind: Kind) = _progress.update { it - key(kind, track.id) }

    private fun fetchToFile(url: String, length: Long, out: File, onProgress: (Float) -> Unit) {
        val tmp = File(out.parentFile, out.name + ".part")
        fetchRanged(url, length, tmp, onProgress)
        if (!tmp.renameTo(out)) error("Gagal menyimpan file")
    }

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

    private fun saveCover(url: String, file: File): String? = runCatching {
        NetDownloader.client.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) return null
            file.writeBytes(r.body!!.bytes())
        }
        android.net.Uri.fromFile(file).toString()
    }.getOrNull()

    companion object {
        fun key(kind: Kind, id: String) = "${kind.name}:$id"
    }
}
