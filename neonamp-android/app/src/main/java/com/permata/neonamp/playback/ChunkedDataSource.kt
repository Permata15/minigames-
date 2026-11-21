package com.permata.neonamp.playback

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.TransferListener

/**
 * Memecah request stream YouTube (googlevideo) menjadi potongan ber-Range berurutan,
 * seperti yang dilakukan NewPipe. Server YouTube cenderung memperlambat / memutus
 * koneksi panjang tanpa batas, jadi video bisa macet; potongan kecil tetap cepat.
 */
@OptIn(UnstableApi::class)
class ChunkedDataSource(
    private val upstream: DataSource,
    private val chunkSize: Long = 3L shl 20, // 3 MB
) : DataSource {

    class Factory(private val upstream: DataSource.Factory) : DataSource.Factory {
        override fun createDataSource(): DataSource = ChunkedDataSource(upstream.createDataSource())
    }

    private var passthrough = false
    private var spec: DataSpec? = null
    private var position = 0L
    private var end = C.LENGTH_UNSET.toLong()   // posisi akhir (eksklusif) bila diketahui
    private var total = C.LENGTH_UNSET.toLong() // ukuran file penuh bila diketahui
    private var chunkLeft = 0L
    private var upstreamOpen = false
    private var finished = false

    override fun addTransferListener(transferListener: TransferListener) = upstream.addTransferListener(transferListener)

    private fun chunkable(uri: Uri): Boolean =
        uri.host?.endsWith("googlevideo.com") == true && uri.path?.startsWith("/videoplayback") == true

    override fun open(dataSpec: DataSpec): Long {
        passthrough = !chunkable(dataSpec.uri)
        if (passthrough) return upstream.open(dataSpec)

        spec = dataSpec
        position = dataSpec.position
        end = if (dataSpec.length != C.LENGTH_UNSET.toLong()) position + dataSpec.length else C.LENGTH_UNSET.toLong()
        finished = false
        openChunk()
        return when {
            dataSpec.length != C.LENGTH_UNSET.toLong() -> dataSpec.length
            total != C.LENGTH_UNSET.toLong() -> total - dataSpec.position
            else -> C.LENGTH_UNSET.toLong()
        }
    }

    /** Buka potongan berikutnya. false = sudah di akhir file. */
    private fun openChunk(): Boolean {
        val s = spec ?: return false
        if (end != C.LENGTH_UNSET.toLong() && position >= end) return false
        if (total != C.LENGTH_UNSET.toLong() && position >= total) return false
        var len = chunkSize
        if (end != C.LENGTH_UNSET.toLong()) len = minOf(len, end - position)
        if (total != C.LENGTH_UNSET.toLong()) len = minOf(len, total - position)
        val sub = s.buildUpon().setPosition(position).setLength(len).build()
        val opened = try {
            upstream.open(sub)
        } catch (e: HttpDataSource.InvalidResponseCodeException) {
            if (e.responseCode == 416 && position > s.position) { upstream.close(); return false }
            throw e
        }
        upstreamOpen = true
        if (total == C.LENGTH_UNSET.toLong()) {
            total = upstream.responseHeaders.entries
                .firstOrNull { it.key.equals("Content-Range", ignoreCase = true) }
                ?.value?.firstOrNull()?.substringAfter('/')?.toLongOrNull() ?: C.LENGTH_UNSET.toLong()
        }
        chunkLeft = if (opened != C.LENGTH_UNSET.toLong()) opened else len
        return true
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (passthrough) return upstream.read(buffer, offset, length)
        if (length == 0) return 0
        if (finished) return C.RESULT_END_OF_INPUT
        if (chunkLeft <= 0L) {
            if (upstreamOpen) { upstream.close(); upstreamOpen = false }
            if (!openChunk()) { finished = true; return C.RESULT_END_OF_INPUT }
        }
        val n = upstream.read(buffer, offset, minOf(length.toLong(), chunkLeft).toInt())
        if (n == C.RESULT_END_OF_INPUT) {
            // Server mengirim lebih sedikit dari yang diminta → akhir file.
            finished = true
            return C.RESULT_END_OF_INPUT
        }
        position += n
        chunkLeft -= n
        return n
    }

    override fun getUri(): Uri? = upstream.uri

    override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

    override fun close() {
        upstreamOpen = false
        upstream.close()
    }
}
