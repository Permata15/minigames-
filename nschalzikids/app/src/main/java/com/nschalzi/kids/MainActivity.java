package com.nschalzi.kids;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayInputStream;

/**
 * Nschalzikids: penjelajah video anak yang hanya membuka YouTube,
 * dengan Mode Terbatas aktif dan pemblokir iklan bawaan (mirip Brave Shields).
 */
public class MainActivity extends Activity {

    private static final String HOME = "file:///android_asset/home.html";

    /** Domain/URL iklan & pelacak yang diblokir (seperti daftar filter Brave). */
    private static final String[] BLOCKED = {
            "doubleclick.net", "googleadservices.com", "googlesyndication.com",
            "google-analytics.com", "googletagmanager.com", "googletagservices.com",
            "adservice.google.", "imasdk.googleapis.com", "pagead2.", "ads.youtube.com",
            "/pagead/", "/api/stats/ads", "/ptracking", "/get_midroll_", "/youtubei/v1/log_event",
            "/generate_204", "/csi_204", "/api/stats/qoe", "/pcs/activeview", "/ad_status",
            "adformat=", "&ctier=l"
    };

    /** Hanya domain ini yang boleh dibuka anak. */
    private static final String[] ALLOWED_HOSTS = {
            "youtube.com", "youtu.be", "youtube-nocookie.com"
    };

    /** CSS + skrip yang menyembunyikan elemen iklan dan melewati iklan video. */
    private static final String SHIELD_JS =
            "(function(){if(window.__nsk)return;window.__nsk=1;" +
            "var css='ytm-promoted-sparkles-web-renderer,ytm-promoted-video-renderer,ytm-companion-ad-renderer," +
            "ad-slot-renderer,ytm-ad-slot-renderer,ytm-promoted-sparkles-text-search-renderer,.ytp-ad-overlay-container," +
            ".ytp-ad-module,.ytp-ad-image-overlay,.video-ads,#player-ads,.masthead-ad,ytm-paid-content-overlay-renderer," +
            "ytm-mealbar-promo-renderer,ytm-statement-banner-renderer,ytm-banner-promo-renderer,ytm-brand-video-singleton-renderer," +
            "ytm-item-section-renderer:has(ad-slot-renderer),ytm-rich-item-renderer:has(ad-slot-renderer)," +
            "ytm-app-promo-renderer{display:none!important}';" +
            "var s=document.createElement('style');s.textContent=css;(document.head||document.documentElement).appendChild(s);" +
            "setInterval(function(){" +
            " var v=document.querySelector('video');" +
            " var ad=document.querySelector('.ad-showing,.ad-interrupting');" +
            " if(v&&ad){if(!v.__nskAd){v.__nskAd=1;v.__nskWasMuted=v.muted;}v.muted=true;" +
            "  try{v.playbackRate=16;}catch(e){} if(isFinite(v.duration)&&v.duration>0)v.currentTime=v.duration;}" +
            " else if(v&&v.__nskAd){v.__nskAd=0;v.muted=!!v.__nskWasMuted;v.playbackRate=1;}" +
            " document.querySelectorAll('.ytp-ad-skip-button,.ytp-ad-skip-button-modern,.ytp-skip-ad-button," +
            "  .ytp-ad-skip-button-slot button,.ytm-skip-ad-button,[class*=skip-ad] button').forEach(function(b){b.click();});" +
            " document.querySelectorAll('ad-slot-renderer,ytm-promoted-sparkles-web-renderer').forEach(function(e){e.remove();});" +
            "},250);})();";

    private WebView web;
    private FrameLayout root;
    private FrameLayout fullscreenBox;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;
    private ImageButton homeBtn;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new FrameLayout(this);
        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));

        fullscreenBox = new FrameLayout(this);
        fullscreenBox.setBackgroundColor(Color.BLACK);
        fullscreenBox.setVisibility(View.GONE);
        root.addView(fullscreenBox, new FrameLayout.LayoutParams(-1, -1));

        homeBtn = buildHomeButton();
        root.addView(homeBtn);
        setContentView(root);

        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setSupportMultipleWindows(false);
        ws.setJavaScriptCanOpenWindowsAutomatically(false);
        ws.setAllowFileAccess(false);

        // Mode Terbatas YouTube (menyaring konten dewasa).
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, false);
        cm.setCookie("https://m.youtube.com", "PREF=f2=8000000&hl=id; domain=.youtube.com; path=/");
        cm.flush();

        web.setWebViewClient(new ShieldClient());
        web.setWebChromeClient(new FullscreenChrome());

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(HOME);
        }
    }

    private ImageButton buildHomeButton() {
        ImageButton b = new ImageButton(this);
        b.setImageResource(R.drawable.ic_logo);
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
        int size = dp(64);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, Gravity.BOTTOM | Gravity.END);
        lp.setMargins(0, 0, dp(16), dp(24));
        b.setLayoutParams(lp);
        b.setVisibility(View.GONE);
        b.setOnClickListener(v -> web.loadUrl(HOME));
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static boolean isBlocked(String url) {
        String u = url.toLowerCase();
        for (String b : BLOCKED) if (u.contains(b)) return true;
        return false;
    }

    private static boolean isAllowedHost(Uri uri) {
        String host = uri.getHost();
        if (host == null) return false;
        host = host.toLowerCase();
        for (String h : ALLOWED_HOSTS) {
            if (host.equals(h) || host.endsWith("." + h)) return true;
        }
        return false;
    }

    private class ShieldClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (isBlocked(request.getUrl().toString())) {
                return new WebResourceResponse("text/plain", "utf-8",
                        new ByteArrayInputStream(new byte[0]));
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            String scheme = uri.getScheme() == null ? "" : uri.getScheme();
            if (scheme.equals("file")) return false;
            if ((scheme.equals("https") || scheme.equals("http")) && isAllowedHost(uri)) {
                // Arahkan versi desktop ke versi mobile.
                if ("www.youtube.com".equalsIgnoreCase(uri.getHost())) {
                    view.loadUrl(uri.toString().replaceFirst("(?i)www\\.youtube\\.com", "m.youtube.com"));
                    return true;
                }
                return false;
            }
            Toast.makeText(MainActivity.this, "Link ini diblokir 🔒", Toast.LENGTH_SHORT).show();
            return true; // blokir tautan keluar / aplikasi lain
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            homeBtn.setVisibility(url != null && url.startsWith("file:") ? View.GONE : View.VISIBLE);
            if (url != null && url.startsWith("http")) view.evaluateJavascript(SHIELD_JS, null);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (url != null && url.startsWith("http")) view.evaluateJavascript(SHIELD_JS, null);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            // YouTube adalah SPA: suntik ulang saat URL berubah tanpa memuat ulang halaman.
            homeBtn.setVisibility(url != null && url.startsWith("file:") ? View.GONE : View.VISIBLE);
            if (url != null && url.startsWith("http")) view.evaluateJavascript(SHIELD_JS, null);
        }
    }

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
            homeBtn.setVisibility(View.GONE);
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
        homeBtn.setVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setFullscreenBars(false);
    }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
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
        root.removeAllViews();
        web.destroy();
        super.onDestroy();
    }
}
