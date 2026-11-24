package com.permata.sinemabebas.data

import org.json.JSONArray
import org.json.JSONObject

/** A title in the catalog (film, series or drama). */
data class Title(
    val id: String,
    val name: String,
    val year: String,
    val posterUrl: String,
    val description: String = "",
)

/** A side-loaded subtitle: [lang] is an ISO 639-1 code such as "id", "en" or "de". */
data class Subtitle(val url: String, val lang: String, val label: String)

/** One playable video (a film, or one episode of a series). */
data class PlayItem(
    val title: String,
    val url: String,
    val subtitles: List<Subtitle> = emptyList(),
) {
    fun toJson(): JSONObject = JSONObject()
        .put("title", title)
        .put("url", url)
        .put("subs", JSONArray().apply {
            subtitles.forEach {
                put(JSONObject().put("url", it.url).put("lang", it.lang).put("label", it.label))
            }
        })

    companion object {
        fun fromJson(o: JSONObject): PlayItem {
            val subs = o.optJSONArray("subs") ?: JSONArray()
            return PlayItem(
                title = o.optString("title"),
                url = o.optString("url"),
                subtitles = (0 until subs.length()).map {
                    val s = subs.getJSONObject(it)
                    Subtitle(s.optString("url"), s.optString("lang"), s.optString("label"))
                },
            )
        }

        fun listToJson(items: List<PlayItem>): String =
            JSONArray().apply { items.forEach { put(it.toJson()) } }.toString()

        fun listFromJson(json: String): List<PlayItem> {
            val arr = JSONArray(json)
            return (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        }
    }
}

/** Full details for a catalog title, including its playable files. */
data class TitleDetail(
    val title: Title,
    val language: String,
    val runtime: String,
    val items: List<PlayItem>,
)

object Languages {
    /** Languages offered for subtitles and audio. */
    val options = listOf("id" to "Bahasa Indonesia", "en" to "English", "de" to "Deutsch")

    fun label(code: String): String = options.firstOrNull { it.first == code }?.second ?: code

    /** Guess a subtitle language from a file name such as "movie.ind.srt" or "Movie_English.vtt". */
    fun guess(fileName: String): String {
        val n = fileName.lowercase()
        val tokens = n.split('.', '_', '-', ' ', '(', ')', '[', ']')
        return when {
            tokens.any { it in setOf("id", "ind", "indo", "indonesia", "indonesian", "bahasa") } -> "id"
            tokens.any { it in setOf("de", "ger", "deu", "german", "deutsch") } -> "de"
            tokens.any { it in setOf("en", "eng", "english") } -> "en"
            else -> "en"
        }
    }
}
