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

enum class CaptionMode(val label: String) { OFF("Mati"), LYRICS("Lirik"), SUBS("Subtitle") }
enum class StreamStability(val label: String, val detail: String) {
    NORMAL("Normal", "Mulai cepat, hemat memori"),
    STABLE("Stabil", "Buffer besar (±2 menit), anti putus-putus di sinyal lemah"),
}

/** Pengaturan kualitas, stabilizer, dan subtitle. */
object Prefs {
    private lateinit var sp: SharedPreferences

    private val _audio = MutableStateFlow(AudioQuality.BEST)
    val audio: StateFlow<AudioQuality> = _audio.asStateFlow()

    /** Resolusi maksimum video (tinggi piksel). */
    private val _videoMax = MutableStateFlow(1080)
    val videoMax: StateFlow<Int> = _videoMax.asStateFlow()

    val videoOptions = listOf(1080, 720, 480, 360)

    private val _caption = MutableStateFlow(CaptionMode.SUBS)
    val caption: StateFlow<CaptionMode> = _caption.asStateFlow()

    /** Penstabil volume: meratakan keras-pelan antar lagu/video. */
    private val _volumeStabilizer = MutableStateFlow(false)
    val volumeStabilizer: StateFlow<Boolean> = _volumeStabilizer.asStateFlow()

    private val _stability = MutableStateFlow(StreamStability.STABLE)
    val stability: StateFlow<StreamStability> = _stability.asStateFlow()

    fun init(context: Context) {
        sp = context.getSharedPreferences("neonamp_prefs", Context.MODE_PRIVATE)
        _audio.value = runCatching { AudioQuality.valueOf(sp.getString("audio_q", null) ?: "BEST") }
            .getOrDefault(AudioQuality.BEST)
        _videoMax.value = sp.getInt("video_max", 1080)
        _caption.value = runCatching { CaptionMode.valueOf(sp.getString("caption", null) ?: "SUBS") }.getOrDefault(CaptionMode.SUBS)
        _volumeStabilizer.value = sp.getBoolean("vol_stab", false)
        _stability.value = runCatching { StreamStability.valueOf(sp.getString("stability", null) ?: "STABLE") }.getOrDefault(StreamStability.STABLE)
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

    fun setCaption(m: CaptionMode) { _caption.value = m; sp.edit().putString("caption", m.name).apply() }
    fun setVolumeStabilizer(on: Boolean) { _volumeStabilizer.value = on; sp.edit().putBoolean("vol_stab", on).apply() }
    fun setStability(s: StreamStability) { _stability.value = s; sp.edit().putString("stability", s.name).apply() }
}
