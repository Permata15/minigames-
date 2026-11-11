package com.permata.neonamp.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Membaca lagu yang sudah ada di HP (MediaStore). */
object LocalMusic {
    suspend fun scan(context: Context): List<Track> = withContext(Dispatchers.IO) {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
        )
        val result = mutableListOf<Track>()
        context.contentResolver.query(
            uri, projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0", null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val content = ContentUris.withAppendedId(uri, id)
                val art = ContentUris.withAppendedId(
                    android.net.Uri.parse("content://media/external/audio/albumart"), c.getLong(albumCol)
                )
                result += Track(
                    id = "local:$id",
                    title = c.getString(titleCol) ?: "Tanpa judul",
                    artist = c.getString(artistCol)?.takeIf { it != "<unknown>" } ?: "",
                    durationMs = c.getLong(durCol),
                    artworkUrl = art.toString(),
                    source = Source.LOCAL,
                    localPath = content.toString(),
                )
            }
        }
        result
    }
}
