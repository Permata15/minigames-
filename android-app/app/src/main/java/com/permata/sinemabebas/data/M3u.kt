package com.permata.sinemabebas.data

/** Parses an M3U / M3U8 master playlist of channels or videos (#EXTINF entries). */
object M3u {
    fun parse(text: String): List<PlayItem> {
        val out = mutableListOf<PlayItem>()
        var pendingName: String? = null
        text.lineSequence().map { it.trim() }.forEach { line ->
            when {
                line.startsWith("#EXTINF", true) -> pendingName = line.substringAfterLast(',').trim()
                line.isEmpty() || line.startsWith("#") -> Unit
                line.startsWith("http://") || line.startsWith("https://") -> {
                    out += PlayItem(pendingName?.takeIf { it.isNotEmpty() } ?: line.substringAfterLast('/'), line)
                    pendingName = null
                }
            }
        }
        return out
    }

    /** True when the text is a list of entries rather than a single HLS stream. */
    fun isPlaylist(text: String): Boolean =
        text.contains("#EXTINF", true) && !text.contains("#EXT-X-TARGETDURATION", true) &&
            !text.contains("#EXT-X-STREAM-INF", true)
}
