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
    data class ResolvedVideo(
        val url: String, val format: MediaFormat?, val contentLength: Long, val height: Int,
        /** true = stream gabungan yang sudah berisi suara (cadangan bila video-only tidak ada). */
        val muxed: Boolean = false,
    )

    /** Pilihan unduhan video: resolusi + perkiraan ukuran total (video + audio). */
    data class VideoOption(val itag: Int, val height: Int, val fps: Int, val codec: String, val bytes: Long, val muxed: Boolean)
    data class AudioOption(val itag: Int, val label: String, val kbps: Int, val bytes: Long)
    data class DownloadOptions(val video: List<VideoOption>, val audio: List<AudioOption>, val defaultAudioItag: Int?)
    data class Subtitle(val url: String, val language: String, val auto: Boolean)

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

    /** Blocking — dipanggil dari thread loader ExoPlayer / IO. [itag] = pilihan khusus (unduhan). */
    fun resolveAudioBlocking(videoId: String, itag: Int? = null): ResolvedAudio {
        val streams = infoBlocking(videoId).audioStreams
        val best = itag?.let { i -> streams.firstOrNull { it.itag == i && it.isUrl } } ?: pickAudio(streams)
            ?: error("Tidak ada stream audio untuk video ini")
        return ResolvedAudio(best.content, best.format, best.itagItem?.contentLength ?: -1L)
    }

    private fun estimate(len: Long?, bitrate: Int, durationSec: Long): Long = when {
        len != null && len > 0 -> len
        bitrate > 0 && durationSec > 0 -> bitrate.toLong() / 8 * durationSec
        else -> -1L
    }

    /** Stream video yang bisa diputar HP ini: video-only dulu, lalu stream gabungan sebagai cadangan. */
    private fun playableVideo(info: StreamInfo): Pair<List<VideoStream>, List<VideoStream>> {
        fun ok(v: VideoStream): Boolean {
            if (v.deliveryMethod != DeliveryMethod.PROGRESSIVE_HTTP || !v.isUrl) return false
            val mime = Codecs.mimeOf(v.codec, v.format) ?: return false
            return Codecs.supports(mime, v.width, v.height)
        }
        return info.videoOnlyStreams.filter(::ok) to info.videoStreams.filter(::ok)
    }

    private fun codecRank(v: VideoStream) = when (Codecs.mimeOf(v.codec, v.format)) {
        "video/avc" -> 3      // decode hardware di hampir semua HP, hemat baterai
        "video/x-vnd.on2.vp9" -> 2
        "video/hevc" -> 1
        else -> 0
    }

    private fun bestVideo(streams: List<VideoStream>, maxHeight: Int): VideoStream? {
        val fit = streams.filter { it.height <= maxHeight }
        val cmp = compareBy<VideoStream> { it.height }
            .thenBy { codecRank(it) }
            .thenBy { if (it.fps <= 30) 1 else 0 }
        return fit.maxWithOrNull(cmp) ?: streams.minByOrNull { it.height }
    }

    /**
     * Video untuk diputar/diunduh, maks [maxHeight]p. Utamakan video-only (digabung dengan audio
     * terbaik), pilih codec yang didukung HP; bila tidak ada, pakai stream gabungan.
     */
    fun resolveVideoBlocking(videoId: String, maxHeight: Int = Prefs.videoMax.value, itag: Int? = null): ResolvedVideo {
        val info = infoBlocking(videoId)
        val (only, muxed) = playableVideo(info)
        val chosen: VideoStream = itag?.let { i -> (info.videoOnlyStreams + info.videoStreams).firstOrNull { it.itag == i && it.isUrl } }
            ?: bestVideo(only, maxHeight)
            ?: bestVideo(muxed, maxHeight)
            ?: error("Video ini tidak punya format yang bisa diputar di HP ini")
        return ResolvedVideo(
            chosen.content, chosen.format, chosen.itagItem?.contentLength ?: -1L, chosen.height,
            muxed = !chosen.isVideoOnly,
        )
    }

    /** Daftar ukuran unduhan untuk dialog "Unduh". */
    suspend fun downloadOptions(videoId: String): DownloadOptions = withContext(Dispatchers.IO) {
        val info = infoBlocking(videoId)
        val dur = info.duration
        val audios = info.audioStreams.filter {
            it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.isUrl &&
                (it.audioTrackType == null || it.audioTrackType == AudioTrackType.ORIGINAL)
        }.ifEmpty { info.audioStreams.filter { it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.isUrl } }
        val defaultAudio = pickAudio(info.audioStreams)
        val audioOpts = audios
            .map {
                val kbps = if (it.averageBitrate > 0) it.averageBitrate else it.bitrate / 1000
                val name = if (it.format == MediaFormat.M4A) "AAC" else "Opus"
                AudioOption(it.itag, "$name $kbps kbps", kbps, estimate(it.itagItem?.contentLength, kbps * 1000, dur))
            }
            .distinctBy { it.label }
            .sortedByDescending { it.kbps }
        val audioBytes = defaultAudio?.let { a -> audioOpts.firstOrNull { it.itag == a.itag }?.bytes } ?: -1L
        val (only, muxed) = playableVideo(info)
        val perHeight = (if (only.isNotEmpty()) only else muxed)
            .groupBy { it.height }
            .mapNotNull { (_, list) -> list.maxWithOrNull(compareBy<VideoStream> { codecRank(it) }.thenBy { if (it.fps <= 30) 1 else 0 }) }
            .sortedByDescending { it.height }
        val videoOpts = perHeight.map { v ->
            val vb = estimate(v.itagItem?.contentLength, v.bitrate, dur)
            val total = if (!v.isVideoOnly || vb < 0 || audioBytes < 0) vb else vb + audioBytes
            VideoOption(v.itag, v.height, v.fps, Codecs.label(v.codec, v.format), total, !v.isVideoOnly)
        }
        DownloadOptions(videoOpts, audioOpts, defaultAudio?.itag)
    }

    fun asVtt(url: String): String =
        if (Regex("[?&]fmt=").containsMatchIn(url)) url.replace(Regex("([?&]fmt=)[^&]*"), "$1vtt") else "$url&fmt=vtt"

    /** Subtitle terbaik: buatan kreator dulu, bahasa Indonesia → Inggris → lainnya. */
    fun subtitleBlocking(videoId: String): Subtitle? {
        // YouTube memberi daftar TTML; URL timedtext yang sama bisa diminta sebagai WebVTT (fmt=vtt).
        val subs = runCatching { infoBlocking(videoId).subtitles }.getOrDefault(emptyList())
            .filter { it.isUrl }
        if (subs.isEmpty()) return null
        fun langRank(tag: String) = when {
            tag.startsWith("id") || tag.startsWith("in") -> 3
            tag.startsWith("en") -> 2
            else -> 1
        }
        val best = subs.maxWithOrNull(
            compareBy<org.schabi.newpipe.extractor.stream.SubtitlesStream> { if (it.isAutoGenerated) 0 else 1 }
                .thenBy { langRank(it.languageTag.lowercase()) }
        ) ?: return null
        return Subtitle(asVtt(best.content), best.displayLanguageName ?: best.languageTag, best.isAutoGenerated)
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
