package com.core9.mechachat;

import android.view.WindowManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Pengunduh file model AI berukuran besar (> 2 GB).
 * - ukuran & progres memakai long, jadi file di atas 2 GB tetap terbaca
 * - melanjutkan unduhan yang terputus (HTTP Range) dari file .part
 * - mengikuti redirect antar-server secara manual agar Range tetap terkirim
 * - menjaga layar tetap menyala selama mengunduh
 */
@CapacitorPlugin(name = "ModelDownloader")
public class ModelDownloaderPlugin extends Plugin {

    private volatile boolean cancelled = false;
    private volatile boolean running = false;

    private File modelsDir() {
        File dir = new File(getContext().getFilesDir(), "models");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    @PluginMethod
    public void info(PluginCall call) {
        String filename = call.getString("filename");
        if (filename == null) {
            call.reject("filename wajib diisi");
            return;
        }
        File out = new File(modelsDir(), filename);
        File part = new File(modelsDir(), filename + ".part");
        JSObject ret = new JSObject();
        ret.put("exists", out.exists() && out.length() > 0);
        ret.put("path", out.getAbsolutePath());
        ret.put("size", out.exists() ? out.length() : 0L);
        ret.put("partial", part.exists() ? part.length() : 0L);
        ret.put("freeBytes", getContext().getFilesDir().getUsableSpace());
        call.resolve(ret);
    }

    @PluginMethod
    public void remove(PluginCall call) {
        String filename = call.getString("filename");
        if (filename != null) {
            new File(modelsDir(), filename).delete();
            new File(modelsDir(), filename + ".part").delete();
        }
        // sisa unduhan versi lama (tersimpan langsung di folder files)
        String legacy = call.getString("legacy");
        if (legacy != null) new File(getContext().getFilesDir(), legacy).delete();
        call.resolve();
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        cancelled = true;
        call.resolve();
    }

    @PluginMethod
    public void download(PluginCall call) {
        final String url = call.getString("url");
        final String filename = call.getString("filename");
        if (url == null || filename == null) {
            call.reject("url dan filename wajib diisi");
            return;
        }
        if (running) {
            call.reject("Unduhan lain masih berjalan");
            return;
        }
        running = true;
        cancelled = false;
        keepScreenOn(true);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                File out = new File(modelsDir(), filename);
                File part = new File(modelsDir(), filename + ".part");
                if (out.exists() && out.length() > 0) {
                    resolvePath(call, out);
                    return;
                }

                long have = part.exists() ? part.length() : 0L;
                String current = url;
                int code = -1;
                for (int hop = 0; hop < 10; hop++) {
                    conn = (HttpURLConnection) new URL(current).openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(90000);
                    conn.setRequestProperty("User-Agent", "CORE9-Android/1.0");
                    if (have > 0) conn.setRequestProperty("Range", "bytes=" + have + "-");
                    code = conn.getResponseCode();
                    if (code >= 300 && code < 400) {
                        String loc = conn.getHeaderField("Location");
                        conn.disconnect();
                        if (loc == null) throw new IOException("Server mengalihkan tanpa alamat tujuan");
                        current = new URL(new URL(current), loc).toString();
                        continue;
                    }
                    break;
                }

                if (code == 416 && have > 0) {
                    // seluruh file sudah ada di .part
                    if (!part.renameTo(out)) throw new IOException("Gagal menyimpan file model");
                    resolvePath(call, out);
                    return;
                }
                if (code == 401 || code == 403) throw new IOException("Server menolak (HTTP " + code + "): file model ini butuh login/izin");
                if (code == 404) throw new IOException("File model tidak ditemukan di server (HTTP 404)");
                if (code != 200 && code != 206) throw new IOException("Server menjawab HTTP " + code);

                boolean append = code == 206;
                if (!append) have = 0L;
                long len = conn.getContentLengthLong();
                long total = len > 0 ? len + have : -1L;

                long free = getContext().getFilesDir().getUsableSpace();
                if (total > 0 && free < (total - have) + 200L * 1024 * 1024) {
                    throw new IOException(
                        "Ruang penyimpanan kurang. Butuh " + gb(total - have) + " GB, tersisa " + gb(free) + " GB"
                    );
                }

                try (
                    InputStream in = new BufferedInputStream(conn.getInputStream(), 1 << 16);
                    FileOutputStream fos = new FileOutputStream(part, append)
                ) {
                    byte[] buf = new byte[1 << 16];
                    long done = have;
                    long lastEmit = 0L;
                    int n;
                    emit(done, total);
                    while ((n = in.read(buf)) != -1) {
                        if (cancelled) throw new IOException("Unduhan dibatalkan");
                        fos.write(buf, 0, n);
                        done += n;
                        long now = System.currentTimeMillis();
                        if (now - lastEmit > 500) {
                            lastEmit = now;
                            emit(done, total);
                        }
                    }
                    emit(done, total);
                }

                if (total > 0 && part.length() < total) {
                    throw new IOException("Unduhan terputus. Tekan COBA LAGI untuk melanjutkan dari titik terakhir");
                }
                if (!part.renameTo(out)) throw new IOException("Gagal menyimpan file model");
                resolvePath(call, out);
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null || msg.isEmpty()) msg = e.getClass().getSimpleName();
                if (e instanceof java.net.UnknownHostException) msg = "Tidak ada koneksi internet";
                if (e instanceof java.net.SocketTimeoutException) msg = "Koneksi terlalu lambat/terputus. Tekan COBA LAGI untuk melanjutkan";
                call.reject(msg);
            } finally {
                if (conn != null) conn.disconnect();
                running = false;
                keepScreenOn(false);
            }
        }).start();
    }

    private void resolvePath(PluginCall call, File out) {
        JSObject ret = new JSObject();
        ret.put("path", out.getAbsolutePath());
        ret.put("size", out.length());
        call.resolve(ret);
    }

    private void emit(long bytes, long total) {
        JSObject ev = new JSObject();
        ev.put("bytes", bytes);
        ev.put("total", total);
        notifyListeners("progress", ev);
    }

    private static String gb(long bytes) {
        return String.format(java.util.Locale.US, "%.1f", bytes / 1073741824.0);
    }

    private void keepScreenOn(boolean on) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (on) getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });
    }
}
