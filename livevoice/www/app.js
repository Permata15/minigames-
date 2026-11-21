/* LiveVoice AI v2 — teman ngobrol suara, penerjemah dua arah & teleprompter.
 * Tanpa framework/CDN supaya ringan, cepat, dan bisa jalan offline (kecuali panggilan AI/terjemahan).
 * Di APK (Capacitor) memakai pengenal suara & TTS native Android; di browser memakai Web Speech API.
 */
'use strict';

/* ============================== Utilitas ============================== */
const $ = (s) => document.querySelector(s);
const $$ = (s) => [...document.querySelectorAll(s)];
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

function toast(msg, ms = 2200) {
  const t = $('#toast');
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(toast._t);
  toast._t = setTimeout(() => t.classList.remove('show'), ms);
}

function store(key, val) {
  try {
    if (val === undefined) return JSON.parse(localStorage.getItem('lv2.' + key));
    localStorage.setItem('lv2.' + key, JSON.stringify(val));
  } catch { return null; }
}

async function copyText(text) {
  try { await navigator.clipboard.writeText(text); }
  catch {
    const ta = document.createElement('textarea');
    ta.value = text; document.body.appendChild(ta); ta.select();
    document.execCommand('copy'); ta.remove();
  }
  toast('Disalin ke clipboard');
}

/* ============================== Setelan ============================== */
const DEFAULTS = {
  apiKey: '', model: 'gemini-flash-latest', engine: 'ai', handsfree: true, search: true,
  silenceMs: 1000, name: 'Nara', style: 'friend', rate: 1.05, pitch: 1.0, ttsOn: true, talkLang: 'auto',
  trA: 'id', trB: 'en', prLang: 'id', prFont: 28,
};
const S = Object.assign({}, DEFAULTS, store('settings') || {});
const saveSettings = () => store('settings', S);

/* ============================== Bahasa ============================== */
// code: kode BCP-47 untuk STT/TTS, tr: kode Google Translate
const LANGS = [
  { code: 'id-ID', tr: 'id', name: 'Indonesia', flag: '🇮🇩' },
  { code: 'en-US', tr: 'en', name: 'English', flag: '🇺🇸' },
  { code: 'jv-ID', tr: 'jw', name: 'Jawa', flag: '🇮🇩', ttsFallback: 'id-ID' },
  { code: 'su-ID', tr: 'su', name: 'Sunda', flag: '🇮🇩', ttsFallback: 'id-ID' },
  { code: 'ms-MY', tr: 'ms', name: 'Melayu', flag: '🇲🇾' },
  { code: 'ja-JP', tr: 'ja', name: '日本語 Jepang', flag: '🇯🇵', cjk: true },
  { code: 'ko-KR', tr: 'ko', name: '한국어 Korea', flag: '🇰🇷' },
  { code: 'zh-CN', tr: 'zh-CN', name: '中文 Mandarin', flag: '🇨🇳', cjk: true },
  { code: 'ar-SA', tr: 'ar', name: 'العربية Arab', flag: '🇸🇦', rtl: true },
  { code: 'hi-IN', tr: 'hi', name: 'हिन्दी Hindi', flag: '🇮🇳' },
  { code: 'th-TH', tr: 'th', name: 'ไทย Thai', flag: '🇹🇭', cjk: true },
  { code: 'vi-VN', tr: 'vi', name: 'Tiếng Việt', flag: '🇻🇳' },
  { code: 'tl-PH', tr: 'tl', name: 'Filipino', flag: '🇵🇭' },
  { code: 'es-ES', tr: 'es', name: 'Español', flag: '🇪🇸' },
  { code: 'fr-FR', tr: 'fr', name: 'Français', flag: '🇫🇷' },
  { code: 'de-DE', tr: 'de', name: 'Deutsch', flag: '🇩🇪' },
  { code: 'it-IT', tr: 'it', name: 'Italiano', flag: '🇮🇹' },
  { code: 'pt-BR', tr: 'pt', name: 'Português', flag: '🇧🇷' },
  { code: 'nl-NL', tr: 'nl', name: 'Nederlands', flag: '🇳🇱' },
  { code: 'ru-RU', tr: 'ru', name: 'Русский', flag: '🇷🇺' },
  { code: 'tr-TR', tr: 'tr', name: 'Türkçe', flag: '🇹🇷' },
];
const langByTr = (tr) => LANGS.find((l) => l.tr === tr) || LANGS[0];
function langByAny(code) {
  if (!code) return null;
  const c = code.replace('_', '-').toLowerCase();
  return LANGS.find((l) => l.code.toLowerCase() === c) ||
    LANGS.find((l) => l.tr.toLowerCase() === c) ||
    LANGS.find((l) => l.code.toLowerCase().split('-')[0] === c.split('-')[0]) || null;
}
function normLang(code) {
  // Menjadikan "en" / "en_US" / "EN-us" → "en-US"
  if (!code) return 'id-ID';
  const l = langByAny(code);
  if (l) return l.code;
  const [a, b] = code.replace('_', '-').split('-');
  return b ? `${a.toLowerCase()}-${b.toUpperCase()}` : a.toLowerCase();
}
const deviceLang = normLang(navigator.language || 'id-ID');

/* ============================== Platform ============================== */
const Cap = window.Capacitor;
const isNative = !!(Cap && Cap.isNativePlatform && Cap.isNativePlatform());
const registerPlugin = (Cap && Cap.registerPlugin) || (window.capacitorExports && window.capacitorExports.registerPlugin);
const NativeSR = isNative && registerPlugin ? registerPlugin('SpeechRecognition') : null;
const NativeTTS = isNative && registerPlugin ? registerPlugin('TextToSpeech') : null;
const WebSR = window.SpeechRecognition || window.webkitSpeechRecognition;

/* ============================== TTS (suara keluar) ============================== */
const TTS = {
  gen: 0, pending: 0, waiters: [], nativeLangs: null, voices: [],

  async init() {
    if (NativeTTS) {
      try { this.nativeLangs = (await NativeTTS.getSupportedLanguages()).languages.map((x) => x.replace('_', '-')); }
      catch { this.nativeLangs = null; }
    } else if (window.speechSynthesis) {
      const load = () => { this.voices = speechSynthesis.getVoices(); };
      load();
      speechSynthesis.onvoiceschanged = load;
    }
  },

  resolveLang(code) {
    const l = langByAny(code);
    let want = normLang(code);
    const list = NativeTTS ? (this.nativeLangs || []) : this.voices.map((v) => v.lang.replace('_', '-'));
    if (!list.length) return l && l.ttsFallback ? l.ttsFallback : want;
    const lower = list.map((x) => x.toLowerCase());
    if (lower.includes(want.toLowerCase())) return want;
    const base = want.split('-')[0];
    const i = lower.findIndex((x) => x.split('-')[0] === base);
    if (i >= 0) return list[i];
    if (l && l.ttsFallback) return l.ttsFallback;
    return want;
  },

  pickVoice(lang) {
    const vs = this.voices.filter((v) => v.lang.replace('_', '-').toLowerCase() === lang.toLowerCase());
    const pool = vs.length ? vs : this.voices.filter((v) => v.lang.toLowerCase().startsWith(lang.split('-')[0]));
    // utamakan suara "natural/online/google" yang lebih manusiawi
    return pool.find((v) => /natural|neural|online|google/i.test(v.name)) || pool[0] || null;
  },

  /** Masukkan kalimat ke antrian bicara. Kembali saat kalimat selesai dibacakan. */
  speak(text, lang) {
    text = cleanForSpeech(text);
    if (!text) return Promise.resolve();
    const gen = this.gen;
    const code = this.resolveLang(lang);
    this.pending++;
    const done = () => { if (gen === this.gen) { this.pending = Math.max(0, this.pending - 1); this._check(); } };
    if (NativeTTS) {
      return NativeTTS.speak({ text, lang: code, rate: S.rate, pitch: S.pitch, volume: 1, category: 'playback', queueStrategy: 1 })
        .catch((e) => console.warn('TTS', e)).finally(done);
    }
    if (!window.speechSynthesis) { done(); return Promise.resolve(); }
    return new Promise((res) => {
      const u = new SpeechSynthesisUtterance(text);
      u.lang = code; u.rate = S.rate; u.pitch = S.pitch;
      const v = this.pickVoice(code); if (v) u.voice = v;
      let fired = false;
      const fin = () => { if (fired) return; fired = true; done(); res(); };
      u.onend = fin; u.onerror = fin;
      speechSynthesis.speak(u);
      // pengaman bila browser tidak memanggil onend
      setTimeout(fin, 4000 + text.length * 160);
    });
  },

  get busy() { return this.pending > 0; },

  /** Menunggu sampai semua antrian bicara selesai atau dihentikan. */
  idle() {
    if (!this.pending) return Promise.resolve();
    return new Promise((r) => this.waiters.push(r));
  },

  _check() {
    if (this.pending === 0) { const w = this.waiters; this.waiters = []; w.forEach((r) => r()); }
  },

  stop() {
    this.gen++; this.pending = 0; this._check();
    if (NativeTTS) NativeTTS.stop().catch(() => {});
    else if (window.speechSynthesis) speechSynthesis.cancel();
  },
};

function cleanForSpeech(t) {
  return String(t || '')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/https?:\/\/\S+/g, '')
    .replace(/[*_#`>~|]/g, '')
    .replace(/^\s*[-•]\s+/gm, '')
    .replace(/\p{Extended_Pictographic}/gu, '')
    .replace(/\s+/g, ' ')
    .trim();
}

/** Memecah teks streaming menjadi kalimat agar suara bisa mulai sebelum jawaban selesai. */
class SentenceStream {
  constructor(onSentence) { this.buf = ''; this.onSentence = onSentence; this.count = 0; }
  push(text) {
    this.buf += text;
    const re = /[\s\S]*?(?:[.!?…]+["'”’)\]]*(?=\s)|[。！？؟\n])/y;
    let m;
    re.lastIndex = 0;
    let consumed = 0;
    while ((m = re.exec(this.buf)) && m[0].length) {
      const s = m[0].trim();
      if (s.length > 1) { this.onSentence(s); this.count++; }
      consumed = re.lastIndex;
    }
    this.buf = this.buf.slice(consumed);
    // kalimat pertama yang panjang: potong di koma supaya suara cepat mulai
    if (this.count === 0 && this.buf.length > 90) {
      const i = this.buf.search(/[,，、;:]\s/);
      if (i > 25) { this.onSentence(this.buf.slice(0, i + 1).trim()); this.count++; this.buf = this.buf.slice(i + 1); }
    }
  }
  flush() { const s = this.buf.trim(); this.buf = ''; if (s) this.onSentence(s); }
}

/* ============================== STT perangkat (teks real-time) ============================== */
const DeviceSTT = {
  available: !!(NativeSR || WebSR),
  permOk: false,

  async ensurePermission() {
    if (!NativeSR || this.permOk) return true;
    try {
      let p = await NativeSR.checkPermissions();
      if (p.speechRecognition !== 'granted') p = await NativeSR.requestPermissions();
      this.permOk = p.speechRecognition === 'granted';
    } catch { this.permOk = false; }
    if (!this.permOk) toast('Izin mikrofon ditolak');
    return this.permOk;
  },

  /**
   * Mendengar satu ucapan. Mengembalikan objek { promise, stop() }.
   * promise → teks akhir ('' jika tidak ada ucapan).
   */
  listen({ lang, onPartial, silenceMs = 1000 }) {
    if (NativeSR) return this._native(lang, onPartial, silenceMs);
    if (WebSR) return this._web(lang, onPartial);
    return { promise: Promise.resolve(''), stop() {} };
  },

  _native(lang, onPartial, silenceMs) {
    let last = '', finished = false, stoppedAt = 0, resolveFn;
    const handles = [];
    let tNoSpeech, tIdle, tAfter;
    const promise = new Promise((r) => (resolveFn = r));
    const finish = () => {
      if (finished) return; finished = true;
      [tNoSpeech, tIdle, tAfter].forEach(clearTimeout);
      handles.forEach((h) => h.then((x) => x.remove()).catch(() => {}));
      NativeSR.stop().catch(() => {});
      resolveFn(last.trim());
    };
    handles.push(NativeSR.addListener('partialResults', (d) => {
      const t = (d.matches && d.matches[0]) || '';
      if (t) { last = t; onPartial && onPartial(t); }
      clearTimeout(tNoSpeech); clearTimeout(tIdle);
      if (stoppedAt) { finish(); return; } // hasil final setelah akhir ucapan
      tIdle = setTimeout(finish, silenceMs + 1800);
    }));
    handles.push(NativeSR.addListener('listeningState', (d) => {
      if (d.status === 'started') clearTimeout(tNoSpeech);
      if (d.status === 'stopped') { stoppedAt = Date.now(); tAfter = setTimeout(finish, 900); }
    }));
    tNoSpeech = setTimeout(finish, 9000);
    this.ensurePermission().then((ok) => {
      if (!ok || finished) return finish();
      NativeSR.start({ language: lang, maxResults: 1, partialResults: true, popup: false }).catch(() => finish());
    });
    return { promise, stop: finish };
  },

  _web(lang, onPartial) {
    const rec = new WebSR();
    rec.lang = lang; rec.interimResults = true; rec.continuous = false; rec.maxAlternatives = 1;
    let finalText = '', interim = '', resolveFn;
    const promise = new Promise((r) => (resolveFn = r));
    rec.onresult = (e) => {
      interim = '';
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const r = e.results[i];
        if (r.isFinal) finalText += r[0].transcript; else interim += r[0].transcript;
      }
      onPartial && onPartial((finalText + ' ' + interim).trim());
    };
    rec.onerror = (e) => { if (e.error === 'not-allowed') toast('Izin mikrofon ditolak'); };
    rec.onend = () => resolveFn((finalText || interim).trim());
    try { rec.start(); } catch { resolveFn(''); }
    return { promise, stop: () => { try { rec.stop(); } catch {} } };
  },
};

/* ============================== Mic + VAD (mode Universal AI) ============================== */
const Mic = {
  ctx: null,

  /**
   * Merekam satu ucapan dengan deteksi suara otomatis (VAD).
   * Kembali { promise → {base64, ms} | null, stop(), hasSpeech() }.
   */
  listen({ onLevel, silenceMs = 1000, maxMs = 30000, noSpeechMs = 12000 }) {
    let resolveFn, stream, proc, src, done = false, speech = false;
    const promise = new Promise((r) => (resolveFn = r));
    const chunks = [], pre = [];
    let voicedRun = 0, silentMs = 0, totalMs = 0, floor = null, rate = 48000;

    const finish = (keep = true) => {
      if (done) return; done = true;
      try { proc && proc.disconnect(); src && src.disconnect(); } catch {}
      stream && stream.getTracks().forEach((t) => t.stop());
      onLevel && onLevel(0);
      if (!keep || !speech || !chunks.length) return resolveFn(null);
      const pcm = mergeFloat(chunks);
      const ms = (pcm.length / rate) * 1000;
      if (ms < 350) return resolveFn(null);
      resolveFn({ base64: encodeWavBase64(pcm, rate, 16000), ms });
    };

    (async () => {
      try {
        stream = await navigator.mediaDevices.getUserMedia({
          audio: { echoCancellation: true, noiseSuppression: true, autoGainControl: true, channelCount: 1 },
        });
      } catch (e) {
        toast('Tidak bisa mengakses mikrofon');
        return finish(false);
      }
      if (done) { stream.getTracks().forEach((t) => t.stop()); return; }
      this.ctx = this.ctx || new (window.AudioContext || window.webkitAudioContext)();
      if (this.ctx.state === 'suspended') await this.ctx.resume();
      rate = this.ctx.sampleRate;
      src = this.ctx.createMediaStreamSource(stream);
      proc = this.ctx.createScriptProcessor(2048, 1, 1);
      const frameMs = (2048 / rate) * 1000;
      proc.onaudioprocess = (e) => {
        if (done) return;
        const d = new Float32Array(e.inputBuffer.getChannelData(0));
        let sum = 0; for (let i = 0; i < d.length; i++) sum += d[i] * d[i];
        const rms = Math.sqrt(sum / d.length);
        totalMs += frameMs;
        floor = floor === null ? rms : Math.min(rms, floor * 0.995 + rms * 0.005 + 1e-6, floor * 1.02);
        const thr = Math.max(0.012, floor * 3);
        const voiced = rms > thr;
        onLevel && onLevel(Math.min(1, rms * 10));
        if (!speech) {
          pre.push(d); if (pre.length > 6) pre.shift();
          voicedRun = voiced ? voicedRun + 1 : 0;
          if (voicedRun >= 2) { speech = true; chunks.push(...pre); }
          else if (totalMs > noSpeechMs) finish(false);
          return;
        }
        chunks.push(d);
        silentMs = voiced ? 0 : silentMs + frameMs;
        if (silentMs >= silenceMs || totalMs > maxMs) finish(true);
      };
      src.connect(proc); proc.connect(this.ctx.destination);
    })();

    return { promise, stop: (keep = true) => finish(keep), hasSpeech: () => speech };
  },
};

function mergeFloat(chunks) {
  const len = chunks.reduce((a, c) => a + c.length, 0);
  const out = new Float32Array(len); let o = 0;
  for (const c of chunks) { out.set(c, o); o += c.length; }
  return out;
}

function encodeWavBase64(pcm, inRate, outRate) {
  // turunkan sample-rate (rata-rata) → 16 kHz mono 16-bit, ukuran kecil & cepat diunggah
  const ratio = inRate / outRate;
  const n = Math.floor(pcm.length / ratio);
  const buf = new ArrayBuffer(44 + n * 2);
  const v = new DataView(buf);
  const w = (o, s) => { for (let i = 0; i < s.length; i++) v.setUint8(o + i, s.charCodeAt(i)); };
  w(0, 'RIFF'); v.setUint32(4, 36 + n * 2, true); w(8, 'WAVE'); w(12, 'fmt ');
  v.setUint32(16, 16, true); v.setUint16(20, 1, true); v.setUint16(22, 1, true);
  v.setUint32(24, outRate, true); v.setUint32(28, outRate * 2, true); v.setUint16(32, 2, true); v.setUint16(34, 16, true);
  w(36, 'data'); v.setUint32(40, n * 2, true);
  let peak = 0.0001; for (let i = 0; i < pcm.length; i++) peak = Math.max(peak, Math.abs(pcm[i]));
  const gain = Math.min(4, 0.9 / peak); // normalisasi volume agar AI mendengar jelas
  for (let i = 0; i < n; i++) {
    const s0 = Math.floor(i * ratio), s1 = Math.min(pcm.length, Math.floor((i + 1) * ratio));
    let s = 0; for (let j = s0; j < s1; j++) s += pcm[j];
    const x = Math.max(-1, Math.min(1, (s / Math.max(1, s1 - s0)) * gain));
    v.setInt16(44 + i * 2, x < 0 ? x * 0x8000 : x * 0x7fff, true);
  }
  const bytes = new Uint8Array(buf);
  let bin = '';
  for (let i = 0; i < bytes.length; i += 0x8000) bin += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
  return btoa(bin);
}

/* ============================== Gemini (otak AI) ============================== */
const Gemini = {
  noThinking: false, noTools: false,

  hasKey() { return !!S.apiKey.trim(); },

  /**
   * Streaming jawaban. onText(delta) dipanggil tiap potongan teks.
   * Kembali { text, sources[] }.
   */
  async stream({ contents, system, onText, signal, useSearch = S.search, temperature = 0.8, maxTokens = 1024 }) {
    if (!this.hasKey()) throw new Error('NO_KEY');
    const model = (S.model || DEFAULTS.model).trim();
    const body = { contents, generationConfig: { temperature, maxOutputTokens: maxTokens } };
    if (system) body.systemInstruction = { parts: [{ text: system }] };
    if (useSearch && !this.noTools) body.tools = [{ google_search: {} }];
    if (!this.noThinking && !/pro/i.test(model)) body.generationConfig.thinkingConfig = { thinkingBudget: 0 };

    const url = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:streamGenerateContent?alt=sse`;
    const res = await fetch(url, {
      method: 'POST', signal, body: JSON.stringify(body),
      headers: { 'Content-Type': 'application/json', 'x-goog-api-key': S.apiKey.trim() },
    });
    if (!res.ok) {
      let msg = `HTTP ${res.status}`;
      try { const j = await res.json(); msg = (j.error && j.error.message) || msg; } catch {}
      // fallback otomatis untuk model yang tidak mendukung opsi tertentu
      if (res.status === 400 && body.generationConfig.thinkingConfig && /think/i.test(msg)) {
        this.noThinking = true; return this.stream(arguments[0]);
      }
      if (res.status === 400 && body.tools && /tool|search|ground/i.test(msg)) {
        this.noTools = true; return this.stream(arguments[0]);
      }
      if (res.status === 400 && /api key/i.test(msg)) throw new Error('API key tidak valid. Cek di Setelan.');
      if (res.status === 429) throw new Error('Kuota API habis / terlalu sering. Tunggu sebentar ya.');
      if (res.status === 404) throw new Error(`Model "${model}" tidak ditemukan. Ganti model di Setelan.`);
      throw new Error(msg);
    }
    const reader = res.body.getReader();
    const dec = new TextDecoder();
    let buf = '', full = '';
    const sources = new Map();
    for (;;) {
      const { value, done } = await reader.read();
      if (done) break;
      buf += dec.decode(value, { stream: true });
      let i;
      while ((i = buf.indexOf('\n')) >= 0) {
        const line = buf.slice(0, i).trim(); buf = buf.slice(i + 1);
        if (!line.startsWith('data:')) continue;
        let j; try { j = JSON.parse(line.slice(5)); } catch { continue; }
        const c = j.candidates && j.candidates[0];
        if (!c) { if (j.promptFeedback && j.promptFeedback.blockReason) throw new Error('Pesan diblokir oleh filter keamanan.'); continue; }
        const parts = (c.content && c.content.parts) || [];
        for (const p of parts) if (p.text && !p.thought) { full += p.text; onText && onText(p.text); }
        const g = c.groundingMetadata;
        if (g && g.groundingChunks) g.groundingChunks.forEach((ch) => ch.web && sources.set(ch.web.uri, ch.web.title || ch.web.uri));
      }
    }
    return { text: full, sources: [...sources].map(([uri, title]) => ({ uri, title })) };
  },

  async once(opts) {
    const r = await this.stream(Object.assign({ useSearch: false, temperature: 0.2 }, opts));
    return r.text.trim();
  },
};

/* ============================== Terjemahan ============================== */
async function translate(text, tl, sl = 'auto') {
  text = text.trim();
  if (!text) return { text: '', detected: sl };
  try {
    const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=${sl}&tl=${tl}&dt=t&q=${encodeURIComponent(text)}`;
    const r = await fetch(url);
    if (!r.ok) throw new Error(r.status);
    const j = await r.json();
    return { text: j[0].map((x) => x[0]).join(''), detected: j[2] || sl };
  } catch (e) {
    if (!Gemini.hasKey()) throw new Error('Terjemahan gagal. Cek koneksi internet.');
    const out = await Gemini.once({
      contents: [{ role: 'user', parts: [{ text }] }],
      system: `Translate the user's text into language code "${tl}". Output only the translation, no notes.`,
    });
    return { text: out, detected: sl };
  }
}

/* ============================== Status UI ============================== */
const STATUS_TEXT = { idle: 'Siap', listening: 'Mendengarkan…', thinking: 'Berpikir…', speaking: 'Berbicara…' };
function setStatus(state, text) {
  $('#status-dot').className = 'dot ' + (state === 'idle' ? '' : state);
  $('#status-text').textContent = text || STATUS_TEXT[state] || state;
}

function bubble(container, cls, html) {
  const el = document.createElement('div');
  el.className = 'bubble ' + cls;
  el.innerHTML = html;
  container.appendChild(el);
  return el;
}

function scrollDown(el) { requestAnimationFrame(() => { el.scrollTop = el.scrollHeight; }); }

/* ============================== NGOBROL (Voice AI) ============================== */
const Talk = {
  active: false, state: 'idle', history: store('talkHistory') || [], lastLang: store('lastLang') || null,
  abort: null, capture: null, misses: 0, runId: 0,

  get msgs() { return $('#talk-msgs'); },

  systemPrompt() {
    const styles = {
      friend: 'a warm, cheerful, casual best friend. React naturally (e.g. "wah", "oh iya?", laughs), show empathy, and sometimes ask a short follow-up question to keep the chat flowing',
      tutor: 'a patient, encouraging language tutor. Gently correct mistakes, give a better phrasing, and keep the user practicing with short questions',
      pro: 'a professional, concise and precise assistant. Get straight to the point',
    };
    const hint = S.talkLang !== 'auto' ? `The user said they will speak ${langByAny(S.talkLang).name} (${S.talkLang}), but still follow the language they actually use.` : '';
    return [
      `You are "${S.name || 'Nara'}", a voice-based AI companion inside the LiveVoice app. Personality: ${styles[S.style] || styles.friend}.`,
      'LANGUAGE: Automatically detect the language the user speaks or writes (any language, including regional ones like Javanese, Sundanese, Balinese, or mixed slang) and ALWAYS reply in that same language, naturally, like a native speaker. If they ask you to switch language or practice another language, do it. ' + hint,
      'OUTPUT FORMAT (mandatory, every reply): the very first line must be [[LANG|TRANSCRIPT]] where LANG is the BCP-47 code of the language of YOUR reply (e.g. id-ID, en-US, ja-JP, jv-ID, su-ID, ar-SA) and TRANSCRIPT is an exact transcript of what the user just said, in its original script. Then a newline, then your reply.',
      'SPOKEN STYLE: your reply is read aloud by text-to-speech, so write plain conversational sentences: no markdown, no bullet lists, no emoji, no URLs, no headings. Keep it short (usually 1-3 sentences, under 60 words) unless the user explicitly asks for detail or a story.',
      'ACCURACY: be factually correct. For current events, prices, schedules, sports, weather, or anything time-sensitive, use Google Search. If you are not sure, say so honestly instead of guessing. Never invent facts.',
      'If the audio is unclear, silent, or just noise, use an empty TRANSCRIPT and briefly ask the user to repeat, in the language you last used.',
      `Current date/time: ${new Date().toLocaleString('id-ID', { dateStyle: 'full', timeStyle: 'short' })}. Device language: ${deviceLang}.`,
    ].join('\n');
  },

  setState(st, hint) {
    this.state = st;
    $('#orb').className = 'orb ' + (st === 'idle' ? '' : st);
    $('#orb-icon').innerHTML = `<use href="#${st === 'speaking' || st === 'thinking' ? 'i-stop' : 'i-mic'}"/>`;
    const hints = {
      idle: 'Ketuk untuk mulai ngobrol',
      listening: 'Bicara sekarang… (ketuk untuk kirim)',
      thinking: 'Sedang berpikir… (ketuk untuk potong)',
      speaking: 'Ketuk untuk memotong & bicara',
    };
    $('#orb-hint').textContent = hint || hints[st];
    if (currentView === 'talk') setStatus(st);
  },

  level(v) { document.documentElement.style.setProperty('--orb-level', v.toFixed(3)); },

  needKey() {
    if (Gemini.hasKey()) return false;
    toast('Masukkan API key Gemini dulu di Setelan', 3000);
    showView('settings');
    $('#set-key').focus();
    return true;
  },

  async onOrb() {
    unlockAudio();
    if (this.needKey()) return;
    if (this.state === 'idle') return this.start();
    if (this.state === 'listening') {
      // kirim sekarang bila sudah ada ucapan, kalau belum → berhenti
      if (this.capture && this.capture.hasSpeech && this.capture.hasSpeech()) this.capture.stop(true);
      else if (this.capture && !this.capture.hasSpeech) this.capture.stop(); // STT HP: selesaikan sekarang
      else this.end();
      return;
    }
    // thinking / speaking → potong (barge-in) lalu langsung dengarkan
    this.interrupt();
    this.active = true;
    this.loop();
  },

  start() { this.active = true; this.misses = 0; this.loop(); },

  interrupt() {
    this.runId++;
    if (this.abort) this.abort.abort();
    TTS.stop();
  },

  end() {
    this.active = false;
    this.interrupt();
    if (this.capture) this.capture.stop(false);
    this.capture = null;
    this.level(0);
    $('#talk-live').textContent = '';
    this.setState('idle');
  },

  useDeviceEngine() {
    if (S.engine === 'device' && DeviceSTT.available) return true;
    return !navigator.mediaDevices || !navigator.mediaDevices.getUserMedia; // fallback
  },

  async loop() {
    const run = ++this.runId;
    while (this.active && run === this.runId) {
      this.setState('listening');
      const input = await this.captureTurn();
      if (!this.active || run !== this.runId) return;
      if (!input) {
        if (++this.misses >= 3) { this.end(); toast('Aku berhenti mendengar. Ketuk bola untuk lanjut ngobrol.'); return; }
        continue;
      }
      this.misses = 0;
      await this.respond(input, run);
      if (run !== this.runId) return;
      if (!S.handsfree) { this.active = false; this.setState('idle'); return; }
    }
  },

  async captureTurn() {
    const live = $('#talk-live');
    live.textContent = '';
    if (this.useDeviceEngine()) {
      const lang = S.talkLang !== 'auto' ? S.talkLang : (this.lastLang || deviceLang);
      $('#talk-engine-label').textContent = 'HP · ' + (langByAny(lang) || { name: lang }).name;
      this.capture = DeviceSTT.listen({ lang, silenceMs: S.silenceMs, onPartial: (t) => { live.textContent = t; } });
      const text = await this.capture.promise;
      this.capture = null;
      return text ? { text } : null;
    }
    $('#talk-engine-label').textContent = 'Universal AI · deteksi otomatis';
    this.capture = Mic.listen({ silenceMs: S.silenceMs, onLevel: (v) => this.level(v) });
    const audio = await this.capture.promise;
    this.capture = null;
    return audio ? { audio } : null;
  },

  async send(text) {
    unlockAudio();
    if (this.needKey() || !text.trim()) return;
    this.interrupt();
    if (this.capture) this.capture.stop(false);
    const run = ++this.runId;
    await this.respond({ text: text.trim() }, run);
    if (this.active && run === this.runId && S.handsfree) this.loop();
    else if (run === this.runId) this.setState('idle');
  },

  async respond(input, run) {
    $('#talk-empty').classList.add('hidden');
    const scroller = $('#talk-scroll');
    const me = bubble(this.msgs, 'me', `<div class="meta">Kamu</div><div class="t">${input.text ? esc(input.text) : '🎤 <i>pesan suara…</i>'}</div>`);
    const ai = bubble(this.msgs, 'ai', `<div class="meta">${esc(S.name || 'Nara')}</div><div class="t"><span class="typing"><i></i><i></i><i></i></span></div>`);
    scrollDown(scroller);
    this.setState('thinking');
    $('#talk-live').textContent = '';

    const userParts = input.audio
      ? [{ inlineData: { mimeType: 'audio/wav', data: input.audio.base64 } }, { text: '(Voice message from the user. Listen carefully, then follow the output format.)' }]
      : [{ text: input.text }];
    const contents = this.history.slice(-20).map((h) => ({ role: h.role, parts: [{ text: h.text }] }));
    contents.push({ role: 'user', parts: userParts });

    let raw = '', header = null, reply = '', lang = this.lastLang || deviceLang, transcript = input.text || '';
    let spoke = false;
    const tEl = ai.querySelector('.t');
    const speaker = new SentenceStream((s) => {
      if (!S.ttsOn || run !== this.runId) return;
      if (!spoke) { spoke = true; this.setState('speaking'); }
      TTS.speak(s, lang);
    });
    const feed = (delta) => {
      if (header === null) {
        raw += delta;
        const t = raw.replace(/^\s+/, '');
        if (t.startsWith('[[')) {
          const end = t.indexOf(']]');
          if (end < 0) { if (t.length < 600) return; header = ''; delta = t; }
          else {
            header = t.slice(2, end);
            const bar = header.indexOf('|');
            const code = (bar >= 0 ? header.slice(0, bar) : header).trim();
            if (/^[a-z]{2,3}(-[A-Za-z]{2,4})?$/i.test(code)) lang = normLang(code);
            if (bar >= 0 && !input.text) transcript = header.slice(bar + 1).trim();
            delta = t.slice(end + 2).replace(/^\s*\n?/, '');
            if (!input.text) me.querySelector('.t').innerHTML = transcript ? esc(transcript) : '🎤 <i>(tidak terdengar jelas)</i>';
          }
        } else if (t.length >= 2) { header = ''; delta = t; }
        else return;
      }
      if (!delta) return;
      reply += delta;
      tEl.textContent = reply;
      speaker.push(delta);
      scrollDown(scroller);
    };

    this.abort = new AbortController();
    try {
      const res = await Gemini.stream({ contents, system: this.systemPrompt(), onText: feed, signal: this.abort.signal });
      if (run !== this.runId) return;
      if (header === null && raw) { // jawaban sangat pendek / header terpotong
        header = ''; reply = raw.replace(/^\s*\[\[[^\]]*\]?\]?\s*/, '').trim(); tEl.textContent = reply; speaker.push(reply);
      }
      speaker.flush();
      reply = reply.trim();
      if (!reply) tEl.textContent = '…';
      this.lastLang = lang; store('lastLang', lang);
      this.addTools(ai, reply, lang, res.sources);
      if (transcript) this.history.push({ role: 'user', text: transcript });
      else this.history.push({ role: 'user', text: '(unclear voice message)' });
      this.history.push({ role: 'model', text: reply });
      this.history = this.history.slice(-40);
      store('talkHistory', this.history);
      if (S.ttsOn && reply) {
        if (!spoke) this.setState('speaking');
        await TTS.idle();
      }
    } catch (e) {
      if (e.name === 'AbortError' || run !== this.runId) { ai.remove(); return; }
      ai.remove();
      bubble(this.msgs, 'err', esc(e.message === 'NO_KEY' ? 'API key belum diisi.' : e.message || 'Terjadi kesalahan'));
      if (!navigator.onLine) toast('Tidak ada internet');
      this.active = false; this.setState('idle');
    } finally {
      this.abort = null;
      scrollDown(scroller);
    }
  },

  addTools(el, text, lang, sources) {
    if (sources && sources.length) {
      const s = document.createElement('div'); s.className = 'sources';
      s.innerHTML = sources.slice(0, 4).map((x) => `<a href="${esc(x.uri)}" target="_blank" rel="noopener">${esc(x.title)}</a>`).join('');
      el.appendChild(s);
    }
    const tools = document.createElement('div'); tools.className = 'tools';
    tools.innerHTML = '<button data-a="speak">🔊 Ulang</button><button data-a="copy">Salin</button><button data-a="tr">Terjemahkan</button>';
    tools.onclick = async (e) => {
      const a = e.target.dataset.a;
      if (a === 'speak') { TTS.stop(); new SentenceStream((s) => TTS.speak(s, lang)).push(text + '\n'); }
      if (a === 'copy') copyText(text);
      if (a === 'tr') {
        const target = (lang || '').startsWith('id') ? 'en' : 'id';
        try { const r = await translate(text, target); const d = document.createElement('div'); d.className = 'tr'; d.textContent = r.text; el.insertBefore(d, tools); e.target.remove(); }
        catch (err) { toast(err.message); }
      }
    };
    el.appendChild(tools);
  },

  renderHistory() {
    this.msgs.innerHTML = '';
    if (!this.history.length) { $('#talk-empty').classList.remove('hidden'); return; }
    $('#talk-empty').classList.add('hidden');
    for (const h of this.history) {
      if (h.role === 'user') bubble(this.msgs, 'me', `<div class="meta">Kamu</div><div class="t">${esc(h.text)}</div>`);
      else { const b = bubble(this.msgs, 'ai', `<div class="meta">${esc(S.name || 'Nara')}</div><div class="t">${esc(h.text)}</div>`); this.addTools(b, h.text, this.lastLang); }
    }
    scrollDown($('#talk-scroll'));
  },

  transcript() { return this.history.map((h) => `${h.role === 'user' ? 'Kamu' : S.name}: ${h.text}`).join('\n\n'); },
  clear() { this.end(); this.history = []; store('talkHistory', []); this.renderHistory(); },
};

/* ============================== TERJEMAH (juru bahasa dua arah) ============================== */
const Tr = {
  busy: false, capture: null, log: [],

  fill() {
    const opts = LANGS.map((l) => `<option value="${l.tr}">${l.flag} ${l.name}</option>`).join('');
    $('#tr-lang-a').innerHTML = opts; $('#tr-lang-b').innerHTML = opts;
    $('#tr-lang-a').value = S.trA; $('#tr-lang-b').value = S.trB;
    this.labels();
  },
  labels() {
    $('#tr-mic-a-label').textContent = langByTr(S.trA).name;
    $('#tr-mic-b-label').textContent = langByTr(S.trB).name;
  },

  async mic(side) {
    unlockAudio();
    const btn = $('#tr-mic-' + side);
    if (this.capture) { const was = this.capture.side; this.capture.stop(); if (was === side) return; await sleep(150); }
    TTS.stop();
    const from = langByTr(side === 'a' ? S.trA : S.trB);
    const to = langByTr(side === 'a' ? S.trB : S.trA);
    $('#tr-empty').classList.add('hidden');
    const box = $('#tr-interim'); box.classList.remove('hidden');
    $('#tr-interim-text').textContent = '…';
    btn.classList.add('active'); setStatus('listening', `Mendengarkan ${from.name}…`);

    let text = '';
    if (DeviceSTT.available) {
      const c = DeviceSTT.listen({ lang: from.code, silenceMs: S.silenceMs, onPartial: (t) => { $('#tr-interim-text').textContent = t; scrollDown($('#tr-scroll')); } });
      this.capture = { side, stop: c.stop };
      text = await c.promise;
    } else if (Gemini.hasKey()) {
      const c = Mic.listen({ silenceMs: S.silenceMs, onLevel: () => {} });
      this.capture = { side, stop: () => c.stop(true) };
      const audio = await c.promise;
      if (audio) {
        setStatus('thinking', 'Mentranskrip…');
        text = await Gemini.once({ contents: [{ role: 'user', parts: [{ inlineData: { mimeType: 'audio/wav', data: audio.base64 } }, { text: `Transcribe this ${from.name} speech exactly. Output only the transcript.` }] }] }).catch(() => '');
      }
    } else toast('Pengenal suara tidak tersedia di browser ini. Isi API key atau pakai Chrome.');
    this.capture = null;
    btn.classList.remove('active'); box.classList.add('hidden');
    if (text) await this.translateAndShow(text, from, to);
    setStatus('idle');
  },

  async translateAndShow(text, from, to, pre) {
    const el = bubble($('#tr-msgs'), from.tr === S.trB ? 'ai' : 'me', `<div class="meta">${from.flag} ${esc(from.name)} → ${to.flag} ${esc(to.name)}</div><div>${esc(text)}</div><div class="tr">…</div>`);
    scrollDown($('#tr-scroll'));
    setStatus('thinking', 'Menerjemahkan…');
    try {
      const out = pre || (await translate(text, to.tr, from.tr)).text;
      el.querySelector('.tr').textContent = out;
      this.log.push(`[${from.name}] ${text}\n[${to.name}] ${out}`);
      scrollDown($('#tr-scroll'));
      if (S.ttsOn) { setStatus('speaking'); new SentenceStream((s) => TTS.speak(s, to.code)).push(out + '\n'); await TTS.idle(); }
    } catch (e) { el.querySelector('.tr').textContent = '⚠ ' + e.message; }
    setStatus('idle');
  },

  /** Teks ketikan: bahasa dideteksi otomatis. Bahasa B → ke A, bahasa lain apa pun → ke B. */
  async typed(text) {
    text = text.trim();
    if (!text) return;
    unlockAudio();
    $('#tr-empty').classList.add('hidden');
    const base = (c) => String(c || '').toLowerCase().split('-')[0];
    try {
      const probe = await translate(text, S.trB);
      const det = probe.detected || 'auto';
      const from = langByAny(det) || { code: det, tr: det, name: det.toUpperCase(), flag: '🌐' };
      if (base(det) === base(S.trB)) return this.translateAndShow(text, langByTr(S.trB), langByTr(S.trA));
      return this.translateAndShow(text, from, langByTr(S.trB), probe.text);
    } catch (e) { toast(e.message); }
  },

  transcript() { return this.log.join('\n\n'); },
  clear() { this.log = []; $('#tr-msgs').innerHTML = ''; $('#tr-empty').classList.remove('hidden'); },
};

/* ============================== PROMPTER ============================== */
const TEMPLATES = {
  id: 'Selamat pagi semuanya. Terima kasih atas kehadiran Bapak dan Ibu sekalian. Hari ini saya akan memaparkan strategi pertumbuhan perusahaan kita untuk tahun depan. Ada tiga hal utama yang akan kita bahas, yaitu inovasi produk, perluasan pasar, dan peningkatan kualitas layanan pelanggan.',
  en: 'Good morning everyone. Imagine a world where language is never a barrier. Our app lets anyone talk to anyone, instantly, in their own language. We already have ten thousand active users, and we are growing twenty percent every month. Today, we are raising our seed round to bring this experience to the world.',
  ja: 'はじめまして。私の名前はアンディです。インドネシアから来ました。日本の文化がとても好きです。どうぞよろしくお願いします。',
  ko: '안녕하세요. 제 이름은 안디입니다. 저는 인도네시아에서 왔습니다. 오늘 발표를 시작하겠습니다. 감사합니다.',
  ar: 'السلام عليكم ورحمة الله وبركاته. أهلا وسهلا بكم جميعا. يسعدني أن أكون معكم اليوم. شكرا لكم.',
};

const Pr = {
  text: store('prText') || TEMPLATES.id, tokens: [], idx: 0, active: false, cap: null, consumed: 0,

  init() {
    $('#pr-lang').innerHTML = LANGS.map((l) => `<option value="${l.tr}">${l.flag} ${l.name}</option>`).join('');
    $('#pr-lang').value = S.prLang;
    this.render();
  },

  get lang() { return langByTr(S.prLang); },

  norm(w) { return w.toLowerCase().normalize('NFKD').replace(/[̀-ͯ]/g, '').replace(/[^\p{L}\p{N}]/gu, ''); },

  split(text) {
    // bahasa tanpa spasi (Jepang/Mandarin/Thai) dipecah per karakter
    if (this.lang.cjk) return [...text].map((ch) => ({ raw: ch, n: this.norm(ch) }));
    return text.split(/(\s+)/).map((w) => ({ raw: w, n: this.norm(w) }));
  },

  render() {
    const view = $('#pr-view');
    view.classList.toggle('rtl', !!this.lang.rtl);
    view.style.fontSize = S.prFont + 'px';
    $('#pr-font').textContent = S.prFont;
    const parts = this.split(this.text);
    this.tokens = [];
    view.innerHTML = parts.map((p) => {
      if (!p.n) return esc(p.raw).replace(/\n/g, '<br>');
      const i = this.tokens.length; this.tokens.push(p.n);
      return `<span class="w" data-i="${i}">${esc(p.raw)}</span>`;
    }).join('');
    this.idx = Math.min(this.idx, this.tokens.length);
    this.paint();
  },

  paint() {
    const ws = $$('#pr-view .w');
    ws.forEach((w, i) => { w.classList.toggle('spoken', i < this.idx); w.classList.toggle('current', i === this.idx); });
    const n = this.tokens.length;
    const pct = n ? Math.round((this.idx / n) * 100) : 0;
    $('#pr-progress').textContent = pct + '%';
    $('#pr-count').textContent = `${this.idx} / ${n}`;
    $('#pr-bar').style.width = pct + '%';
    const cur = ws[this.idx];
    if (cur) cur.scrollIntoView({ block: 'center', behavior: 'smooth' });
    if (n && this.idx >= n && this.active) { this.stop(); toast('🎉 Naskah selesai dibaca!'); }
  },

  similar(a, b) {
    if (a === b) return true;
    if (a.length < 4 || b.length < 4) return false;
    if (a.startsWith(b) || b.startsWith(a)) return true;
    // jarak Levenshtein ≤ 1 untuk toleransi salah dengar
    if (Math.abs(a.length - b.length) > 1) return false;
    let i = 0, j = 0, diff = 0;
    while (i < a.length && j < b.length) {
      if (a[i] === b[j]) { i++; j++; continue; }
      if (++diff > 1) return false;
      if (a.length > b.length) i++; else if (b.length > a.length) j++; else { i++; j++; }
    }
    return diff + (a.length - i) + (b.length - j) <= 1;
  },

  onHeard(text) {
    const heard = this.split(text).map((p) => p.n).filter(Boolean);
    const window = this.lang.cjk ? 12 : 6;
    for (let k = this.consumed; k < heard.length; k++) {
      for (let j = this.idx; j < Math.min(this.tokens.length, this.idx + window); j++) {
        if (this.similar(heard[k], this.tokens[j])) { this.idx = j + 1; break; }
      }
    }
    this.consumed = heard.length;
    this.paint();
  },

  async toggle() {
    unlockAudio();
    if (this.active) return this.stop();
    if (!DeviceSTT.available) return toast('Pengenal suara tidak tersedia di perangkat/browser ini');
    TTS.stop();
    this.active = true;
    $('#pr-mic').classList.add('active'); $('#pr-mic-label').textContent = 'Berhenti';
    setStatus('listening', 'Mengikuti suaramu…');
    while (this.active) {
      this.consumed = 0;
      this.cap = DeviceSTT.listen({ lang: this.lang.code, silenceMs: 2500, onPartial: (t) => this.onHeard(t) });
      const finalText = await this.cap.promise;
      if (finalText) this.onHeard(finalText);
      await sleep(60);
    }
  },

  stop() {
    this.active = false;
    if (this.cap) this.cap.stop();
    $('#pr-mic').classList.remove('active'); $('#pr-mic-label').textContent = 'Mulai Latihan';
    setStatus('idle');
  },

  setText(t) { this.text = t; store('prText', t); this.idx = 0; this.render(); },

  async translateTo() {
    try {
      toast('Menerjemahkan naskah…');
      const r = await translate(this.text, S.prLang);
      this.setText(r.text);
      toast('Naskah diterjemahkan ke ' + this.lang.name);
    } catch (e) { toast(e.message); }
  },

  listen() {
    unlockAudio();
    if (TTS.busy) return TTS.stop();
    new SentenceStream((s) => TTS.speak(s, this.lang.code)).push(this.text + '\n');
  },
};

/* ============================== Navigasi & UI ============================== */
let currentView = 'talk';
function showView(v) {
  currentView = v;
  $$('.view').forEach((el) => el.classList.toggle('active', el.id === 'view-' + v));
  $$('.nav-btn').forEach((b) => b.classList.toggle('active', b.dataset.view === v));
  const titles = { talk: 'LiveVoice AI', translate: 'Penerjemah', prompter: 'Teleprompter', settings: 'Setelan' };
  $('#title').textContent = titles[v];
  if (v !== 'prompter' && Pr.active) Pr.stop();
  if (v === 'talk') Talk.setState(Talk.state);
  else setStatus('idle');
}

let audioUnlocked = false;
function unlockAudio() {
  // browser butuh gestur pengguna sebelum boleh bersuara
  if (audioUnlocked || NativeTTS || !window.speechSynthesis) return;
  audioUnlocked = true;
  try { const u = new SpeechSynthesisUtterance(' '); u.volume = 0; speechSynthesis.speak(u); } catch {}
}

function renderTtsBtn() {
  const b = $('#btn-tts');
  b.classList.toggle('on', S.ttsOn);
  b.innerHTML = `<svg><use href="#${S.ttsOn ? 'i-vol' : 'i-volx'}"/></svg>`;
}

function talkLangLabel() {
  $('#talk-lang-label').textContent = S.talkLang === 'auto' ? 'Bahasa: Otomatis' : `${langByAny(S.talkLang).flag} ${langByAny(S.talkLang).name}`;
  $('#talk-engine-label').textContent = S.engine === 'ai' ? 'Universal AI · deteksi otomatis' : 'Pengenal suara HP';
}

function openLangSheet() {
  const grid = $('#lang-grid');
  const items = [{ code: 'auto', name: 'Otomatis', flag: '✨' }, ...LANGS];
  grid.innerHTML = items.map((l) => `<button data-c="${l.code}" class="${S.talkLang === l.code ? 'sel' : ''}">${l.flag} ${esc(l.name)}</button>`).join('');
  $('#lang-sheet').classList.remove('hidden');
}

function bindSettings() {
  const key = $('#set-key'), model = $('#set-model');
  key.value = S.apiKey; model.value = S.model;
  key.onchange = () => { S.apiKey = key.value.trim(); saveSettings(); };
  model.onchange = () => { S.model = model.value.trim() || DEFAULTS.model; Gemini.noThinking = Gemini.noTools = false; saveSettings(); };
  $('#set-key-show').onclick = () => { key.type = key.type === 'password' ? 'text' : 'password'; };
  $('#set-test').onclick = async () => {
    S.apiKey = key.value.trim(); S.model = model.value.trim() || DEFAULTS.model; saveSettings();
    const out = $('#set-test-result'); out.textContent = 'Menguji…';
    const t0 = performance.now();
    try {
      const r = await Gemini.once({ contents: [{ role: 'user', parts: [{ text: 'Say "OK" only.' }] }] });
      out.textContent = `✅ Terhubung (${Math.round(performance.now() - t0)} ms): ${r.slice(0, 40)}`;
    } catch (e) { out.textContent = '❌ ' + (e.message === 'NO_KEY' ? 'API key kosong' : e.message); }
  };
  $$('input[name=engine]').forEach((r) => { r.checked = r.value === S.engine; r.onchange = () => { S.engine = r.value; saveSettings(); talkLangLabel(); }; });
  const bindCheck = (id, k) => { const el = $(id); el.checked = S[k]; el.onchange = () => { S[k] = el.checked; saveSettings(); }; };
  bindCheck('#set-handsfree', 'handsfree'); bindCheck('#set-search', 'search');
  const bindRange = (id, k, fmt) => {
    const el = $(id), lab = $(id + '-val');
    el.value = S[k]; lab.textContent = fmt(S[k]);
    el.oninput = () => { S[k] = parseFloat(el.value); lab.textContent = fmt(S[k]); saveSettings(); };
  };
  bindRange('#set-silence', 'silenceMs', (v) => `${(v / 1000).toFixed(1)} detik`);
  bindRange('#set-rate', 'rate', (v) => `${v.toFixed(2)}×`);
  bindRange('#set-pitch', 'pitch', (v) => v.toFixed(2));
  const name = $('#set-name'); name.value = S.name; name.onchange = () => { S.name = name.value.trim() || 'Nara'; saveSettings(); };
  const style = $('#set-style'); style.value = S.style; style.onchange = () => { S.style = style.value; saveSettings(); };
  $('#set-voice-test').onclick = () => {
    unlockAudio(); TTS.stop();
    const l = Talk.lastLang || deviceLang;
    const samples = { id: `Halo! Aku ${S.name}. Seneng banget bisa ngobrol sama kamu.`, en: `Hi! I'm ${S.name}. So happy to chat with you.`, ja: `こんにちは、${S.name}です。よろしくね。` };
    TTS.speak(samples[l.split('-')[0]] || samples.en, samples[l.split('-')[0]] ? l : 'en-US');
  };
  $('#platform-info').textContent = isNative ? 'Mode aplikasi Android (suara native).' : 'Mode web.';
}

function bindUI() {
  $$('.nav-btn').forEach((b) => (b.onclick = () => showView(b.dataset.view)));

  // header
  $('#btn-tts').onclick = () => { S.ttsOn = !S.ttsOn; saveSettings(); renderTtsBtn(); if (!S.ttsOn) TTS.stop(); toast(S.ttsOn ? 'Suara jawaban aktif' : 'Suara jawaban mati'); };
  $('#btn-copy').onclick = () => {
    const t = currentView === 'translate' ? Tr.transcript() : currentView === 'prompter' ? Pr.text : Talk.transcript();
    if (t) copyText(t); else toast('Belum ada yang bisa disalin');
  };
  $('#btn-clear').onclick = () => {
    if (!confirm('Hapus riwayat di tab ini?')) return;
    if (currentView === 'translate') Tr.clear(); else if (currentView === 'prompter') { Pr.idx = 0; Pr.paint(); } else Talk.clear();
  };

  // talk
  $('#orb').onclick = () => Talk.onOrb();
  $('#talk-stop').onclick = () => Talk.end();
  $('#talk-keyboard').onclick = () => { const f = $('#talk-form'); f.classList.toggle('hidden'); if (!f.classList.contains('hidden')) $('#talk-input').focus(); };
  $('#talk-form').onsubmit = (e) => { e.preventDefault(); const i = $('#talk-input'); const v = i.value; i.value = ''; Talk.send(v); };
  $$('.suggest button').forEach((b) => (b.onclick = () => Talk.send(b.dataset.q)));
  $('#talk-lang-chip').onclick = openLangSheet;
  $('#lang-sheet').onclick = (e) => {
    const c = e.target.closest('button[data-c]');
    if (c) { S.talkLang = c.dataset.c; saveSettings(); talkLangLabel(); }
    if (c || e.target.id === 'lang-sheet') $('#lang-sheet').classList.add('hidden');
  };

  // translate
  Tr.fill();
  $('#tr-lang-a').onchange = (e) => { S.trA = e.target.value; saveSettings(); Tr.labels(); };
  $('#tr-lang-b').onchange = (e) => { S.trB = e.target.value; saveSettings(); Tr.labels(); };
  $('#tr-swap').onclick = () => { [S.trA, S.trB] = [S.trB, S.trA]; saveSettings(); Tr.fill(); };
  $('#tr-mic-a').onclick = () => Tr.mic('a');
  $('#tr-mic-b').onclick = () => Tr.mic('b');
  $('#tr-form').onsubmit = (e) => { e.preventDefault(); const i = $('#tr-input'); const v = i.value; i.value = ''; Tr.typed(v); };

  // prompter
  Pr.init();
  $('#pr-mic').onclick = () => Pr.toggle();
  $('#pr-reset').onclick = () => { Pr.idx = 0; Pr.paint(); $('#pr-view').scrollTop = 0; };
  $('#pr-font-up').onclick = () => { S.prFont = Math.min(64, S.prFont + 2); saveSettings(); Pr.render(); };
  $('#pr-font-down').onclick = () => { S.prFont = Math.max(14, S.prFont - 2); saveSettings(); Pr.render(); };
  $('#pr-lang').onchange = (e) => { S.prLang = e.target.value; saveSettings(); Pr.render(); };
  $('#pr-template').onchange = (e) => {
    const k = e.target.value; if (!k) return;
    S.prLang = k; saveSettings(); $('#pr-lang').value = k; Pr.setText(TEMPLATES[k]); e.target.value = '';
  };
  $('#pr-view').onclick = (e) => { const w = e.target.closest('.w'); if (w) { Pr.idx = +w.dataset.i; Pr.paint(); } };
  $('#pr-translate').onclick = () => Pr.translateTo();
  $('#pr-listen').onclick = () => Pr.listen();
  const toggleEditor = (open) => {
    $('#pr-editor').classList.toggle('hidden', !open);
    $('#pr-view').classList.toggle('hidden', open);
    $('#pr-edit-label').textContent = open ? 'Tutup Editor' : 'Edit Naskah';
    if (open) $('#pr-text').value = Pr.text;
  };
  $('#pr-edit').onclick = () => toggleEditor($('#pr-editor').classList.contains('hidden'));
  $('#pr-save').onclick = () => { Pr.setText($('#pr-text').value.trim() || TEMPLATES.id); toggleEditor(false); toast('Naskah disimpan'); };
  $('#pr-upload').onclick = () => $('#pr-file').click();
  $('#pr-file').onchange = async (e) => {
    const f = e.target.files[0]; if (!f) return;
    let t = await f.text();
    if (/\.srt$/i.test(f.name)) t = t.replace(/^\d+\s*$/gm, '').replace(/^[\d:,]+ --> [\d:,]+\s*$/gm, '').replace(/\n{2,}/g, '\n');
    $('#pr-text').value = t.trim(); e.target.value = '';
  };
  $('#pr-from-chat').onclick = () => {
    const last = [...Talk.history].reverse().find((h) => h.role === 'model');
    if (last) $('#pr-text').value = last.text; else toast('Belum ada jawaban AI di tab Ngobrol');
  };

  bindSettings();
  renderTtsBtn();
  talkLangLabel();
}

/* ============================== Start ============================== */
(async function init() {
  bindUI();
  Talk.renderHistory();
  Talk.setState('idle');
  await TTS.init();
  if (!isNative && 'serviceWorker' in navigator && location.protocol.startsWith('http')) {
    navigator.serviceWorker.register('sw.js').catch(() => {});
  }
  if (!Gemini.hasKey()) setTimeout(() => toast('Tip: isi API key Gemini gratis di tab Setelan untuk mulai ngobrol', 4000), 800);
})();
