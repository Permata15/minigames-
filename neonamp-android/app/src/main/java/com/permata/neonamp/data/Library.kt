package com.permata.neonamp.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.io.File

/**
 * Daftar lagu/video yang sudah diunduh (offline), disimpan sebagai JSON.
 * Dipakai dua kali: koleksi Audio ("music") dan koleksi Video ("video").
 */
class Library(context: Context, name: String) {
    val dir = File(context.filesDir, name).apply { mkdirs() }
    val lyricsDir = File(context.filesDir, "lyrics").apply { mkdirs() }
    private val file = File(context.filesDir, if (name == "music") "library.json" else "$name.json")

    private val _tracks = MutableStateFlow(load())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private fun exists(path: String?) = path == null || File(path).exists()

    private fun load(): List<Track> = runCatching {
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { Track.fromJson(arr.getJSONObject(it)) }
            .filter { it.localPath != null && exists(it.localPath) && exists(it.videoPath) }
    }.getOrDefault(emptyList())

    private fun save(list: List<Track>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        file.writeText(arr.toString())
    }

    fun get(id: String): Track? = _tracks.value.firstOrNull { it.id == id }

    fun add(track: Track) {
        _tracks.update { list -> (listOf(track) + list.filterNot { it.id == track.id }).also(::save) }
    }

    fun remove(track: Track) {
        val stored = get(track.id) ?: track
        listOfNotNull(stored.localPath, stored.videoPath).forEach { File(it).delete() }
        stored.artworkUrl?.takeIf { it.startsWith("file:") }?.let { File(android.net.Uri.parse(it).path ?: "").delete() }
        _tracks.update { list -> list.filterNot { it.id == track.id }.also(::save) }
    }
}
