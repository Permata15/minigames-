package com.permata.neonamp.data

import android.media.MediaCodecList
import org.schabi.newpipe.extractor.MediaFormat

/** Cek codec video yang benar-benar bisa di-decode HP ini (AV1/VP9 tidak ada di semua HP). */
object Codecs {
    private val cache = HashMap<String, Boolean>()
    private val infos by lazy { runCatching { MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.toList() }.getOrDefault(emptyList()) }

    fun mimeOf(codec: String?, format: MediaFormat?): String? {
        val c = codec?.lowercase()
        return when {
            c == null -> when (format) {
                MediaFormat.MPEG_4, MediaFormat.v3GPP -> "video/avc"
                MediaFormat.WEBM -> "video/x-vnd.on2.vp9"
                else -> null
            }
            c.startsWith("avc") -> "video/avc"
            c.startsWith("vp9") || c.startsWith("vp09") -> "video/x-vnd.on2.vp9"
            c.startsWith("av01") -> "video/av01"
            c.startsWith("hev") || c.startsWith("hvc") -> "video/hevc"
            c.startsWith("mp4v") -> "video/mp4v-es"
            c.startsWith("vp8") -> "video/x-vnd.on2.vp8"
            else -> null
        }
    }

    fun supports(mime: String, width: Int, height: Int): Boolean = synchronized(cache) {
        cache.getOrPut("$mime:$width:$height") {
            if (infos.isEmpty()) return@getOrPut mime == "video/avc"
            infos.any { info ->
                !info.isEncoder && info.supportedTypes.any { it.equals(mime, ignoreCase = true) } &&
                    runCatching {
                        val vc = info.getCapabilitiesForType(mime).videoCapabilities
                        vc == null || width <= 0 || height <= 0 || vc.isSizeSupported(width, height)
                    }.getOrDefault(false)
            }
        }
    }

    /** Nama codec yang ramah dibaca. */
    fun label(codec: String?, format: MediaFormat?): String = when (mimeOf(codec, format)) {
        "video/avc" -> "H.264"
        "video/x-vnd.on2.vp9" -> "VP9"
        "video/av01" -> "AV1"
        "video/hevc" -> "HEVC"
        else -> format?.getName() ?: "?"
    }
}
