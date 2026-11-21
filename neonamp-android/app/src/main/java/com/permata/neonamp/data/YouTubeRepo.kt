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
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.stream.AudioTrackType
import java.util.concurrent.ConcurrentHashMap

/** Pencarian & resolusi stream audio YouTube lewat NewPipeExtractor. */
object YouTubeRepo {
    private val yt get() = ServiceList.YouTube

    data class ResolvedAudio(val url: String, val format: MediaFormat?, val contentLength: Long)
    data class ResolvedVideo(val url: String, val format: MediaFormat?, val contentLength: Long, val height: Int)

    private data class Cached(val info: StreamInfo, val at: Long)
    private val cache = ConcurrentHashMap<String, Cached>()
    private const val CACHE_MS = 3 * 60 * 60 * 1000L // URL googlevideo berlaku ~6 jam

    /** Sumber pencarian. "Lagu" memakai katalog YouTube Music (rekaman resmi, audio bagus). */
    enum class Filter(val label: String, val contentFilter: String) {
        SONGS("Lagu", "music_songs"),
        VIDEOS("Video", "videos"),
        MUSIC_VIDEOS("Video Musik", "music_videos"),
        ALL("Semua", "all"),
    }

    class SearchPage(
        val tracks: List<Track>,
        internal val handler: SearchQueryHandler,
        internal val next: Page?,
    ) {
        val hasMore: Boolean get() = next != null
    }

    suspend fun search(query: String, filter: Filter = Filter.VIDEOS): SearchPage = withContext(Dispatchers.IO) {
        val handler = yt.searchQHFactory.fromQuery(query, listOf(filter.contentFilter), "")
        val info = SearchInfo.getInfo(yt, handler)
        SearchPage(toTracks(info.relatedItems), handler, info.nextPage?.takeIf { Page.isValid(it) })
    }

    /** Halaman hasil berikutnya (gulir terus = pencarian lebih luas). */
    suspend fun more(page: SearchPage): SearchPage = withContext(Dispatchers.IO) {
        val next = page.next ?: return@withContext SearchPage(emptyList(), page.handler, null)
        val res = SearchInfo.getMoreItems(yt, page.handler, next)
        SearchPage(toTracks(res.items), page.handler, res.nextPage?.takeIf { res.hasNextPage() && Page.isValid(it) })
    }

    suspend fun suggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        runCatching { yt.suggestionExtractor.suggestionList(query).take(8) }.getOrDefault(emptyList())
    }

    private fun toTracks(items: List<InfoItem>): List<Track> = items
        .filterIsInstance<StreamInfoItem>()
        .filter { it.streamType == StreamType.VIDEO_STREAM || it.streamType == StreamType.AUDIO_STREAM }
        .filterNot { runCatching { it.isShortFormContent }.getOrDefault(false) } // buang Shorts
        .mapNotNull { item ->
            val id = runCatching { yt.streamLHFactory.getId(item.url) }.getOrNull()
                ?: return@mapNotNull null
            Track(
                id = "yt:$id",
                title = item.name,
                artist = (item.uploaderName ?: "").removeSuffix(" - Topic"),
                durationMs = item.duration * 1000L,
                artworkUrl = bestImage(item.thumbnails),
                source = Source.YOUTUBE,
                pageUrl = item.url,
            )
        }

    fun clearCache() = cache.clear()

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

    /** Lupakan link lama (misal setelah ditolak 403) supaya diambil ulang. */
    fun invalidate(videoId: String) { cache.remove(videoId) }

    private fun infoBlocking(videoId: String): StreamInfo {
        cache[videoId]?.let { if (System.currentTimeMillis() - it.at < CACHE_MS) return it.info }
        val info = StreamInfo.getInfo(yt, "https://www.youtube.com/watch?v=$videoId")
        remember(info)
        return info
    }

    private fun remember(info: StreamInfo) {
        cache[info.id] = Cached(info, System.currentTimeMillis())
    }

    /** Blocking — dipanggil dari thread loader ExoPlayer / IO. */
    fun resolveAudioBlocking(videoId: String): ResolvedAudio {
        val best = pickAudio(infoBlocking(videoId).audioStreams)
            ?: error("Tidak ada stream audio untuk video ini")
        return ResolvedAudio(best.content, best.format, best.itagItem?.contentLength ?: -1L)
    }

    /** Stream video TANPA suara (video-only), maks [maxHeight]p, utamakan H.264/MP4 agar ringan. */
    fun resolveVideoBlocking(videoId: String, maxHeight: Int = Prefs.videoMax.value): ResolvedVideo {
        val streams = infoBlocking(videoId).videoOnlyStreams.filter {
            it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.isUrl
        }
        fun score(v: VideoStream): Int {
            val h = v.height
            var s = if (h <= maxHeight) h * 10 else -h          // resolusi tertinggi yang ≤ batas
            if (v.format == MediaFormat.MPEG_4) s += 3
            if (v.codec?.startsWith("avc") == true) s += 5      // H.264: decode hardware hampir di semua HP
            if (v.fps > 30) s -= 2                               // 30fps lebih hemat & mulus di HP biasa
            return s
        }
        val best = streams.maxByOrNull(::score) ?: error("Tidak ada stream video untuk video ini")
        return ResolvedVideo(best.content, best.format, best.itagItem?.contentLength ?: -1L, best.height)
    }

    private fun pickAudio(streams: List<AudioStream>): AudioStream? {
        val progressive = streams.filter {
            it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.isUrl
        }
        // Utamakan trek audio asli (bukan sulih suara otomatis).
        val original = progressive.filter {
            it.audioTrackType == null || it.audioTrackType == AudioTrackType.ORIGINAL
        }.ifEmpty { progressive }
        fun br(s: AudioStream) = if (s.averageBitrate > 0) s.averageBitrate else s.bitrate / 1000
        return when (Prefs.audio.value) {
            AudioQuality.BEST -> original.maxWithOrNull(
                compareBy<AudioStream> { br(it) }.thenBy { if (it.format == MediaFormat.M4A) 1 else 0 }
            )
            AudioQuality.STANDARD -> original.filter { it.format == MediaFormat.M4A }.maxByOrNull { br(it) }
                ?: original.maxByOrNull { br(it) }
            AudioQuality.SAVER -> original.filter { br(it) > 0 }.minByOrNull { br(it) }
                ?: original.firstOrNull()
        }
    }

    private fun bestImage(images: List<Image>): String? =
        images.maxByOrNull { it.width * it.height }?.url ?: images.firstOrNull()?.url

    fun extractUrl(text: String): String? =
        Regex("""https?://(www\.|m\.|music\.)?(youtube\.com|youtu\.be)/\S+""").find(text)?.value
}
