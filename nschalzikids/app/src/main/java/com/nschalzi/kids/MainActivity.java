package com.nschalzi.kids;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.Locale;

/**
 * Nschalzikids: pemutar video anak.
 * - Hanya video dari channel anak yang diizinkan (daftar putih) yang bisa diputar.
 * - Iklan diblokir (jaringan + data iklan dihapus dari respons YouTube + lewati otomatis).
 * - Mode Orang Tua (dengan soal hitungan) untuk menambah / menghapus channel.
 */
public class MainActivity extends Activity {

    private static final String HOME = "file:///android_asset/home.html";
    private static final String PREFS = "nsk";
    private static final String KEY_CHANNELS = "channels_v1";

    /** Daftar channel bawaan: [kategori, emoji, nama, handle, channelId]. */
    private static final String[][] PRESET = {
            {"Islami", "🧕", "Nussa", "nussaofficialseries", ""},
            {"Islami", "👦", "Riko The Series", "rikotheseries", ""},
            {"Islami", "🌙", "Omar & Hana Indonesia", "laguanakanakislamiomarhana", ""},
            {"Islami", "⭐", "Omar & Hana", "omarhanalagukanakkanakislam", ""},
            {"Belajar", "👧", "Diva The Series", "divatheseries", "UCG9eNeLwgczW2cI1X51y1fw"},
            {"Belajar", "🎵", "Little Angel Indonesia", "littleangelindonesia", "UCdrHrQf0o0TO8YDntXJ4VIQ"},
            {"Belajar", "✏️", "YuBimba", "yubimba", ""},
            {"English", "🔢", "Numberblocks", "numberblocks", "UCPlwvN0w4qFSP1FllALB92w"},
            {"English", "🔤", "Alphablocks", "officialalphablocks", "UC_qs3c0ehDvZkbiEbOj6Drg"},
            {"English", "🎶", "Super Simple Songs", "supersimplesongs", ""},
            {"English", "🧸", "Sesame Street", "sesamestreet", ""},
            {"Sains", "🔬", "SciShow Kids", "scishowkids", ""},
            {"Sains", "🦒", "PBS KIDS", "pbskids", ""},
    };

    /** Domain/URL iklan & pelacak yang diblokir. */
    private static final String[] BLOCKED = {
            "doubleclick.net", "googleadservices.com", "googlesyndication.com",
            "google-analytics.com", "googletagmanager.com", "googletagservices.com",
            "adservice.google.", "imasdk.googleapis.com", "pagead2.", "ads.youtube.com",
            "/pagead/", "/api/stats/ads", "/api/stats/atr", "/ptracking", "/get_midroll_",
            "/youtubei/v1/log_event", "/csi_204", "/api/stats/qoe", "/pcs/activeview",
            "/ad_status", "adformat=", "&ctier=l"
    };

    private WebView web;
    private FrameLayout fullscreenBox;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;
    private ImageButton homeBtn;
    private TextView addBtn;
    private SharedPreferences prefs;
    private String shieldJs;

    private volatile boolean parentMode = false;
    private volatile String currentUrl = HOME;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        shieldJs = readAsset("shield.js");

        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));

        fullscreenBox = new FrameLayout(this);
        fullscreenBox.setBackgroundColor(Color.BLACK);
        fullscreenBox.setVisibility(View.GONE);
        root.addView(fullscreenBox, new FrameLayout.LayoutParams(-1, -1));

        homeBtn = buildHomeButton();
        root.addView(homeBtn);
        addBtn = buildAddButton();
        root.addView(addBtn);
        setContentView(root);

        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setSupportMultipleWindows(false);
        ws.setJavaScriptCanOpenWindowsAutomatically(false);
        ws.setAllowFileAccess(false);
        ws.setGeolocationEnabled(false);

        // Mode Terbatas YouTube (lapisan penyaring tambahan).
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, false);
        cm.setCookie("https://m.youtube.com", "PREF=f2=8000000&hl=id&f5=30000; domain=.youtube.com; path=/");
        cm.flush();

        web.addJavascriptInterface(new Bridge(), "NSK");
        web.setWebViewClient(new ShieldClient());
        web.setWebChromeClient(new FullscreenChrome());
        web.loadUrl(HOME);
    }

    // ------------------------------------------------------------------ channel list

    private JSONArray channels() {
        String s = prefs.getString(KEY_CHANNELS, null);
        try {
            if (s != null) return new JSONArray(s);
            JSONArray a = new JSONArray();
            for (String[] p : PRESET) {
                a.put(new JSONObject().put("c", p[0]).put("e", p[1]).put("n", p[2])
                        .put("h", p[3]).put("i", p[4]));
            }
            saveChannels(a);
            return a;
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveChannels(JSONArray a) {
        prefs.edit().putString(KEY_CHANNELS, a.toString()).apply();
    }

    private static String norm(String h) {
        if (h == null) return "";
        h = Uri.decode(h).trim();
        if (h.startsWith("@")) h = h.substring(1);
        return h.toLowerCase(Locale.ROOT);
    }

    private boolean handleAllowed(String h) {
        h = norm(h);
        if (h.isEmpty()) return false;
        JSONArray a = channels();
        for (int k = 0; k < a.length(); k++) {
            if (h.equals(a.optJSONObject(k).optString("h"))) return true;
        }
        return false;
    }

    private boolean idAllowed(String id) {
        if (id == null || id.isEmpty()) return false;
        JSONArray a = channels();
        for (int k = 0; k < a.length(); k++) {
            if (id.equals(a.optJSONObject(k).optString("i"))) return true;
        }
        return false;
    }

    /** Skrip yang disuntikkan ke halaman YouTube, beserta konfigurasi terkini. */
    private String pageScript() {
        JSONArray a = channels();
        JSONArray hs = new JSONArray(), ids = new JSONArray(), unl = new JSONArray();
        for (int k = 0; k < a.length(); k++) {
            JSONObject o = a.optJSONObject(k);
            if (!o.optString("h").isEmpty()) hs.put(o.optString("h"));
            if (!o.optString("i").isEmpty()) ids.put(o.optString("i"));
            else if (!o.optString("h").isEmpty()) unl.put(o.optString("h"));
        }
        return "window.__nskCfg={parent:" + parentMode + ",handles:" + hs + ",ids:" + ids
                + ",unl:" + unl + "};" + shieldJs;
    }

    // ------------------------------------------------------------------ navigation rules

    /** Apakah URL ini boleh dibuka dalam mode anak? */
    private boolean kidAllowed(Uri uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!(host.equals("youtube.com") || host.endsWith(".youtube.com"))) return false;
        String path = uri.getPath() == null ? "/" : uri.getPath();
        if (path.equals("/watch") || path.startsWith("/playlist")) return true; // video diperiksa per channel
        String[] seg = path.split("/");
        if (seg.length >= 2 && seg[1].startsWith("@")) return handleAllowed(seg[1]);
        if (seg.length >= 3 && seg[1].equals("channel")) return idAllowed(seg[2]);
        return false; // beranda, pencarian, shorts, feed, dll.
    }

    private boolean isYouTube(Uri uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        return host.equals("youtube.com") || host.endsWith(".youtube.com") || host.equals("youtu.be");
    }

    private void blockAndReturn(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (web.canGoBack()) web.goBack(); else web.loadUrl(HOME);
    }

    private void updateButtons(String url) {
        boolean onHome = url == null || url.startsWith("file:");
        homeBtn.setVisibility(onHome || customView != null ? View.GONE : View.VISIBLE);
        addBtn.setVisibility(parentMode && !onHome && customView == null ? View.VISIBLE : View.GONE);
    }

    private class ShieldClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String u = request.getUrl().toString().toLowerCase(Locale.ROOT);
            for (String b : BLOCKED) {
                if (u.contains(b)) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                }
            }
            return null;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            String scheme = uri.getScheme() == null ? "" : uri.getScheme();
            if (scheme.equals("file")) return false;
            if (!scheme.startsWith("http") || !isYouTube(uri)) {
                Toast.makeText(MainActivity.this, "Link ini diblokir 🔒", Toast.LENGTH_SHORT).show();
                return true;
            }
            if ("youtu.be".equalsIgnoreCase(uri.getHost())) {
                view.loadUrl("https://m.youtube.com/watch?v=" + uri.getLastPathSegment());
                return true;
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (!host.equals("m.youtube.com")) {
                view.loadUrl(uri.buildUpon().authority("m.youtube.com").build().toString());
                return true;
            }
            if (!parentMode && !kidAllowed(uri)) {
                Toast.makeText(MainActivity.this, "Hanya channel anak yang diizinkan 🔒", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            currentUrl = url;
            updateButtons(url);
            if (url != null && url.startsWith("http")) view.evaluateJavascript(pageScript(), null);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (url != null && url.startsWith("http")) view.evaluateJavascript(pageScript(), null);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            // YouTube berpindah halaman tanpa memuat ulang: periksa setiap perubahan URL.
            currentUrl = url;
            updateButtons(url);
            if (url == null || !url.startsWith("http")) return;
            if (!parentMode && !kidAllowed(Uri.parse(url))) {
                view.post(() -> blockAndReturn("Hanya channel anak yang diizinkan 🔒"));
                return;
            }
            view.evaluateJavascript(pageScript(), null);
        }
    }

    // ------------------------------------------------------------------ JS bridge

    private boolean onHomePage() {
        return currentUrl != null && currentUrl.startsWith("file:");
    }

    private class Bridge {
        @JavascriptInterface
        public String channels() {
            return MainActivity.this.channels().toString();
        }

        @JavascriptInterface
        public boolean isParent() {
            return parentMode;
        }

        @JavascriptInterface
        public void setParent(boolean on) {
            if (!onHomePage()) return;
            parentMode = on;
        }

        @JavascriptInterface
        public void remove(String handle, String id) {
            if (!onHomePage() || !parentMode) return;
            JSONArray a = MainActivity.this.channels(), out = new JSONArray();
            for (int k = 0; k < a.length(); k++) {
                JSONObject o = a.optJSONObject(k);
                boolean same = (!handle.isEmpty() && handle.equals(o.optString("h")))
                        || (handle.isEmpty() && !id.isEmpty() && id.equals(o.optString("i")));
                if (!same) out.put(o);
            }
            saveChannels(out);
        }

        @JavascriptInterface
        public void resetChannels() {
            if (!onHomePage() || !parentMode) return;
            prefs.edit().remove(KEY_CHANNELS).apply();
        }

        /** Halaman channel melaporkan ID channel miliknya (untuk channel yang baru punya handle). */
        @JavascriptInterface
        public void learn(String handle, String id) {
            handle = norm(handle);
            if (handle.isEmpty() || id == null || !id.startsWith("UC")) return;
            String url = currentUrl == null ? "" : currentUrl.toLowerCase(Locale.ROOT);
            if (!url.contains("/@" + handle)) return;
            JSONArray a = MainActivity.this.channels();
            boolean changed = false;
            for (int k = 0; k < a.length(); k++) {
                JSONObject o = a.optJSONObject(k);
                if (handle.equals(o.optString("h")) && o.optString("i").isEmpty()) {
                    try { o.put("i", id); changed = true; } catch (Exception ignored) { }
                }
            }
            if (changed) saveChannels(a);
        }

        /** Hasil pemeriksaan video: false = bukan dari channel yang diizinkan. */
        @JavascriptInterface
        public void verdict(boolean ok) {
            if (ok || parentMode) return;
            web.post(() -> {
                pauseMedia();
                blockAndReturn("Video ini bukan dari channel anak 🔒");
            });
        }
    }

    private void pauseMedia() {
        web.evaluateJavascript("document.querySelectorAll('video').forEach(function(v){v.pause()})", null);
    }

    // ------------------------------------------------------------------ parent: add channel

    private void addCurrentChannel() {
        web.evaluateJavascript("window.__nskInfo?JSON.stringify(window.__nskInfo()):'null'", value -> {
            try {
                String raw = new JSONArray("[" + value + "]").optString(0, "null");
                if (raw.equals("null")) throw new Exception();
                JSONObject info = new JSONObject(raw);
                String h = norm(info.optString("h"));
                String id = info.optString("i");
                String name = info.optString("n").replace(" - YouTube", "").trim();
                if (h.isEmpty() && !id.startsWith("UC")) throw new Exception();
                if (name.isEmpty()) name = h.isEmpty() ? id : "@" + h;
                JSONArray a = channels();
                for (int k = 0; k < a.length(); k++) {
                    JSONObject o = a.optJSONObject(k);
                    if ((!h.isEmpty() && h.equals(o.optString("h"))) || (!id.isEmpty() && id.equals(o.optString("i")))) {
                        if (o.optString("h").isEmpty()) o.put("h", h);
                        if (o.optString("i").isEmpty()) o.put("i", id);
                        saveChannels(a);
                        Toast.makeText(this, "✅ " + o.optString("n") + " sudah diizinkan", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                a.put(new JSONObject().put("c", "Pilihan Orang Tua").put("e", "⭐").put("n", name)
                        .put("h", h).put("i", id.startsWith("UC") ? id : ""));
                saveChannels(a);
                Toast.makeText(this, "✅ " + name + " ditambahkan", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Buka halaman channel atau video dulu, lalu coba lagi", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ------------------------------------------------------------------ UI helpers

    private ImageButton buildHomeButton() {
        ImageButton b = new ImageButton(this);
        b.setImageResource(R.drawable.ic_launcher_foreground);
        b.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        b.setPadding(0, 0, 0, 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#FFE3EE"));
        bg.setStroke(dp(3), Color.parseColor("#E8467C"));
        b.setBackground(bg);
        b.setElevation(dp(8));
        b.setClipToOutline(true);
        b.setContentDescription("Beranda");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(64), dp(64), Gravity.BOTTOM | Gravity.END);
        lp.setMargins(0, 0, dp(16), dp(24));
        b.setLayoutParams(lp);
        b.setVisibility(View.GONE);
        b.setOnClickListener(v -> web.loadUrl(HOME));
        return b;
    }

    private TextView buildAddButton() {
        TextView t = new TextView(this);
        t.setText("➕ Izinkan channel ini");
        t.setTextColor(Color.WHITE);
        t.setTextSize(15);
        t.setPadding(dp(18), dp(12), dp(18), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(30));
        bg.setColor(Color.parseColor("#2E9E6B"));
        t.setBackground(bg);
        t.setElevation(dp(8));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.START);
        lp.setMargins(dp(16), 0, 0, dp(32));
        t.setLayoutParams(lp);
        t.setVisibility(View.GONE);
        t.setOnClickListener(v -> addCurrentChannel());
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private String readAsset(String name) {
        try (java.io.InputStream in = getAssets().open(name)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    // ------------------------------------------------------------------ fullscreen video

    private class FullscreenChrome extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;
            customCallback = callback;
            fullscreenBox.addView(view, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fullscreenBox.setVisibility(View.VISIBLE);
            web.setVisibility(View.GONE);
            updateButtons(currentUrl);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            setFullscreenBars(true);
        }

        @Override
        public void onHideCustomView() {
            exitFullscreen();
        }
    }

    private void exitFullscreen() {
        if (customView == null) return;
        fullscreenBox.removeView(customView);
        fullscreenBox.setVisibility(View.GONE);
        customView = null;
        if (customCallback != null) customCallback.onCustomViewHidden();
        customCallback = null;
        web.setVisibility(View.VISIBLE);
        updateButtons(currentUrl);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setFullscreenBars(false);
    }

    @SuppressWarnings("deprecation")
    private void setFullscreenBars(boolean on) {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c == null) return;
            if (on) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                c.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(on
                    ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    : View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    // ------------------------------------------------------------------ lifecycle

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (customView != null) {
            exitFullscreen();
        } else if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Mode Orang Tua otomatis terkunci lagi saat aplikasi ditinggalkan.
        if (parentMode) {
            parentMode = false;
            web.loadUrl(HOME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }
}
