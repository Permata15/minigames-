package com.permata.sinemabebas.data

import org.json.JSONObject
import java.net.URLEncoder

/**
 * Catalog backed by the Internet Archive (archive.org): public-domain and freely licensed
 * films, TV series and dramas that may legally be watched for free.
 */
object ArchiveRepository {

    data class Category(val key: String, val label: String, val query: String, val sort: String = "downloads desc")

    private const val CC = "licenseurl:(*creativecommons*)"

    // Collections checked against archive.org: each has hundreds or more titles from 1997 onwards.
    val categories = listOf(
        Category("baru", "Terbaru", "collection:(feature_films)", sort = "year desc"),
        Category("film", "Film Populer", "collection:(feature_films)"),
        Category("indie", "Drama & Indie", "collection:(moviesandfilms) AND $CC"),
        Category("pendek", "Film Pendek", "collection:(short_films)"),
        Category("animasi", "Animasi", "collection:(animationandcartoons) AND $CC"),
        Category("scifi", "Sci-Fi & Horor", "collection:(SciFi_Horror)"),
        Category("serial", "Serial & Web Series", "collection:(classic_tv) AND $CC"),
    )

    private const val SEARCH_SCOPE =
        "(collection:(feature_films OR short_films OR SciFi_Horror) OR " +
            "(collection:(moviesandfilms OR animationandcartoons OR classic_tv) AND $CC))"

    /** Only titles released in or after this year are shown (set from Settings; default 1997). */
    @Volatile var minYear: Int = 1997

    private fun yearFilter() = "year:[$minYear TO 2100]"

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    suspend fun browse(query: String, page: Int, sort: String = "downloads desc", rows: Int = 36): List<Title> {
        val q = "($query) AND mediatype:(movies) AND ${yearFilter()}"
        val url = "https://archive.org/advancedsearch.php?q=${enc(q)}" +
            "&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=year&fl%5B%5D=description" +
            "&sort%5B%5D=${enc(sort)}&rows=$rows&page=$page&output=json"
        val docs = JSONObject(Http.get(url)).getJSONObject("response").getJSONArray("docs")
        return (0 until docs.length()).map { i ->
            val d = docs.getJSONObject(i)
            val id = d.getString("identifier")
            Title(
                id = id,
                name = d.optString("title", id),
                year = d.optString("year", ""),
                posterUrl = "https://archive.org/services/img/$id",
                description = descriptionText(d.opt("description")),
            )
        }
    }

    suspend fun search(text: String, page: Int): List<Title> {
        val cleaned = text.replace(Regex("[\"():]"), " ").trim()
        if (cleaned.isEmpty()) return emptyList()
        return browse("title:($cleaned) AND $SEARCH_SCOPE", page)
    }

    suspend fun detail(id: String): TitleDetail {
        val root = JSONObject(Http.get("https://archive.org/metadata/${enc(id)}"))
        val meta = root.optJSONObject("metadata") ?: JSONObject()
        val files = root.optJSONArray("files")
        val names = mutableListOf<String>()
        if (files != null) for (i in 0 until files.length()) {
            names += files.getJSONObject(i).optString("name")
        }

        // Prefer the plain .mp4 derivative; drop ".ia.mp4" duplicates when a plain one exists.
        val mp4 = names.filter { it.endsWith(".mp4", ignoreCase = true) }
        val videos = mp4.filter { n ->
            !n.endsWith(".ia.mp4", true) || mp4.none { it.equals(n.removeSuffix(".ia.mp4") + ".mp4", true) }
        }.sortedWith(naturalOrder())

        val subFiles = names.filter {
            it.endsWith(".srt", true) || it.endsWith(".vtt", true) || it.endsWith(".ass", true)
        }
        val base = "https://archive.org/download/${enc(id)}/"
        fun fileUrl(n: String) = base + n.split('/').joinToString("/") { enc(it).replace("+", "%20") }

        val name = meta.optString("title", id)
        val items = videos.map { v ->
            val stem = v.substringBeforeLast('.').removeSuffix(".ia").substringBeforeLast("_512kb")
            // Subtitles that belong to this video, or all of them when there is a single video.
            val matching = subFiles.filter { videos.size == 1 || it.startsWith(stem) }
            PlayItem(
                title = if (videos.size == 1) name else prettyEpisode(v),
                url = fileUrl(v),
                subtitles = matching.map { s ->
                    val lang = Languages.guess(s)
                    Subtitle(fileUrl(s), lang, Languages.label(lang))
                },
            )
        }
        return TitleDetail(
            title = Title(
                id = id,
                name = name,
                year = meta.optString("year", meta.optString("date", "").take(4)),
                posterUrl = "https://archive.org/services/img/$id",
                description = descriptionText(meta.opt("description")),
            ),
            language = meta.optString("language", ""),
            runtime = meta.optString("runtime", ""),
            items = items,
        )
    }

    private fun prettyEpisode(file: String): String =
        file.substringAfterLast('/').substringBeforeLast('.').removeSuffix(".ia")
            .removeSuffix("_512kb").replace('_', ' ').trim()

    /** Natural sort so "Episode 2" comes before "Episode 10". */
    private fun naturalOrder() = Comparator<String> { a, b ->
        val ra = Regex("\\d+|\\D+").findAll(a.lowercase()).map { it.value }.toList()
        val rb = Regex("\\d+|\\D+").findAll(b.lowercase()).map { it.value }.toList()
        for (i in 0 until minOf(ra.size, rb.size)) {
            val x = ra[i]; val y = rb[i]
            val c = if (x[0].isDigit() && y[0].isDigit()) {
                (x.toBigInteger()).compareTo(y.toBigInteger())
            } else x.compareTo(y)
            if (c != 0) return@Comparator c
        }
        ra.size - rb.size
    }

    private fun descriptionText(raw: Any?): String {
        val s = when (raw) {
            is org.json.JSONArray -> (0 until raw.length()).joinToString("\n") { raw.optString(it) }
            null -> ""
            else -> raw.toString()
        }
        return s.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
            .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ")
            .trim()
    }
}
