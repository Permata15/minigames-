package com.permata.neonamp.data

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.json.JSONObject

enum class Source { YOUTUBE, LOCAL }

/**
 * Satu lagu. Untuk YouTube, [id] = "yt:<videoId>".
 * Jika sudah diunduh, [localPath] berisi file audio di penyimpanan aplikasi.
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artworkUrl: String?,
    val source: Source,
    val pageUrl: String? = null,
    /** File audio (offline) atau content:// (musik HP). */
    val localPath: String? = null,
    /** File video tanpa suara (offline). Audio-nya tetap di [localPath]. */
    val videoPath: String? = null,
    /** File subtitle .vtt (video offline). */
    val subtitlePath: String? = null,
    /** Putar dengan gambar video. Video & audio di-stream/disimpan terpisah lalu digabung saat diputar. */
    val wantVideo: Boolean = false,
) {
    val videoId: String? get() = if (source == Source.YOUTUBE) id.removePrefix("yt:") else null
    val isOffline: Boolean get() = localPath != null

    /** URI yang diputar: file lokal, content:// (musik HP), atau yt://<id> (di-resolve saat streaming). */
    fun playUri(): Uri = when {
        localPath != null && localPath.startsWith("content://") -> localPath.toUri()
        localPath != null -> Uri.fromFile(java.io.File(localPath))
        else -> "yt://$videoId".toUri()
    }

    /** URI video (tanpa suara) yang digabung dengan [playUri], atau null untuk mode audio saja. */
    fun videoUri(): Uri? = when {
        !wantVideo -> null
        videoPath != null -> Uri.fromFile(java.io.File(videoPath))
        videoId != null -> "ytv://$videoId".toUri()
        else -> null
    }

    fun toMediaItem(): MediaItem {
        val extras = Bundle().apply { putString(KEY_JSON, toJson().toString()) }
        val meta = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(artworkUrl?.toUri())
            .setExtras(extras)
            .build()
        val uri = playUri()
        return MediaItem.Builder()
            .setMediaId(if (wantVideo) "$id#video" else id)
            .setUri(uri)
            .setMediaMetadata(meta)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder().setMediaUri(uri).build()
            )
            .build()
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("artist", artist)
        put("durationMs", durationMs)
        put("artworkUrl", artworkUrl ?: "")
        put("source", source.name)
        put("pageUrl", pageUrl ?: "")
        put("localPath", localPath ?: "")
        put("videoPath", videoPath ?: "")
        put("wantVideo", wantVideo)
        put("subtitlePath", subtitlePath ?: "")
    }

    companion object {
        const val KEY_JSON = "neonamp_track"

        fun fromJson(o: JSONObject) = Track(
            id = o.getString("id"),
            title = o.optString("title"),
            artist = o.optString("artist"),
            durationMs = o.optLong("durationMs"),
            artworkUrl = o.optString("artworkUrl").ifBlank { null },
            source = Source.valueOf(o.optString("source", "YOUTUBE")),
            pageUrl = o.optString("pageUrl").ifBlank { null },
            localPath = o.optString("localPath").ifBlank { null },
            videoPath = o.optString("videoPath").ifBlank { null },
            wantVideo = o.optBoolean("wantVideo", false),
            subtitlePath = o.optString("subtitlePath").ifBlank { null },
        )

        fun fromMediaItem(item: MediaItem?): Track? {
            val json = item?.mediaMetadata?.extras?.getString(KEY_JSON) ?: return null
            return runCatching { fromJson(JSONObject(json)) }.getOrNull()
        }
    }
}
