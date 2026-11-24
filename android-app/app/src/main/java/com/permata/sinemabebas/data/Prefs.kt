package com.permata.sinemabebas.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Settings, the user's own sources and "continue watching" positions. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("sinema", Context.MODE_PRIVATE)

    /** Preferred subtitle language code, or "off". */
    var subtitleLang: String
        get() = sp.getString("subLang", "id") ?: "id"
        set(v) = sp.edit().putString("subLang", v).apply()

    /** Preferred audio language code, or "auto" for the original track. */
    var audioLang: String
        get() = sp.getString("audioLang", "auto") ?: "auto"
        set(v) = sp.edit().putString("audioLang", v).apply()

    /** Lower data use: limits video to ~480p. */
    var dataSaver: Boolean
        get() = sp.getBoolean("dataSaver", false)
        set(v) = sp.edit().putBoolean("dataSaver", v).apply()

    /** Show only titles released in or after this year. */
    var minYear: Int
        get() = sp.getInt("minYear", 1997)
        set(v) = sp.edit().putInt("minYear", v).apply()

    // ---- User-added sources ----
    fun sources(): List<PlayItem> =
        runCatching { PlayItem.listFromJson(sp.getString("sources", "[]") ?: "[]") }.getOrDefault(emptyList())

    fun saveSources(list: List<PlayItem>) = sp.edit().putString("sources", PlayItem.listToJson(list)).apply()

    // ---- Resume positions ----
    fun position(url: String): Long = sp.getLong("pos:$url", 0L)

    fun savePosition(url: String, posMs: Long, durationMs: Long) {
        val e = sp.edit()
        // Forget the position when (nearly) finished.
        if (durationMs > 0 && posMs > durationMs - 30_000) e.remove("pos:$url") else e.putLong("pos:$url", posMs)
        e.apply()
    }

    // ---- Continue-watching history (catalog titles) ----
    fun history(): List<Title> = runCatching {
        val arr = JSONArray(sp.getString("history", "[]"))
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Title(o.getString("id"), o.optString("name"), o.optString("year"), o.optString("poster"))
        }
    }.getOrDefault(emptyList())

    fun addHistory(t: Title) {
        val list = (listOf(t) + history().filter { it.id != t.id }).take(20)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("name", it.name).put("year", it.year).put("poster", it.posterUrl))
        }
        sp.edit().putString("history", arr.toString()).apply()
    }
}
