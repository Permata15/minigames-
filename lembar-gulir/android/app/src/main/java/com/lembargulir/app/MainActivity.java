package com.lembargulir.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Hosts the Lembar Gulir web app (assets/index.html) in a WebView and adds what a
 * WebView lacks: a file picker for uploads, the device's text-to-speech engine
 * (exposed to the page as a speechSynthesis replacement), shared text from other
 * apps, and an "Artinya?" entry in the system text-selection menu.
 */
public class MainActivity extends Activity {

    // Fixed https origin so localStorage and IndexedDB persist between launches.
    private static final String BASE_URL = "https://lembar-gulir.app/";
    private static final int FILE_REQUEST = 1001;

    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private TextToSpeech tts;
    private volatile boolean ttsReady = false;
    private boolean pageLoaded = false;
    private String pendingMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);

        web.addJavascriptInterface(new TtsBridge(), "AndroidTTS");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (BASE_URL.startsWith(url.getScheme() + "://" + url.getHost())) return false;
                // Links such as Google Translate and KBBI open in the browser.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                } catch (ActivityNotFoundException ignored) {
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageLoaded = true;
                deliverPending();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Pilih buku"), FILE_REQUEST);
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });

        tts = new TextToSpeech(this, status -> {
            ttsReady = status == TextToSpeech.SUCCESS;
            if (ttsReady) {
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { ttsEvent(id, "start", ""); }
                    @Override public void onDone(String id) { ttsEvent(id, "end", ""); }
                    @Override public void onError(String id) { ttsEvent(id, "error", "synthesis-failed"); }
                    @Override public void onError(String id, int code) { ttsEvent(id, "error", "synthesis-failed"); }
                    @Override public void onStop(String id, boolean interrupted) { ttsEvent(id, "error", "interrupted"); }
                });
            }
            web.post(() -> web.evaluateJavascript("window.__ttsReady && window.__ttsReady()", null));
        });

        web.loadDataWithBaseURL(BASE_URL, readAsset("index.html"), "text/html", "UTF-8", null);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        try {
            JSONObject msg = new JSONObject();
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                if (text == null) return;
                msg.put("type", "text");
                msg.put("text", text.toString());
                String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                msg.put("title", subject == null ? "" : subject);
            } else if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
                CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
                if (text == null) return;
                msg.put("type", "word");
                msg.put("text", text.toString());
            } else {
                return;
            }
            pendingMessage = msg.toString();
            deliverPending();
        } catch (Exception ignored) {
        }
    }

    private void deliverPending() {
        if (!pageLoaded || pendingMessage == null) return;
        String js = "window.__fromAndroid && window.__fromAndroid(" + JSONObject.quote(pendingMessage) + ")";
        pendingMessage = null;
        web.evaluateJavascript(js, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_REQUEST || fileCallback == null) return;
        Uri[] result = null;
        if (resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                result = new Uri[]{data.getData()};
            } else if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                result = new Uri[]{data.getClipData().getItemAt(0).getUri()};
            }
        }
        fileCallback.onReceiveValue(result);
        fileCallback = null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        // Let the page close a sheet or go back to the feed first.
        web.evaluateJavascript("window.__back ? window.__back() : false", value -> {
            if (!"true".equals(value)) MainActivity.super.onBackPressed();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) tts.stop();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        if (web != null) web.destroy();
        super.onDestroy();
    }

    private void ttsEvent(String id, String type, String err) {
        String js = "window.__ttsEvent && window.__ttsEvent(" + JSONObject.quote(id) + "," + JSONObject.quote(type) + "," + JSONObject.quote(err) + ")";
        web.post(() -> web.evaluateJavascript(js, null));
    }

    private String readAsset(String name) {
        try (InputStream in = getAssets().open(name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return "<p>Aplikasi tidak bisa dimuat.</p>";
        }
    }

    /** Called from the page's speechSynthesis replacement. */
    private class TtsBridge {
        @JavascriptInterface
        public String voices() {
            JSONArray list = new JSONArray();
            if (!ttsReady) return list.toString();
            try {
                Set<Voice> all = tts.getVoices();
                Set<String> seen = new HashSet<>();
                if (all != null) {
                    for (Voice v : all) {
                        Locale loc = v.getLocale();
                        if (loc == null) continue;
                        String tag = loc.toLanguageTag();
                        if (!seen.add(tag)) continue;
                        JSONObject o = new JSONObject();
                        o.put("name", loc.getDisplayName(new Locale("id")));
                        o.put("lang", tag);
                        list.put(o);
                    }
                }
            } catch (Exception ignored) {
            }
            return list.toString();
        }

        @JavascriptInterface
        public void speak(String id, String text, String lang, float rate, float volume) {
            if (!ttsReady) {
                ttsEvent(id, "error", "synthesis-unavailable");
                return;
            }
            if (lang != null && !lang.isEmpty()) {
                int r = tts.setLanguage(Locale.forLanguageTag(lang));
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsEvent(id, "error", "language-unavailable");
                    return;
                }
            }
            tts.setSpeechRate(rate > 0 ? rate : 1f);
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, Math.max(0f, Math.min(1f, volume)));
            String say = text == null || text.trim().isEmpty() ? " " : text;
            if (tts.speak(say, TextToSpeech.QUEUE_ADD, params, id) != TextToSpeech.SUCCESS) {
                ttsEvent(id, "error", "synthesis-failed");
            }
        }

        @JavascriptInterface
        public void stop() {
            if (tts != null) tts.stop();
        }
    }
}
