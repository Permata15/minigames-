package com.permata.neonamp.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.permata.neonamp.MainActivity
import com.permata.neonamp.NeonAmpApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Menjaga unduhan tetap berjalan saat aplikasi ditutup, dengan notifikasi progres. */
class DownloadService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Unduhan", NotificationManager.IMPORTANCE_LOW)
        )
        startInForeground(build("Menyiapkan unduhan…", 0, true))

        val dm = (application as NeonAmpApp).downloads
        scope.launch {
            combine(dm.progress, dm.titles) { p, t -> p to t }.collect { (progress, titles) ->
                val active = progress.filterValues { it >= 0f }
                if (active.isEmpty()) {
                    ServiceCompat.stopForeground(this@DownloadService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }
                val avg = active.values.average().toFloat()
                val title = if (active.size == 1) titles[active.keys.first()] ?: "Mengunduh"
                else "Mengunduh ${active.size} item"
                val n = build(title, (avg * 100).toInt(), avg <= 0f)
                runCatching { NotificationManagerCompat.from(this@DownloadService).notify(ID, n) }
            }
        }
    }

    private fun startInForeground(n: android.app.Notification) {
        val type = if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, ID, n, type)
    }

    private fun build(title: String, percent: Int, indeterminate: Boolean) =
        NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(com.permata.neonamp.R.drawable.ic_stat_robot)
            .setContentTitle(title)
            .setContentText(if (indeterminate) "Menghubungkan ke YouTube…" else "$percent%")
            .setProgress(100, percent, indeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 1, Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Wajib dipanggil untuk setiap startForegroundService().
        startInForeground(build("Mengunduh…", 0, true))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "downloads"
        private const val ID = 42
    }
}
