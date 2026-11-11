package com.permata.neonamp

import android.app.Application
import com.permata.neonamp.data.DownloadManager
import com.permata.neonamp.data.Library
import com.permata.neonamp.data.NetDownloader
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class NeonAmpApp : Application() {
    lateinit var library: Library
        private set
    lateinit var downloads: DownloadManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        NewPipe.init(NetDownloader, Localization("id", "ID"), ContentCountry("ID"))
        library = Library(this)
        downloads = DownloadManager(this, library)
    }

    companion object {
        lateinit var instance: NeonAmpApp
            private set
    }
}
