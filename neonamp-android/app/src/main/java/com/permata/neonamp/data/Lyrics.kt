package com.permata.neonamp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.abs

data class LyricLine(val timeMs: Long, val text: String)

data class LyricsResult(
    val lines: List<LyricLine>,
    /** true jika lirik punya timestamp (bisa ikut berjalan sesuai lagu). */
    val synced: Boolean,
)

/** Lirik dari LRCLIB (gratis, tanpa API key) + cache file .lrc untuk offline. */
object Lyrics {
    private const val API = "https://lrclib.net/api"

    suspend fun load(track: Track, cacheDir: File): LyricsResult? = withContext(Dispatchers.IO) {
        val cached = cacheFile(track, cacheDir)
        if (cached.exists()) return@withContext parse(cached.readText())
        runCatching { fetchAndCache(track, cacheDir) }.getOrNull()
    }

    fun fetchAndCache(track: Track, cacheDir: File): LyricsResult? {
        val (artist, title) = guessArtistTitle(track)
        val raw = fetch(artist, title, track.durationMs / 1000) ?: return null
        cacheFile(track, cacheDir).writeText(raw)
        return parse(raw)
    }

    private fun cacheFile(track: Track, dir: File) =
        File(dir, track.id.replace(Regex("[^A-Za-z0-9_-]"), "_") + ".lrc")

    private fun fetch(artist: String, title: String, durationSec: Long): String? {
        // 1) Cocok persis
        if (artist.isNotBlank()) {
            val url = "$API/get".toHttpUrl().newBuilder()
                .addQueryParameter("artist_name", artist)
                .addQueryParameter("track_name", title)
                .apply { if (durationSec > 0) addQueryParameter("duration", durationSec.toString()) }
                .build()
            get(url.toString())?.let { body ->
                pick(JSONObject(body))?.let { return it }
            }
        }
        // 2) Pencarian bebas, pilih yang durasinya paling dekat & punya lirik sinkron
        val url = "$API/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", "$artist $title".trim())
            .build()
        val body = get(url.toString()) ?: return null
        val arr = JSONArray(body)
        val items = (0 until arr.length()).map { arr.getJSONObject(it) }
        val sorted = items.sortedWith(
            compareByDescending<JSONObject> { !it.isNull("syncedLyrics") }
                .thenBy { if (durationSec > 0) abs(it.optDouble("duration", 0.0) - durationSec) else 0.0 }
        )
        return sorted.firstNotNullOfOrNull { pick(it) }
    }

    private fun pick(o: JSONObject): String? {
        val synced = o.optString("syncedLyrics").takeIf { !o.isNull("syncedLyrics") && it.isNotBlank() }
        val plain = o.optString("plainLyrics").takeIf { !o.isNull("plainLyrics") && it.isNotBlank() }
        return synced ?: plain
    }

    private fun get(url: String): String? {
        val req = Request.Builder().url(url).header("User-Agent", "NeonAmp/1.0").build()
        NetDownloader.client.newCall(req).execute().use { r ->
            return if (r.isSuccessful) r.body?.string() else null
        }
    }

    private val tag = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")

    fun parse(raw: String): LyricsResult {
        val lines = mutableListOf<LyricLine>()
        var synced = false
        raw.lineSequence().forEach { line ->
            val tags = tag.findAll(line).toList()
            if (tags.isEmpty()) {
                if (line.isNotBlank() && !line.trimStart().startsWith("[")) lines += LyricLine(-1, line.trim())
                return@forEach
            }
            synced = true
            val text = line.substring(tags.last().range.last + 1).trim()
            tags.forEach { m ->
                val min = m.groupValues[1].toLong()
                val sec = m.groupValues[2].toLong()
                val frac = m.groupValues[3]
                val ms = when (frac.length) {
                    0 -> 0L
                    1 -> frac.toLong() * 100
                    2 -> frac.toLong() * 10
                    else -> frac.take(3).toLong()
                }
                lines += LyricLine(min * 60_000 + sec * 1000 + ms, text)
            }
        }
        return if (synced) {
            LyricsResult(lines.filter { it.timeMs >= 0 }.sortedBy { it.timeMs }, true)
        } else {
            LyricsResult(lines, false)
        }
    }

    /** Bersihkan judul YouTube: "Artis - Lagu (Official Video)" -> ("Artis", "Lagu"). */
    fun guessArtistTitle(track: Track): Pair<String, String> {
        var t = track.title
            .replace(Regex("""\s*[(\[][^)\]]*(official|lyric|lirik|video|audio|mv|hd|4k|live|visualizer)[^)\]]*[)\]]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\|.*$"""), "")
            .replace(Regex("""(?i)\s*(official\s+(music\s+)?video|lyrics?|lirik)\s*$"""), "")
            .trim()
        var artist = track.artist.removeSuffix(" - Topic").replace(Regex("(?i)vevo$"), "").trim()
        val dash = Regex("""\s+[-–—]\s+""").find(t)
        if (dash != null) {
            artist = t.substring(0, dash.range.first).trim()
            t = t.substring(dash.range.last + 1).trim()
        }
        t = t.replace(Regex("""(?i)\s*(ft\.|feat\.).*$"""), "").trim()
        return artist to t
    }
}
