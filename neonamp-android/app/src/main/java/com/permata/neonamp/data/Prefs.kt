package com.permata.neonamp.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioQuality(val label: String, val detail: String) {
    BEST("Terbaik", "Bitrate tertinggi (Opus ±160 kbps / AAC 128 kbps)"),
    STANDARD("Standar AAC", "AAC/M4A 128 kbps, paling kompatibel"),
    SAVER("Hemat kuota", "±50 kbps, cocok untuk data seluler"),
}

/** Pengaturan kualitas stream & unduhan. */
object Prefs {
    private lateinit var sp: SharedPreferences

    private val _audio = MutableStateFlow(AudioQuality.BEST)
    val audio: StateFlow<AudioQuality> = _audio.asStateFlow()

    /** Resolusi maksimum video (tinggi piksel). */
    private val _videoMax = MutableStateFlow(1080)
    val videoMax: StateFlow<Int> = _videoMax.asStateFlow()

    val videoOptions = listOf(1080, 720, 480, 360)

    fun init(context: Context) {
        sp = context.getSharedPreferences("neonamp_prefs", Context.MODE_PRIVATE)
        _audio.value = runCatching { AudioQuality.valueOf(sp.getString("audio_q", null) ?: "BEST") }
            .getOrDefault(AudioQuality.BEST)
        _videoMax.value = sp.getInt("video_max", 1080)
    }

    fun setAudio(q: AudioQuality) {
        _audio.value = q
        sp.edit().putString("audio_q", q.name).apply()
        YouTubeRepo.clearCache()
    }

    fun setVideoMax(h: Int) {
        _videoMax.value = h
        sp.edit().putInt("video_max", h).apply()
    }
}
