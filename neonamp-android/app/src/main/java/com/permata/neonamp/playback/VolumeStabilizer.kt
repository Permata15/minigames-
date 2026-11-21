package com.permata.neonamp.playback

import android.media.audiofx.AudioEffect
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.LoudnessEnhancer
import android.os.Build

/**
 * Penstabil volume: kompresor + limiter dinamis pada output pemutar, sehingga lagu/video
 * yang pelan dinaikkan dan yang keras ditahan (tidak kaget saat ganti lagu).
 */
class VolumeStabilizer {
    private var effect: AudioEffect? = null
    private var session = 0
    private var enabled = false

    fun attach(audioSessionId: Int) {
        if (audioSessionId == session && effect != null) return
        release()
        session = audioSessionId
        if (audioSessionId == 0) return
        effect = runCatching { create(audioSessionId) }.getOrNull()
        runCatching { effect?.enabled = enabled }
    }

    fun setEnabled(on: Boolean) {
        enabled = on
        runCatching { effect?.enabled = on }
    }

    private fun create(sessionId: Int): AudioEffect =
        if (Build.VERSION.SDK_INT >= 28) {
            val cfg = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION, 2,
                false, 0, true, 1, false, 0, true,
            ).build()
            DynamicsProcessing(0, sessionId, cfg).apply {
                // kompresor 1 band: rata-ratakan keras-pelan, lalu naikkan level keseluruhan
                setMbcBandAllChannelsTo(
                    0,
                    DynamicsProcessing.MbcBand(true, 20000f, 5f, 120f, 3.5f, -28f, 8f, -90f, 1f, 0f, 9f),
                )
                // limiter: cegah pecah/terlalu keras
                setLimiterAllChannelsTo(DynamicsProcessing.Limiter(true, true, 0, 1f, 80f, 10f, -2f, 0f))
            }
        } else {
            LoudnessEnhancer(sessionId).apply { setTargetGain(500) }
        }

    fun release() {
        runCatching { effect?.release() }
        effect = null
        session = 0
    }
}
