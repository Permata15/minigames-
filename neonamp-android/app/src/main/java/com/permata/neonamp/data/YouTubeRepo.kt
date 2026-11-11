package com.permata.neonamp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.MediaFormat
import java.util.concurrent.ConcurrentHashMap

/** Pencarian & resolusi stream audio YouTube lewat NewPipeExtractor. */
object YouTubeRepo {
    private val yt get() = ServiceList.YouTube

    data class ResolvedAudio(val url: String, val format: MediaFormat?, val contentLength: Long)

    private data class Cached(val audio: ResolvedAudio, val at: Long)
    private val cache = ConcurrentHashMap<String, Cached>()
    private const val CACHE_MS = 3 * 60 * 60 * 1000L // URL googlevideo berlaku ~6 jam

    suspend fun search(query: String): List<Track> = withContext(Dispatchers.IO) {
        val handler = yt.searchQHFactory.fromQuery(query, listOf("videos"), "")
        val info = SearchInfo.getInfo(yt, handler)
        info.relatedItems
            .filterIsInstance<StreamInfoItem>()
            .filter { it.streamType == StreamType.VIDEO_STREAM || it.streamType == StreamType.AUDIO_STREAM }
            .mapNotNull { item ->
                val id = runCatching { yt.streamLHFactory.getId(item.url) }.getOrNull()
                    ?: return@mapNotNull null
                Track(
                    id = "yt:$id",
                    title = item.name,
                    artist = item.uploaderName ?: "",
                    durationMs = item.duration * 1000L,
                    artworkUrl = bestImage(item.thumbnails),
                    source = Source.YOUTUBE,
                    pageUrl = item.url,
                )
            }
    }

    /** Ambil metadata satu video dari link (dipakai saat user membagikan link YouTube). */
    suspend fun fromUrl(url: String): Track = withContext(Dispatchers.IO) {
        val info = StreamInfo.getInfo(yt, url)
        remember(info)
        Track(
            id = "yt:${info.id}",
            title = info.name,
            artist = info.uploaderName ?: "",
            durationMs = info.duration * 1000L,
            artworkUrl = bestImage(info.thumbnails),
            source = Source.YOUTUBE,
            pageUrl = info.url,
        )
    }

    /** Blocking — dipanggil dari thread loader ExoPlayer / IO. */
    fun resolveAudioBlocking(videoId: String): ResolvedAudio {
        cache[videoId]?.let { if (System.currentTimeMillis() - it.at < CACHE_MS) return it.audio }
        val info = StreamInfo.getInfo(yt, "https://www.youtube.com/watch?v=$videoId")
        return remember(info) ?: error("Tidak ada stream audio untuk video ini")
    }

    private fun remember(info: StreamInfo): ResolvedAudio? {
        val best = pickAudio(info.audioStreams) ?: return null
        val audio = ResolvedAudio(best.content, best.format, best.itagItem?.contentLength ?: -1L)
        cache[info.id] = Cached(audio, System.currentTimeMillis())
        return audio
    }

    private fun pickAudio(streams: List<AudioStream>): AudioStream? {
        val progressive = streams.filter {
            it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.isUrl
        }
        // Utamakan m4a (kompatibel luas), lalu bitrate tertinggi.
        return progressive.filter { it.format == MediaFormat.M4A }.maxByOrNull { it.averageBitrate }
            ?: progressive.maxByOrNull { it.averageBitrate }
    }

    private fun bestImage(images: List<Image>): String? =
        images.maxByOrNull { it.width * it.height }?.url ?: images.firstOrNull()?.url

    fun extractUrl(text: String): String? =
        Regex("""https?://(www\.|m\.|music\.)?(youtube\.com|youtu\.be)/\S+""").find(text)?.value
}
