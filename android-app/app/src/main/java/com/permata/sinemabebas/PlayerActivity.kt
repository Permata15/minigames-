package com.permata.sinemabebas

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.permata.sinemabebas.data.Languages
import com.permata.sinemabebas.data.PlayItem
import com.permata.sinemabebas.data.Prefs
import com.permata.sinemabebas.data.Subtitle

/**
 * Full-screen player built on Media3 ExoPlayer.
 * - CC button: choose subtitle (Indonesia / English / Deutsch / off)
 * - ⚙ button: choose audio track (dub language) and playback speed
 * - "+ Subtitle": load an .srt / .vtt / .ass file from the phone
 * - Remembers where you stopped, and plays series episodes as a playlist.
 */
@OptIn(UnstableApi::class)
class PlayerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ITEMS = "items"
        const val EXTRA_INDEX = "index"
    }

    private lateinit var prefs: Prefs
    private lateinit var playerView: PlayerView
    private lateinit var titleView: TextView
    private lateinit var overlay: FrameLayout
    private var player: ExoPlayer? = null

    private lateinit var items: MutableList<PlayItem>
    private var index = 0
    private var position = C.TIME_UNSET

    private val pickSubtitle = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) askLanguageAndAdd(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        items = PlayItem.listFromJson(intent.getStringExtra(EXTRA_ITEMS) ?: "[]").toMutableList()
        index = savedInstanceState?.getInt("index") ?: intent.getIntExtra(EXTRA_INDEX, 0)
        position = savedInstanceState?.getLong("pos") ?: C.TIME_UNSET
        if (items.isEmpty()) { finish(); return }

        buildViews()
        hideSystemBars()
    }

    private fun buildViews() {
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        playerView = PlayerView(this).apply {
            keepScreenOn = true
            setShowSubtitleButton(true)
            setShowNextButton(items.size > 1)
            setShowPreviousButton(items.size > 1)
            setShowShuffleButton(false)
        }
        root.addView(playerView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // Top bar overlay that appears together with the player controls.
        overlay = FrameLayout(this).apply {
            setPadding(32, 24, 32, 24)
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xAA000000.toInt(), 0x00000000),
            )
        }
        titleView = TextView(this).apply {
            setTextColor(Color.WHITE); textSize = 16f; maxLines = 1
            setShadowLayer(4f, 0f, 0f, Color.BLACK)
        }
        overlay.addView(titleView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START or Gravity.CENTER_VERTICAL).apply { marginEnd = 360 })
        val addSub = Button(this).apply {
            text = "+ Subtitle"; isAllCaps = false
            setTextColor(Color.WHITE); textSize = 13f
            minHeight = 0; minimumHeight = 0; setPadding(36, 14, 36, 14)
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 60f; setColor(0x66000000); setStroke(2, 0x99FFFFFF.toInt())
            }
            setOnClickListener {
                pickSubtitle.launch(arrayOf("application/x-subrip", "text/vtt", "text/*", "application/octet-stream", "*/*"))
            }
        }
        overlay.addView(addSub, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END or Gravity.CENTER_VERTICAL))
        root.addView(overlay, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP))

        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { v -> overlay.visibility = v })
        setContentView(root)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun mediaItem(p: PlayItem): MediaItem {
        val subs = p.subtitles.map { s ->
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(s.url))
                // Files picked from the phone have no extension in their content:// URI; the label holds the name.
                .setMimeType(subtitleMime(if (s.url.startsWith("content:")) s.label else s.url))
                .setLanguage(s.lang)
                .setLabel(s.label)
                .build()
        }
        val path = p.url.substringBefore('?').lowercase()
        return MediaItem.Builder()
            .setUri(p.url)
            .setMediaId(p.url)
            .apply {
                when {
                    path.endsWith(".m3u8") || path.endsWith(".m3u") -> setMimeType(MimeTypes.APPLICATION_M3U8)
                    path.endsWith(".mpd") -> setMimeType(MimeTypes.APPLICATION_MPD)
                }
            }
            .setSubtitleConfigurations(subs)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(p.title).build())
            .build()
    }

    private fun subtitleMime(name: String): String {
        val n = name.substringBefore('?').lowercase().trimEnd(')', ' ')
        return when {
            n.endsWith(".vtt") -> MimeTypes.TEXT_VTT
            n.endsWith(".ass") || n.endsWith(".ssa") -> MimeTypes.TEXT_SSA
            n.endsWith(".ttml") || n.endsWith(".xml") -> MimeTypes.APPLICATION_TTML
            else -> MimeTypes.APPLICATION_SUBRIP
        }
    }

    private fun initPlayer() {
        val trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(
            trackSelector.buildUponParameters().apply {
                val sub = prefs.subtitleLang
                // "off": no preferred language, so subtitles stay hidden until picked via the CC button.
                setPreferredTextLanguage(if (sub == "off") null else sub)
                val audio = prefs.audioLang
                if (audio != "auto") setPreferredAudioLanguage(audio)
                if (prefs.dataSaver) setMaxVideoSize(854, 480)
            },
        )

        val http = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("SinemaBebas/1.0 (Android)")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)
        val dataSource = DefaultDataSource.Factory(this, http)

        // Modest buffers: start fast, smooth playback, low memory use.
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(15_000, 50_000, 1_500, 3_000)
            .build()

        val exo = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()

        exo.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                titleView.text = mediaItem?.mediaMetadata?.title ?: ""
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@PlayerActivity, "Gagal memutar: ${error.errorCodeName}", Toast.LENGTH_LONG).show()
            }
        })

        exo.setMediaItems(items.map(::mediaItem), index, startPosition())
        exo.playWhenReady = true
        exo.prepare()
        playerView.player = exo
        titleView.text = items[index].title
        player = exo
    }

    private fun startPosition(): Long =
        if (position != C.TIME_UNSET) position else prefs.position(items[index].url).takeIf { it > 0 } ?: C.TIME_UNSET

    private fun saveProgress() {
        val p = player ?: return
        index = p.currentMediaItemIndex
        position = p.currentPosition
        prefs.savePosition(items[index].url, p.currentPosition, p.duration)
    }

    private fun releasePlayer() {
        saveProgress()
        player?.release()
        player = null
    }

    override fun onStart() { super.onStart(); if (player == null) initPlayer() }
    override fun onStop() { super.onStop(); releasePlayer() }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let { index = it.currentMediaItemIndex; position = it.currentPosition }
        outState.putInt("index", index)
        outState.putLong("pos", position)
    }

    // ---- Load a subtitle file from the device ----

    private fun askLanguageAndAdd(uri: Uri) {
        runCatching { contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        val fileName = queryName(uri)
        val labels = Languages.options.map { it.second }.toTypedArray()
        val guessed = Languages.options.indexOfFirst { it.first == Languages.guess(fileName) }.coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle("Bahasa subtitle")
            .setSingleChoiceItems(labels, guessed) { d, which ->
                d.dismiss()
                val (code, label) = Languages.options[which]
                addSubtitle(Subtitle(uri.toString(), code, "$label (${fileName.ifEmpty { "file" }})"), fileName)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun addSubtitle(sub: Subtitle, fileName: String) {
        val p = player ?: return
        index = p.currentMediaItemIndex
        position = p.currentPosition
        val current = items[index]
        items[index] = current.copy(subtitles = current.subtitles + sub)
        val cfg = MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
            .setMimeType(subtitleMime(fileName.ifEmpty { sub.url }))
            .setLanguage(sub.lang)
            .setLabel(sub.label)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()
        val updated = p.getMediaItemAt(index).buildUpon()
            .setSubtitleConfigurations(p.getMediaItemAt(index).localConfiguration?.subtitleConfigurations.orEmpty() + cfg)
            .build()
        p.trackSelectionParameters = p.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .setPreferredTextLanguage(sub.lang)
            .build()
        p.replaceMediaItem(index, updated)
        p.seekTo(index, position)
        p.prepare()
        Toast.makeText(this, "Subtitle ${Languages.label(sub.lang)} dimuat", Toast.LENGTH_SHORT).show()
    }

    private fun queryName(uri: Uri): String = runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else ""
        } ?: ""
    }.getOrDefault("")
}
