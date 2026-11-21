package com.permata.neonamp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

data class Cue(val startMs: Long, val endMs: Long, val text: String)

/** Subtitle YouTube (WebVTT): ambil, simpan untuk offline, dan parse jadi potongan teks berwaktu. */
object Subtitles {
    suspend fun load(track: Track, cacheDir: File): List<Cue> = withContext(Dispatchers.IO) {
        track.subtitlePath?.let { p -> File(p).takeIf { it.exists() }?.let { return@withContext parse(it.readText()) } }
        val id = track.videoId ?: return@withContext emptyList()
        val cached = File(cacheDir, "$id.vtt")
        if (cached.exists()) return@withContext parse(cached.readText())
        val text = fetch(id) ?: return@withContext emptyList()
        runCatching { cached.writeText(text) }
        parse(text)
    }

    /** Unduh teks VTT (dipakai juga saat menyimpan video offline). */
    fun fetch(videoId: String): String? {
        val sub = YouTubeRepo.subtitleBlocking(videoId) ?: return null
        val req = Request.Builder().url(sub.url).header("User-Agent", NetDownloader.USER_AGENT).build()
        return runCatching {
            NetDownloader.client.newCall(req).execute().use { r -> if (r.isSuccessful) r.body?.string() else null }
        }.getOrNull()?.takeIf { it.contains("-->") }
    }

    private val time = Regex("""(?:(\d+):)?(\d{1,2}):(\d{2})[.,](\d{3})""")
    private val tags = Regex("<[^>]+>")

    private fun ms(m: MatchResult): Long {
        val h = m.groupValues[1].toLongOrNull() ?: 0
        return ((h * 60 + m.groupValues[2].toLong()) * 60 + m.groupValues[3].toLong()) * 1000 + m.groupValues[4].toLong()
    }

    fun parse(vtt: String): List<Cue> {
        val auto = vtt.contains("<c>") || vtt.contains("align:start position:0%")
        val cues = mutableListOf<Cue>()
        val lines = vtt.replace("\r", "").split("\n")
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if ("-->" in line) {
                val parts = line.split("-->")
                val s = time.find(parts[0]); val e = time.find(parts.getOrElse(1) { "" })
                i++
                val text = StringBuilder()
                while (i < lines.size && lines[i].isNotBlank()) {
                    val t = lines[i].replace(tags, "").replace("&amp;", "&").replace("&gt;", ">").replace("&lt;", "<").replace("&nbsp;", " ").trim()
                    if (t.isNotEmpty()) { if (text.isNotEmpty()) text.append('\n'); text.append(t) }
                    i++
                }
                if (s != null && e != null && text.isNotEmpty()) cues += Cue(ms(s), ms(e), text.toString())
            } else i++
        }
        // Subtitle otomatis YouTube mengulang baris sebelumnya; ambil baris terakhir saja agar bersih.
        return cues.map { c ->
            val ls = c.text.lines()
            if (auto && ls.size > 1) c.copy(text = ls.last()) else c
        }.filter { it.endMs - it.startMs > 20 }
    }

    fun at(cues: List<Cue>, pos: Long): String? = cues.lastOrNull { pos >= it.startMs && pos < it.endMs }?.text
}
