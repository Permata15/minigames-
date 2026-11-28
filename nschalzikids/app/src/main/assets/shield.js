/* Nschalzikids shield: blokir iklan + hanya video dari channel yang diizinkan. */
(function () {
  var root = document.documentElement;
  function cfg() { return window.__nskCfg || { parent: false, handles: [], ids: [] }; }
  root.classList.toggle('nsk-kid', !cfg().parent);
  if (window.__nsk) return;
  window.__nsk = 1;

  var vids = {};   // videoId -> {c: channelId, a: author}
  var chan = null; // info channel dari halaman channel terakhir

  /* ---------- 1. Hapus data iklan dari respons YouTube sebelum dipakai pemutar ---------- */
  var AD_KEYS = ['adPlacements', 'adSlots', 'playerAds', 'adBreakHeartbeatParams', 'adBreakParams'];
  function prune(o) {
    try {
      if (!o || typeof o !== 'object') return o;
      if (Array.isArray(o)) { o.forEach(prune); return o; }
      AD_KEYS.forEach(function (k) { if (k in o) delete o[k]; });
      if (o.playerResponse) prune(o.playerResponse);
      if (o.response) prune(o.response);
      var d = o.videoDetails;
      if (d && d.videoId) vids[d.videoId] = { c: d.channelId, a: d.author };
      var m = o.metadata && o.metadata.channelMetadataRenderer;
      if (m && m.externalId) chan = { i: m.externalId, n: m.title, u: m.vanityChannelUrl || '' };
    } catch (e) {}
    return o;
  }
  var jp = JSON.parse;
  JSON.parse = function () { return prune(jp.apply(this, arguments)); };
  if (window.Response) {
    var rj = Response.prototype.json;
    Response.prototype.json = function () { return rj.call(this).then(prune); };
  }
  ['ytInitialPlayerResponse', 'ytInitialData'].forEach(function (n) {
    var cur = window[n];
    if (typeof cur === 'string') { try { cur = JSON.parse(cur); } catch (e) {} } else prune(cur);
    try {
      Object.defineProperty(window, n, {
        configurable: true,
        get: function () { return cur; },
        set: function (v) { cur = prune(v); }
      });
    } catch (e) {}
  });

  /* ---------- 2. Sembunyikan iklan & elemen yang tidak cocok untuk anak ---------- */
  var ads = 'ytm-promoted-sparkles-web-renderer,ytm-promoted-video-renderer,ytm-companion-ad-renderer,' +
    'ad-slot-renderer,ytm-ad-slot-renderer,ytm-promoted-sparkles-text-search-renderer,.ytp-ad-overlay-container,' +
    '.ytp-ad-module,.ytp-ad-image-overlay,.video-ads,#player-ads,.masthead-ad,ytm-paid-content-overlay-renderer,' +
    'ytm-mealbar-promo-renderer,ytm-statement-banner-renderer,ytm-banner-promo-renderer,' +
    'ytm-brand-video-singleton-renderer,ytm-app-promo-renderer,ytm-merch-shelf-renderer,ytm-ticket-shelf-renderer,' +
    'ytm-item-section-renderer:has(ad-slot-renderer),ytm-rich-item-renderer:has(ad-slot-renderer)';
  var kid = [
    'ytm-comment-section-renderer', 'ytm-comments-entry-point-header-renderer', 'ytm-comments-simplebox-renderer',
    'ytm-item-section-renderer[section-identifier="related-items"]', 'ytm-item-section-renderer[data-content-type="related"]',
    'ytm-reel-shelf-renderer', 'ytm-shorts-lockup-view-model', 'ytm-pivot-bar-renderer',
    'ytm-topbar-menu-button-renderer', 'button[aria-label*="Search"]', 'button[aria-label*="Telusuri"]',
    'button[aria-label*="Cari"]', '.mobile-topbar-header-endpoint', 'ytm-autonav-toggle-button-container',
    '.ytp-ce-element', '.ytp-endscreen-content', '.ytp-pause-overlay', '.ytp-suggested-action',
    'ytm-subscribe-button-renderer', 'ytm-backstage-post-thread-renderer', 'ytm-mobile-topbar-renderer .topbar-menu-button-avatar-button'
  ].map(function (s) { return 'html.nsk-kid ' + s; }).join(',');
  var css = document.createElement('style');
  css.textContent = ads + '{display:none!important}' + kid + '{display:none!important}' +
    'html.nsk-kid.nsk-check #player-container-id,html.nsk-kid.nsk-check .player-container,' +
    'html.nsk-kid.nsk-check ytm-single-column-watch-next-results-renderer{visibility:hidden!important}' +
    'html.nsk-kid.nsk-check body:after{content:"⏳ Memeriksa video...";position:fixed;top:40%;left:0;right:0;' +
    'text-align:center;font-size:20px;color:#E8467C;font-family:sans-serif}';
  (document.head || root).appendChild(css);

  /* ---------- 3. Periksa pemilik video (hanya channel yang diizinkan) ---------- */
  function norm(h) {
    try { h = decodeURIComponent(h); } catch (e) {}
    return h.replace(/^@/, '').toLowerCase();
  }
  function ownerFromDom() {
    var a = document.querySelector('ytm-slim-owner-renderer a[href],a.slim-owner-icon-and-title[href],' +
      'ytm-slim-video-metadata-section-renderer a[href^="/@"],ytm-slim-video-metadata-section-renderer a[href^="/channel/"]');
    if (!a) return null;
    var href = a.getAttribute('href') || '';
    var name = (document.querySelector('ytm-slim-owner-renderer .slim-owner-channel-name,ytm-slim-owner-renderer h3') || a)
      .textContent.trim();
    var m = href.match(/^\/@([^\/?#]+)/), c = href.match(/^\/channel\/(UC[\w-]+)/);
    return { h: m ? norm(m[1]) : '', i: c ? c[1] : '', n: name };
  }
  function pathHandle() {
    var m = location.pathname.match(/^\/@([^\/?#]+)/);
    return m ? norm(m[1]) : '';
  }
  // Info untuk tombol "Izinkan channel ini" (mode orang tua).
  window.__nskInfo = function () {
    if (location.pathname === '/watch') {
      var v = new URLSearchParams(location.search).get('v');
      var o = ownerFromDom() || { h: '', i: '', n: '' };
      var r = vids[v];
      if (r) { o.i = r.c || o.i; o.n = r.a || o.n; }
      return o;
    }
    var h = pathHandle();
    var ci = location.pathname.match(/^\/channel\/(UC[\w-]+)/);
    var info = { h: h, i: ci ? ci[1] : '', n: document.title };
    if (chan && (ci ? chan.i === ci[1] : chan.u.toLowerCase().indexOf('/@' + h) >= 0)) {
      info.i = chan.i; info.n = chan.n;
    }
    return info;
  };

  var lastHref = '', since = 0, decided = false;
  function check() {
    var c = cfg();
    root.classList.toggle('nsk-kid', !c.parent);
    if (location.href !== lastHref) { lastHref = location.href; since = Date.now(); decided = false; }

    // Halaman channel yang diizinkan: pelajari ID channel-nya.
    var h = pathHandle();
    if (h && chan && chan.u.toLowerCase().indexOf('/@' + h) >= 0 && c.handles.indexOf(h) >= 0 && c.ids.indexOf(chan.i) < 0) {
      try { NSK.learn(h, chan.i); } catch (e) {}
      c.ids.push(chan.i);
    }

    if (c.parent || location.pathname !== '/watch') { root.classList.remove('nsk-check'); return; }
    if (decided) return;
    root.classList.add('nsk-check');
    var vid = document.querySelector('video');
    if (vid && !vid.paused) vid.pause();

    var v = new URLSearchParams(location.search).get('v');
    var waited = Date.now() - since;
    var ok = null;
    var r = vids[v];
    var o = waited > 1500 ? ownerFromDom() : null;
    var domMatch = !!(o && ((o.h && c.handles.indexOf(o.h) >= 0) || (o.i && c.ids.indexOf(o.i) >= 0)));
    if (r && r.c) {
      // Data resmi video tersedia: channel ID harus ada di daftar.
      ok = c.ids.indexOf(r.c) >= 0;
      // Pengecualian: channel diizinkan lewat handle tetapi ID-nya belum diketahui.
      if (!ok && o && o.h && (c.unl || []).indexOf(o.h) >= 0 && (!o.i || o.i === r.c)) ok = true;
      if (!ok && waited < 1500) ok = null; // beri waktu sebentar sebelum memutuskan
    } else if (o) {
      ok = domMatch;
    }
    if (ok === null && waited > 9000) ok = false; // tidak bisa dipastikan: blokir demi keamanan
    if (ok === null) return;
    decided = true;
    root.classList.remove('nsk-check');
    if (ok) { if (vid) vid.play().catch(function () {}); }
    try { NSK.verdict(ok); } catch (e) {}
  }

  /* ---------- 4. Cadangan: lewati iklan video yang masih lolos ---------- */
  function skipAds() {
    var v = document.querySelector('video');
    var ad = document.querySelector('.ad-showing,.ad-interrupting');
    if (v && ad) {
      if (!v.__nskAd) { v.__nskAd = 1; v.__nskMuted = v.muted; }
      v.muted = true;
      try { v.playbackRate = 16; } catch (e) {}
      if (isFinite(v.duration) && v.duration > 0) v.currentTime = v.duration;
    } else if (v && v.__nskAd) {
      v.__nskAd = 0; v.muted = !!v.__nskMuted; v.playbackRate = 1;
    }
    document.querySelectorAll('.ytp-ad-skip-button,.ytp-ad-skip-button-modern,.ytp-skip-ad-button,' +
      '.ytp-ad-skip-button-slot button,.ytm-skip-ad-button,[class*=skip-ad] button').forEach(function (b) { b.click(); });
    document.querySelectorAll('ad-slot-renderer,ytm-promoted-sparkles-web-renderer,ytm-ad-slot-renderer')
      .forEach(function (e) { e.remove(); });
  }

  setInterval(function () { skipAds(); check(); }, 250);
})();
