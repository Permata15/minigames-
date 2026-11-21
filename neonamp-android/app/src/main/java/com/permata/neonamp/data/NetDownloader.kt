package com.permata.neonamp.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

/** Jembatan HTTP untuk NewPipeExtractor memakai OkHttp. */
object NetDownloader : Downloader() {
    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"

    /**
     * Header untuk server video YouTube (googlevideo) disamakan dengan klien yang meminta
     * link-nya (seperti aplikasi NewPipe). Link dari klien visionOS butuh User-Agent visionOS,
     * link klien web butuh Origin/Referer youtube.com — kalau tidak cocok server bisa menolak (403).
     */
    private val streamHeaders = Interceptor { chain ->
        val req = chain.request()
        val url = req.url.toString()
        if (!req.url.encodedPath.startsWith("/videoplayback")) return@Interceptor chain.proceed(req)
        val b = req.newBuilder()
        if (YoutubeParsingHelper.isVisionOsStreamingUrl(url)) {
            b.header("User-Agent", YoutubeParsingHelper.getVisionOsUserAgent(null))
        } else {
            b.header("User-Agent", USER_AGENT)
        }
        if (YoutubeParsingHelper.isWebStreamingUrl(url)) {
            b.header("Origin", "https://www.youtube.com")
            b.header("Referer", "https://www.youtube.com/")
        }
        chain.proceed(b.build())
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(streamHeaders)
        .build()

    override fun execute(request: Request): Response {
        val body = request.dataToSend()?.toRequestBody()
        val builder = okhttp3.Request.Builder()
            .method(request.httpMethod(), body)
            .url(request.url())
            .addHeader("User-Agent", USER_AGENT)

        request.headers().forEach { (name, values) ->
            builder.removeHeader(name)
            values.forEach { builder.addHeader(name, it) }
        }

        client.newCall(builder.build()).execute().use { resp ->
            if (resp.code == 429) {
                throw ReCaptchaException("reCaptcha Challenge requested", request.url())
            }
            val text = resp.body?.string()
            return Response(
                resp.code,
                resp.message,
                resp.headers.toMultimap(),
                text,
                resp.request.url.toString()
            )
        }
    }
}
