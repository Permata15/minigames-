package com.permata.neonamp.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.io.File

/** Daftar lagu yang sudah diunduh (offline), disimpan sebagai JSON. */
class Library(context: Context) {
    val musicDir = File(context.filesDir, "music").apply { mkdirs() }
    val lyricsDir = File(context.filesDir, "lyrics").apply { mkdirs() }
    private val file = File(context.filesDir, "library.json")

    private val _tracks = MutableStateFlow(load())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private fun load(): List<Track> = runCatching {
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { Track.fromJson(arr.getJSONObject(it)) }
            .filter { it.localPath != null && File(it.localPath).exists() }
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
        track.localPath?.let { File(it).delete() }
        _tracks.update { list -> list.filterNot { it.id == track.id }.also(::save) }
    }

    /** Ganti lagu YouTube dengan versi offline-nya bila sudah diunduh. */
    fun preferOffline(track: Track): Track = get(track.id) ?: track
}
