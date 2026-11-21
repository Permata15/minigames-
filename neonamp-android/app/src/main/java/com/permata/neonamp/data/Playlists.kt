package com.permata.neonamp.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Grup buatan sendiri (mis. "Galau", "Olahraga", "Kerja") berisi lagu audio offline. */
data class Playlist(val id: String, val name: String, val trackIds: List<String>)

class PlaylistStore(context: Context) {
    private val file = File(context.filesDir, "playlists.json")
    private val _lists = MutableStateFlow(load())
    val playlists: StateFlow<List<Playlist>> = _lists.asStateFlow()

    private fun load(): List<Playlist> = runCatching {
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val ids = o.getJSONArray("tracks")
            Playlist(o.getString("id"), o.getString("name"), (0 until ids.length()).map { ids.getString(it) })
        }
    }.getOrDefault(emptyList())

    private fun save(list: List<Playlist>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().put("id", p.id).put("name", p.name).put("tracks", JSONArray(p.trackIds)))
        }
        file.writeText(arr.toString())
    }

    private fun change(f: (List<Playlist>) -> List<Playlist>) = _lists.update { f(it).also(::save) }

    fun create(name: String, firstTrack: String? = null): Playlist {
        val p = Playlist(UUID.randomUUID().toString(), name.trim().ifBlank { "Grup baru" }, listOfNotNull(firstTrack))
        change { it + p }
        return p
    }

    fun rename(id: String, name: String) = change { l -> l.map { if (it.id == id) it.copy(name = name.trim().ifBlank { it.name }) else it } }

    fun delete(id: String) = change { l -> l.filterNot { it.id == id } }

    fun toggle(id: String, trackId: String) = change { l ->
        l.map {
            if (it.id != id) it
            else if (trackId in it.trackIds) it.copy(trackIds = it.trackIds - trackId)
            else it.copy(trackIds = it.trackIds + trackId)
        }
    }

    fun removeTrackEverywhere(trackId: String) = change { l -> l.map { it.copy(trackIds = it.trackIds - trackId) } }
}
