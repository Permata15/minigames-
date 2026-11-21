package com.permata.neonamp

import android.app.Application
import com.permata.neonamp.data.DownloadManager
import com.permata.neonamp.data.Library
import com.permata.neonamp.data.NetDownloader
import com.permata.neonamp.data.PlaylistStore
import com.permata.neonamp.data.Prefs
import com.permata.neonamp.ui.Neon
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class NeonAmpApp : Application() {
    lateinit var audioLib: Library
        private set
    lateinit var videoLib: Library
        private set
    lateinit var downloads: DownloadManager
        private set
    lateinit var playlists: PlaylistStore
        private set

    override fun onCreate() {
        super.onCreate()
        NewPipe.init(NetDownloader, Localization("id", "ID"), ContentCountry("ID"))
        Neon.load(this)
        Prefs.init(this)
        playlists = PlaylistStore(this)
        audioLib = Library(this, "music")
        videoLib = Library(this, "video")
        downloads = DownloadManager(this, audioLib, videoLib)
    }
}
