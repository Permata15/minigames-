import Anthropic from "@anthropic-ai/sdk";
import { Capacitor, registerPlugin } from "@capacitor/core";
import { SpeechRecognition } from "@capacitor-community/speech-recognition";
import { TextToSpeech, QueueStrategy } from "@capacitor-community/text-to-speech";
import { CapgoLLM } from "@capgo/capacitor-llm";
// Pengunduh file besar buatan sendiri (lihat android/.../ModelDownloaderPlugin.java)
const ModelDownloader = registerPlugin("ModelDownloader");

/* ================= konfigurasi ================= */
// KILAT: model tercepat, tanpa berpikir dulu → jawaban langsung keluar.
// CERDAS: model terpintar, berpikir sebelum menjawab.
const MODELS = { quick: "claude-haiku-5-5", smart: "claude-opus-5-5" };
// Gemini (gratis): model Flash, dengan cadangan jika nama model tidak tersedia.
const GEMINI_MODELS = ["gemini-flash-latest", "gemini-2.5-flash", "gemini-flash-lite-latest"];
// Offline: Gemini Nano bawaan HP (jika ada), atau Gemma 4 dari Google yang diunduh sekali.
const OFFLINE_MODEL = {
  url: "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true",
  file: "gemma-4-E2B-it.litertlm",
};

const LANGS = {
  id: ["Bahasa Indonesia", "id-ID"], en: ["English", "en-US"], ms: ["Bahasa Melayu", "ms-MY"],
  ja: ["Japanese (日本語)", "ja-JP"], ko: ["Korean (한국어)", "ko-KR"], zh: ["Simplified Chinese (中文)", "zh-CN"],
  ar: ["Arabic (العربية)", "ar-SA"], hi: ["Hindi (हिन्दी)", "hi-IN"], th: ["Thai (ไทย)", "th-TH"],
  vi: ["Vietnamese (Tiếng Việt)", "vi-VN"], es: ["Spanish (Español)", "es-ES"], fr: ["French (Français)", "fr-FR"],
  de: ["German (Deutsch)", "de-DE"], pt: ["Portuguese (Português)", "pt-BR"], ru: ["Russian (Русский)", "ru-RU"],
  tr: ["Turkish (Türkçe)", "tr-TR"],
};

const SUGGESTIONS = [
  "Perkenalkan dirimu, CORE-9!",
  "Tulis percakapan di kafe dalam bahasa Inggris, Jepang, dan Korea beserta artinya",
  "Ajari saya 5 kalimat sapaan dalam bahasa Arab",
  "Jelaskan cara kerja AI dengan bahasa sederhana",
];

/* ================= state ================= */
const $ = (id) => document.getElementById(id);
const log = $("log"), input = $("input"), fire = $("fire"), form = $("form");
const app = $("app"), bay = $("bay"), mic = $("mic"), langSel = $("lang");

const store = {
  get(k, d) { try { const v = localStorage.getItem(k); return v === null ? d : v; } catch { return d; } },
  set(k, v) { try { localStorage.setItem(k, v); } catch {} },
};

let turns = [];
let stream = null;            // permintaan AI yang sedang berjalan: { abort() }
let tier = store.get("core9-mode", "quick") === "smart" ? "smart" : "quick";
let lang = store.get("core9-lang", "auto");
if (lang !== "auto" && !LANGS[lang]) lang = "auto";
let voiceOn = store.get("core9-voice", "on") === "on";
let handsFree = store.get("core9-hands", "off") === "on";
let apiKey = store.get("core9-key", "");        // Claude
let gKey = store.get("core9-gkey", "");          // Gemini
let brain = store.get("core9-brain", "offline");
let client = apiKey ? makeClient(apiKey) : null;
let geminiModel = 0;                             // indeks di GEMINI_MODELS yang terbukti jalan
let geminiNoThinkCfg = false;
const hasKey = () => (brain === "offline" ? offline.state === "ready" : brain === "gemini" ? !!gKey : !!client);

function makeClient(key) {
  // Aplikasi berjalan di HP Anda sendiri, jadi API key dikirim langsung dari HP ke Claude.
  return new Anthropic({ apiKey: key, dangerouslyAllowBrowser: true, maxRetries: 2 });
}

function systemPrompt() {
  const langRule = lang === "auto"
    ? "Jawab dalam bahasa yang sama dengan pesan terakhir pengguna (bawaan: Bahasa Indonesia)."
    : `Selalu jawab dalam ${LANGS[lang][0]}, apa pun bahasa yang dipakai pengguna, kecuali pengguna secara jelas meminta bahasa lain.`;
  return `Kamu adalah CORE-9, sebuah AI berwujud robot mecha yang ramah, cerdas, dan setia membantu, berjalan sebagai aplikasi di HP Android pengguna.
Gaya bicara: hangat dan percaya diri, sesekali memakai istilah robot/mecha (misalnya "sistem", "misi", "energi", "memindai"), tapi jangan berlebihan.
${langRule}
Kamu fasih semua bahasa dunia dan bahasa daerah. Jika diminta menulis percakapan/dialog dalam beberapa bahasa, tulis tiap baris dialog lengkap dengan nama pembicara, dan sertakan terjemahan Bahasa Indonesia di bawahnya jika berguna.
Langsung ke inti jawaban sejak kalimat pertama, tanpa basa-basi pembuka.
Jawaban ringkas dan jelas. Boleh pakai **tebal**, \`kode\`, blok kode dengan \`\`\`, dan daftar berawalan "- ". Jangan pakai heading markdown (#).
${voiceOn ? "Jawabanmu juga dibacakan dengan suara, jadi utamakan kalimat lisan yang mengalir; hindari tabel dan simbol berlebihan." : ""}`;
}

/* ================= tampilan ================= */
function esc(s) { return s.replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c])); }
function format(text) {
  return text.split(/```/).map((chunk, i) => {
    if (i % 2 === 1) {
      const code = chunk.replace(/^[a-zA-Z0-9+#-]*\n/, "");
      return `<pre><code>${esc(code.replace(/\n$/, ""))}</code></pre>`;
    }
    return esc(chunk)
      .replace(/`([^`\n]+)`/g, "<code>$1</code>")
      .replace(/\*\*([^*\n]+)\*\*/g, "<strong>$1</strong>")
      .replace(/^- /gm, "▸ ");
  }).join("");
}
function bubble(kind, text) {
  const wrap = document.createElement("div");
  wrap.className = `msg ${kind}`;
  const who = document.createElement("div");
  who.className = "who";
  who.textContent = kind === "you" ? "ANDA" : kind === "bot" ? "CORE-9" : "SISTEM";
  const body = document.createElement("div");
  body.className = "body";
  body.dir = "auto";
  if (text !== undefined) body.innerHTML = format(text);
  wrap.append(who, body);
  log.append(wrap);
  log.scrollTop = log.scrollHeight;
  return body;
}
function setStatus(text, kind) {
  $("statusText").textContent = text;
  $("hudStatus").textContent = text;
  $("status").className = "status" + (kind ? " " + kind : "");
  app.classList.toggle("busy", kind === "work" || kind === "talk");
}
function idleStatus() {
  if (listening) setStatus("MENDENGAR", "listen");
  else if (stream) setStatus("MENJAWAB", "work");
  else if (speaking()) setStatus("BERBICARA", "talk");
  else setStatus("SIAGA");
}
function pressed(id, on) { $(id).setAttribute("aria-pressed", String(on)); }
function setMode(t) {
  tier = t;
  pressed("modeQuick", t === "quick");
  pressed("modeSmart", t === "smart");
  $("hudMode").textContent = t === "quick" ? "KILAT" : "CERDAS";
  store.set("core9-mode", t);
}
function setVoice(on) {
  voiceOn = on;
  pressed("voiceBtn", on);
  $("voiceBtn").textContent = on ? "🔊 SUARA" : "🔇 SUARA";
  if (!on) stopSpeaking();
  store.set("core9-voice", on ? "on" : "off");
}
function setHands(on) {
  handsFree = on;
  pressed("handsBtn", on);
  store.set("core9-hands", on ? "on" : "off");
}
function updateCount() { $("hudCount").textContent = String(turns.filter((t) => t.role === "user").length).padStart(3, "0"); }

function greet() {
  log.innerHTML = "";
  bubble("bot", "Sistem aktif. **CORE-9** siap bertugas! 🤖\nKetik pesan, atau tekan 🎤 lalu bicara: saya langsung menjawab begitu Anda berhenti bicara. Nyalakan **🔁 OBROLAN** untuk ngobrol bolak-balik tanpa menyentuh layar.");
  const chips = document.createElement("div");
  chips.className = "chips";
  chips.id = "chips";
  for (const s of SUGGESTIONS) {
    const b = document.createElement("button");
    b.type = "button";
    b.textContent = s;
    b.onclick = () => send(s);
    chips.append(b);
  }
  log.append(chips);
}
function reforge() {
  bay.classList.add("reforge");
  void bay.offsetWidth;
  bay.classList.remove("reforge");
}

/* ================= suara keluar (dibacakan sambil menulis) ================= */
let speechGen = 0;       // naik setiap kali suara dihentikan; promise lama diabaikan
let pendingSpeech = 0;
let speech = null;       // { said, chunks, gen }

function speaking() { return pendingSpeech > 0; }

function guessLang(text) {
  if (lang !== "auto") return LANGS[lang][1];
  if (/[぀-ヿ]/.test(text)) return "ja-JP";
  if (/[가-힯]/.test(text)) return "ko-KR";
  if (/[一-鿿]/.test(text)) return "zh-CN";
  if (/[؀-ۿ]/.test(text)) return "ar-SA";
  if (/[ऀ-ॿ]/.test(text)) return "hi-IN";
  if (/[฀-๿]/.test(text)) return "th-TH";
  if (/[Ѐ-ӿ]/.test(text)) return "ru-RU";
  const w = text.toLowerCase().match(/[a-z]+/g) || [];
  const en = w.filter((x) => ["the", "and", "is", "you", "to", "are", "of", "it", "this", "what", "i"].includes(x)).length;
  const id = w.filter((x) => ["yang", "dan", "di", "ini", "itu", "saya", "anda", "untuk", "dengan", "tidak", "ada", "bisa"].includes(x)).length;
  return en > id ? "en-US" : "id-ID";
}
function spokenText(s) {
  return s
    .replace(/```[\s\S]*?```/g, " ")
    .replace(/```/g, " ")
    .replace(/`([^`]+)`/g, "$1")
    .replace(/\*\*/g, "")
    .replace(/^\s*[-▸]\s+/gm, "")
    .replace(/[#*_>|]/g, " ")
    .replace(/\p{Extended_Pictographic}/gu, "")
    .replace(/\s+/g, " ")
    .trim();
}
function speakChunk(raw) {
  const text = spokenText(raw);
  if (!/[\p{L}\p{N}]/u.test(text)) return;
  const gen = speechGen;
  pendingSpeech++;
  setStatus(stream ? "MENJAWAB" : "BERBICARA", stream ? "work" : "talk");
  TextToSpeech.speak({ text, lang: guessLang(text), rate: 1.05, pitch: 0.85, queueStrategy: QueueStrategy.Add })
    .catch(() => {})
    .finally(() => {
      if (gen !== speechGen) return;
      pendingSpeech = Math.max(0, pendingSpeech - 1);
      if (pendingSpeech === 0 && !stream) afterReply();
    });
}
// Kirim kalimat yang sudah lengkap ke suara. Potongan pertama pendek supaya robot langsung bicara.
function feedSpeech(full, final) {
  if (!speech || speech.gen !== speechGen) return;
  let rest = full.slice(speech.said);
  if (!final) {
    const fences = (full.slice(0, speech.said).match(/```/g) || []).length;
    const min = speech.chunks === 0 ? 25 : 140;
    const re = /[.!?…]["')\]]?\s|[。！？]|\n/g;
    let cut = -1, m;
    while ((m = re.exec(rest))) {
      const end = m.index + m[0].length;
      if ((fences + (rest.slice(0, end).match(/```/g) || []).length) % 2 === 1) continue;
      cut = end;
      if (end >= min) break;
    }
    if (cut < min) return;
    rest = rest.slice(0, cut);
  }
  if (!rest.trim()) return;
  speech.said += rest.length;
  speech.chunks++;
  speakChunk(rest);
}
function stopSpeaking() {
  speechGen++;
  speech = null;
  pendingSpeech = 0;
  TextToSpeech.stop().catch(() => {});
}

/* ================= kirim ke Claude ================= */
function trimTurns() {
  const size = () => turns.reduce((n, t) => n + t.content.length, 0);
  while (size() > 120000 && turns.length > 2) turns.splice(0, 2);
  while (turns.length && turns[0].role !== "user") turns.shift();
}

// ---------- Claude ----------
async function runClaude(onDelta, signal) {
  const common = {
    max_tokens: 16000,
    system: systemPrompt(),
    messages: turns.map((t) => ({ role: t.role, content: t.content })),
  };
  const st = tier === "quick"
    ? client.messages.stream({
        ...common,
        model: MODELS.quick,
        thinking: { type: "disabled" },          // tanpa jeda berpikir
        output_config: { effort: "low" },
      }, { signal })
    : client.beta.messages.stream({
        ...common,
        model: MODELS.smart,
        output_config: { effort: "medium" },
        betas: ["server-side-fallback-2026-07-01"],
        fallbacks: "default",                      // jika ditolak filter keamanan, model cadangan melanjutkan
      }, { signal });
  for await (const ev of st) {
    if (ev.type === "content_block_delta" && ev.delta.type === "text_delta") onDelta(ev.delta.text);
  }
  const msg = await st.finalMessage();
  return msg.stop_reason;
}

function claudeError(e) {
  if (e instanceof Anthropic.AuthenticationError) return { msg: "API key Claude salah atau tidak aktif. Periksa di ⚙ Pengaturan.", settings: true };
  if (e instanceof Anthropic.PermissionDeniedError) return { msg: "API key Claude ini tidak punya izin. Periksa akun di console.anthropic.com.", settings: true };
  if (e instanceof Anthropic.RateLimitError) return { msg: "Terlalu banyak permintaan ke Claude. Tunggu sebentar lalu coba lagi." };
  if (e instanceof Anthropic.BadRequestError) {
    const low = /credit|balance|billing/i.test(e.message);
    return low
      ? { msg: "Saldo Claude API Anda $0. Isi saldo di console.anthropic.com → Billing, atau ganti ke Gemini (gratis) di ⚙ Pengaturan.", settings: true }
      : { msg: "Permintaan ditolak: " + e.message };
  }
  if (e instanceof Anthropic.InternalServerError) return { msg: "Server Claude sedang sibuk. Coba kirim ulang." };
  if (e instanceof Anthropic.APIConnectionError) return { msg: "Tidak ada koneksi internet atau server tidak terjangkau." };
  return { msg: "Terjadi gangguan: " + (e && e.message ? e.message : "tidak diketahui") };
}

// ---------- Gemini (gratis) ----------
class GeminiError extends Error {
  constructor(msg, opts = {}) { super(msg); this.settings = !!opts.settings; this.retry = opts.retry; }
}
async function runGemini(onDelta, signal) {
  const model = GEMINI_MODELS[geminiModel];
  const body = {
    systemInstruction: { parts: [{ text: systemPrompt() }] },
    contents: turns.map((t) => ({ role: t.role === "assistant" ? "model" : "user", parts: [{ text: t.content }] })),
    generationConfig: { maxOutputTokens: 4096, temperature: 0.8 },
  };
  // KILAT = tanpa berpikir dulu (langsung menjawab); CERDAS = model berpikir sebentar.
  if (tier === "quick" && !geminiNoThinkCfg) body.generationConfig.thinkingConfig = { thinkingBudget: 0 };

  let res;
  try {
    res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:streamGenerateContent?alt=sse`, {
      method: "POST",
      signal,
      headers: { "Content-Type": "application/json", "x-goog-api-key": gKey },
      body: JSON.stringify(body),
    });
  } catch (e) {
    if (signal.aborted) throw e;
    throw new GeminiError("Tidak ada koneksi internet atau server Google tidak terjangkau.");
  }
  if (!res.ok) {
    let msg = "";
    try { msg = (await res.json()).error?.message || ""; } catch {}
    if (res.status === 400 && body.generationConfig.thinkingConfig && /think/i.test(msg)) {
      geminiNoThinkCfg = true;
      throw new GeminiError("", { retry: true });
    }
    if (res.status === 404 && geminiModel < GEMINI_MODELS.length - 1) {
      geminiModel++;
      throw new GeminiError("", { retry: true });
    }
    if (/api key|API_KEY/i.test(msg) || res.status === 401 || res.status === 403)
      throw new GeminiError("API key Gemini salah atau belum aktif. Periksa di ⚙ Pengaturan.", { settings: true });
    if (res.status === 429)
      throw new GeminiError("Kuota gratis Gemini sedang habis (batas per menit atau per hari). Tunggu sekitar 1 menit lalu coba lagi. Jika masih gagal, kuota hari ini habis dan reset besok.");
    if (res.status >= 500) throw new GeminiError("Server Gemini sedang sibuk. Coba kirim ulang.");
    throw new GeminiError("Permintaan ditolak Gemini: " + (msg || "HTTP " + res.status));
  }

  const reader = res.body.getReader();
  const dec = new TextDecoder();
  let buf = "", finish = "";
  for (;;) {
    const { value, done } = await reader.read();
    if (done) break;
    buf += dec.decode(value, { stream: true });
    let i;
    while ((i = buf.indexOf("\n")) >= 0) {
      const line = buf.slice(0, i).trim();
      buf = buf.slice(i + 1);
      if (!line.startsWith("data:")) continue;
      let j;
      try { j = JSON.parse(line.slice(5)); } catch { continue; }
      if (j.promptFeedback?.blockReason) return "refusal";
      const c = j.candidates?.[0];
      if (!c) continue;
      for (const part of c.content?.parts || []) if (part.text && !part.thought) onDelta(part.text);
      if (c.finishReason) finish = c.finishReason;
    }
  }
  if (finish === "MAX_TOKENS") return "max_tokens";
  if (finish === "SAFETY" || finish === "PROHIBITED_CONTENT") return "refusal";
  return "end_turn";
}

// ---------- Offline di HP (tanpa key) ----------
const offline = { state: "idle", kind: null, chatId: null, busy: null, error: "" };
let offlineListeners = false;
const offlineHandlers = { text: null, done: null, fail: null };

function offlineLabel() { return offline.kind === "nano" ? "Gemini Nano (bawaan HP)" : "Gemma 4 (offline)"; }

async function ensureOfflineListeners() {
  if (offlineListeners) return;
  offlineListeners = true;
  await CapgoLLM.addListener("textFromAi", (e) => { if (e.chatId === offline.chatId) offlineHandlers.text?.(e.text || ""); });
  await CapgoLLM.addListener("aiFinished", (e) => { if (e.chatId === offline.chatId) offlineHandlers.done?.(); });
  await CapgoLLM.addListener("generationError", (e) => { if (!e.chatId || e.chatId === offline.chatId) offlineHandlers.fail?.(e.error || "gagal"); });
  await ModelDownloader.addListener("progress", (e) => showDownload(e.bytes, e.total));
  // Bersihkan sisa unduhan versi lama aplikasi (pengunduh lama tidak bisa melanjutkan).
  if (!store.get("core9-legacy-clean", "") && !store.get("core9-offline-path", "")) {
    await ModelDownloader.remove({ legacy: OFFLINE_MODEL.file }).catch(() => {});
    store.set("core9-legacy-clean", "1");
  }
}

function errText(e) {
  if (!e) return "tidak diketahui";
  const parts = [e.message || e.errorMessage, e.code, e.data && (e.data.message || JSON.stringify(e.data))]
    .filter((x) => x && typeof x === "string");
  return [...new Set(parts)].join(" · ") || String(e);
}
// Muat file Gemma dengan beberapa setelan, dari yang paling ringan untuk RAM.
async function loadGemma(path) {
  const tries = [
    { backend: "cpu", maxTokens: 2048 },
    { backend: "gpu", maxTokens: 2048 },
    { backend: "cpu", maxTokens: 1024 },
  ];
  const errors = [];
  for (const t of tries) {
    try {
      await CapgoLLM.setModel({ path, modelType: "litertlm", temperature: 0.7, ...t });
      return;
    } catch (e) {
      errors.push(`${t.backend.toUpperCase()}: ${errText(e)}`);
    }
  }
  const info = await ModelDownloader.info({ filename: OFFLINE_MODEL.file }).catch(() => null);
  const size = info && info.size ? ` · ukuran file ${gbText(info.size)} GB` : "";
  throw new Error(errors.join(" | ") + size);
}

// Coba muat otak offline yang sudah ada. Hasil: "ready" atau "need_download".
async function loadOffline() {
  if (!Capacitor.isNativePlatform()) throw new Error("Mode offline hanya bisa dipakai di aplikasi Android");
  await ensureOfflineListeners();
  offline.state = "loading";
  updateOfflinePanel();
  const path = store.get("core9-offline-path", "");
  if (path) {
    try {
      await loadGemma(path);
      return offlineReady("gemma");
    } catch (e) { offline.loadError = e.message; }
  }
  const info = await ModelDownloader.info({ filename: OFFLINE_MODEL.file }).catch(() => null);
  if (info && info.exists) {
    try {
      await loadGemma(info.path);
      store.set("core9-offline-path", info.path);
      return offlineReady("gemma");
    } catch (e) {
      offline.loadError = e.message;
    }
  }
  try {
    await CapgoLLM.setModel({ path: "Gemini Nano" });
    return offlineReady("nano");
  } catch { /* HP ini tidak punya Gemini Nano */ }
  offline.partial = info ? info.partial : 0;
  offline.freeBytes = info ? info.freeBytes : 0;
  offline.fileReady = !!(info && info.exists);
  if (offline.fileReady && offline.loadError) {
    // File sudah lengkap, tapi gagal dijalankan: tampilkan alasannya.
    offline.state = "error";
    offline.error = offline.loadError;
    updateOfflinePanel();
    return "need_download";
  }
  offline.state = "need_download";
  updateOfflinePanel();
  return "need_download";
}
async function offlineReady(kind) {
  offline.kind = kind;
  offline.state = "ready";
  offline.chatId = (await CapgoLLM.createChat()).id;
  offline.fresh = true;
  updateOfflinePanel();
  applyBrainLabel();
  return "ready";
}
async function downloadOffline() {
  offline.state = "downloading";
  dl.startBytes = -1;
  updateOfflinePanel();
  showDownload(offline.partial || 0, -1);
  try {
    const { path } = await ModelDownloader.download({ url: OFFLINE_MODEL.url, filename: OFFLINE_MODEL.file });
    store.set("core9-offline-path", path);
    offline.state = "loading";
    updateOfflinePanel();
    await loadGemma(path);
    await offlineReady("gemma");
    return true;
  } catch (e) {
    offline.state = "error";
    offline.error = errText(e);
    const info = await ModelDownloader.info({ filename: OFFLINE_MODEL.file }).catch(() => null);
    offline.partial = info ? info.partial : 0;
    offline.fileReady = !!(info && info.exists);
    updateOfflinePanel();
    return false;
  }
}
async function newOfflineChat() {
  if (offline.state !== "ready") return;
  try { offline.chatId = (await CapgoLLM.createChat()).id; offline.fresh = true; } catch {}
}

async function runOffline(onDelta, signal) {
  if (offline.busy) { setStatus("MENYELESAIKAN", "work"); await offline.busy; }
  const last = turns[turns.length - 1].content;
  const langNote = lang === "auto" ? "" : `[Jawab dalam ${LANGS[lang][0]}] `;
  // Model offline tidak punya kolom instruksi sistem: instruksi ikut di pesan pertama percakapan.
  const message = offline.fresh ? `${systemPrompt()}\n\n---\nPesan pengguna: ${langNote}${last}` : langNote + last;
  offline.fresh = false;
  let finish;
  const done = new Promise((resolve, reject) => {
    offlineHandlers.text = (t) => { if (!signal.aborted) onDelta(t); };
    offlineHandlers.done = () => resolve("end_turn");
    offlineHandlers.fail = (err) => reject(new GeminiError("Otak offline gagal menjawab: " + err));
  });
  offline.busy = done.catch(() => {}).finally(() => { offline.busy = null; });
  await CapgoLLM.sendMessage({ chatId: offline.chatId, message });
  // Tombol STOP: tampilan berhenti seketika, model menyelesaikan kalimatnya di latar belakang.
  const stopped = new Promise((resolve) => signal.addEventListener("abort", () => resolve("end_turn"), { once: true }));
  return Promise.race([done, stopped]);
}

async function runAI(onDelta, signal) {
  if (brain === "offline") return runOffline(onDelta, signal);
  if (brain === "claude") return runClaude(onDelta, signal);
  for (let attempt = 0; ; attempt++) {
    try { return await runGemini(onDelta, signal); }
    catch (e) { if (e instanceof GeminiError && e.retry && attempt < 4) continue; throw e; }
  }
}

async function send(text) {
  text = (text || "").trim();
  if (!text || stream) return;
  if (!hasKey()) { openSettings(); return; }
  stopSpeaking();
  $("chips")?.remove();
  bubble("you", text);
  turns.push({ role: "user", content: text });
  updateCount();
  trimTurns();

  const body = bubble("bot");
  body.innerHTML = '<span class="thinking">MEMINDAI</span>';
  const myTurns = turns;
  speech = voiceOn ? { said: 0, chunks: 0, gen: speechGen } : null;
  const mySpeech = speech;

  let full = "";
  fire.textContent = "STOP";
  fire.classList.add("stop");
  setStatus(tier === "quick" ? "MEMPROSES" : "BERPIKIR", "work");

  let ok = false;
  const ctl = new AbortController();
  stream = { abort: () => ctl.abort() };
  try {
    const stop = await runAI((delta) => {
      full += delta;
      body.innerHTML = format(full);
      log.scrollTop = log.scrollHeight;
      if (!speaking()) setStatus("MENJAWAB", "work");
      if (speech === mySpeech) feedSpeech(full, false);
    }, ctl.signal);
    if (stop === "refusal") {
      if (!full) body.parentElement.remove();
      bubble("sys", "Permintaan ini tidak bisa diproses. Coba ubah pertanyaannya.");
    } else if (stop === "max_tokens") {
      bubble("sys", "Jawaban terpotong karena terlalu panjang. Minta lanjutannya jika perlu.");
    }
    ok = true;
  } catch (e) {
    const aborted = ctl.signal.aborted || e instanceof Anthropic.APIUserAbortError;
    if (!aborted) {
      stopSpeaking();
      const { msg, settings } = e instanceof GeminiError ? { msg: e.message, settings: e.settings } : claudeError(e);
      bubble("sys", msg);
      setStatus("GANGGUAN", "err");
      if (settings) openSettings(msg);
    }
  } finally {
    stream = null;
    fire.textContent = "KIRIM";
    fire.classList.remove("stop");
  }

  if (full && turns === myTurns) turns.push({ role: "assistant", content: full });
  else if (!full) {
    body.parentElement?.remove();
    if (turns === myTurns) { turns.pop(); updateCount(); }
  }
  if (speech === mySpeech && full) feedSpeech(full, true);
  if (ok) {
    idleStatus();
    if (!speaking()) afterReply();
  }
}

// Dipanggil setelah jawaban selesai ditulis DAN selesai dibacakan.
function afterReply() {
  idleStatus();
  if (handsFree && micReady && !listening && !stream && $("settings").hidden) startListening();
}

/* ================= suara masuk (ngobrol langsung) ================= */
let micReady = false;
let listening = false;
let heard = "";
let cancelListen = false;

async function initMic() {
  try {
    const { available } = await SpeechRecognition.available();
    if (!available) throw new Error("tidak tersedia");
    micReady = true;
    SpeechRecognition.addListener("partialResults", ({ matches }) => {
      if (matches && matches[0] !== undefined) input.value = heard + matches[0];
    });
    SpeechRecognition.addListener("listeningState", ({ status }) => {
      if (status === "started") { listening = true; mic.classList.add("on"); setStatus("MENDENGAR", "listen"); return; }
      listening = false;
      mic.classList.remove("on");
      const text = input.value.trim();
      if (!cancelListen && text) {
        input.value = ""; heard = "";
        send(text);                      // langsung menjawab begitu Anda berhenti bicara
      } else {
        idleStatus();
      }
      cancelListen = false;
    });
  } catch {
    micReady = false;
    mic.hidden = true;
    $("handsBtn").hidden = true;
    $("hint").textContent = "ENTER = KIRIM";
  }
}

async function startListening() {
  if (!micReady || listening) return;
  stopSpeaking();
  try {
    let perm = await SpeechRecognition.checkPermissions();
    if (perm.speechRecognition !== "granted") perm = await SpeechRecognition.requestPermissions();
    if (perm.speechRecognition !== "granted") {
      bubble("sys", "Izin mikrofon ditolak. Aktifkan di Pengaturan HP → Aplikasi → CORE-9 → Izin → Mikrofon.");
      setHands(false);
      return;
    }
    heard = input.value.trim() ? input.value.trim() + " " : "";
    cancelListen = false;
    await SpeechRecognition.start({
      language: lang === "auto" ? (navigator.language || "id-ID") : LANGS[lang][1],
      partialResults: true,
      popup: false,
      maxResults: 1,
    });
  } catch (e) {
    listening = false;
    mic.classList.remove("on");
    bubble("sys", "Mikrofon tidak bisa dipakai saat ini. Pastikan aplikasi Google terpasang dan coba lagi.");
    idleStatus();
  }
}
function stopListening(cancel) {
  cancelListen = !!cancel;
  SpeechRecognition.stop().catch(() => {});
}

/* ================= pengaturan ================= */
const KEY_HELP = {
  gemini: 'Gratis: buka <span class="url">aistudio.google.com/apikey</span> → login akun Google → <b>Create API key</b> → salin, lalu tempel di bawah. Tidak perlu kartu kredit.',
  claude: 'Berbayar: buat key di <span class="url">console.anthropic.com</span> → API Keys, dan isi saldo di menu Billing (tanpa saldo, Claude tidak bisa menjawab).',
  offline: "Tanpa API key dan tanpa biaya. AI berjalan langsung di HP Anda, jadi tetap bisa dipakai tanpa internet.",
};
let formBrain = brain;
const dl = { startBytes: -1, startTime: 0 };
const gbText = (b) => (b / 1073741824).toLocaleString("id-ID", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
function showDownload(bytes, total) {
  bytes = Number(bytes) || 0;
  total = Number(total) || -1;
  const now = Date.now();
  if (dl.startBytes < 0) { dl.startBytes = bytes; dl.startTime = now; }
  const secs = (now - dl.startTime) / 1000;
  const speed = secs > 2 ? (bytes - dl.startBytes) / secs : 0;           // byte per detik
  $("offlineMeter").hidden = false;
  if (total > 0) {
    const pct = Math.min(100, (bytes / total) * 100);
    $("offlineBar").style.width = pct.toFixed(1) + "%";
    let line = `${gbText(bytes)} GB dari ${gbText(total)} GB · ${pct.toFixed(0)}%`;
    if (speed > 0) {
      line += ` · ${(speed / 1048576).toFixed(1)} MB/s`;
      const left = (total - bytes) / speed;
      line += left > 90 ? ` · sisa ±${Math.ceil(left / 60)} menit` : ` · sisa ±${Math.ceil(left)} detik`;
    }
    if (offline.state === "downloading") $("offlineSub").textContent = line;
  } else {
    $("offlineBar").style.width = "3%";
    if (offline.state === "downloading")
      $("offlineSub").textContent = bytes > 0 ? `${gbText(bytes)} GB terunduh…` : "Menghubungi server Google…";
  }
}
function updateOfflinePanel() {
  const t = $("offlineText"), sub = $("offlineSub"), ok = $("settingsOk");
  $("offlineMeter").hidden = offline.state !== "downloading";
  ok.disabled = offline.state === "downloading" || offline.state === "loading";
  if (formBrain !== "offline") { ok.textContent = "SIMPAN"; ok.disabled = false; return; }
  switch (offline.state) {
    case "ready":
      t.innerHTML = `✅ Otak offline siap: <b>${offlineLabel()}</b>.`;
      sub.textContent = "Tidak perlu internet untuk mengobrol.";
      ok.textContent = "PAKAI";
      break;
    case "loading":
      t.textContent = "⏳ Menyiapkan otak AI di HP… (bisa 10–60 detik)";
      sub.textContent = "";
      ok.textContent = "TUNGGU";
      break;
    case "downloading":
      t.innerHTML = "📥 Mengunduh otak AI <b>Gemma 4</b> dari Google (sekali saja). Layar dibuat tetap menyala; jangan tutup aplikasi.";
      ok.textContent = "MENGUNDUH";
      break;
    case "need_download":
      t.innerHTML = "HP ini belum punya AI bawaan. Unduh otak AI <b>Gemma 4</b> sekali saja (ukuran besar, beberapa GB), setelah itu bisa dipakai tanpa internet.";
      sub.textContent = (offline.partial > 0 ? `Sudah terunduh ${gbText(offline.partial)} GB, akan dilanjutkan. ` : "")
        + "Pakai Wi-Fi. Butuh ruang kosong ±4 GB dan RAM 6 GB atau lebih."
        + (offline.freeBytes ? ` Ruang kosong HP Anda: ${gbText(offline.freeBytes)} GB.` : "")
        + (offline.loadError ? ` (Info: ${offline.loadError})` : "");
      ok.textContent = offline.partial > 0 ? "LANJUTKAN" : "UNDUH";
      break;
    case "error":
      t.innerHTML = "⚠️ Gagal menyiapkan otak offline.";
      sub.textContent = "Detail: " + offline.error + "."
        + (offline.fileReady
          ? " File AI sudah terunduh lengkap; masalahnya terjadi saat AI dijalankan di HP. Tutup aplikasi lain agar RAM lega, lalu tekan COBA LAGI."
          : (offline.partial > 0 ? ` Sudah terunduh ${gbText(offline.partial)} GB dan akan dilanjutkan.` : "")
            + " Pastikan Wi-Fi stabil, lalu tekan COBA LAGI.");
      ok.textContent = "COBA LAGI";
      break;
    default:
      t.textContent = "Tekan SIAPKAN untuk memeriksa AI bawaan HP Anda.";
      sub.textContent = "";
      ok.textContent = "SIAPKAN";
  }
}
function showBrain(b) {
  formBrain = b;
  $("brainOffline").checked = b === "offline";
  $("brainGemini").checked = b === "gemini";
  $("brainClaude").checked = b === "claude";
  $("keyHelp").innerHTML = KEY_HELP[b];
  $("keyFields").hidden = b === "offline";
  $("offlinePanel").hidden = b !== "offline";
  if (b !== "offline") {
    $("apiKey").value = b === "gemini" ? gKey : apiKey;
    $("apiKey").placeholder = b === "gemini" ? "AIza..." : "sk-ant-...";
  }
  updateOfflinePanel();
}
function applyBrainLabel() {
  $("hudBrain").textContent = brain === "offline" ? (offline.kind === "nano" ? "NANO" : "OFFLINE") : brain === "gemini" ? "GEMINI" : "CLAUDE";
}
function openSettings(err) {
  showBrain(brain);
  $("settingsErr").hidden = !err;
  $("settingsErr").textContent = err || "";
  $("settingsCancel").hidden = !hasKey();
  $("settings").hidden = false;
}
$("brainOffline").onchange = () => showBrain("offline");
$("brainGemini").onchange = () => showBrain("gemini");
$("brainClaude").onchange = () => showBrain("claude");
function useBrain(b) {
  brain = b;
  store.set("core9-brain", b);
  applyBrainLabel();
  $("settings").hidden = true;
  setStatus("SIAGA");
}
$("settingsForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("settingsErr").hidden = true;
  if (formBrain === "offline") {
    try {
      if (offline.state === "ready") return useBrain("offline");
      if (offline.state === "idle") {
        if ((await loadOffline()) === "ready") return useBrain("offline");
        return;
      }
      if (offline.state === "need_download" || offline.state === "error") {
        if (await downloadOffline()) useBrain("offline");
      }
    } catch (err) {
      offline.state = "error";
      offline.error = err.message || String(err);
      updateOfflinePanel();
    }
    return;
  }
  const key = $("apiKey").value.trim();
  const bad = formBrain === "claude" ? !key.startsWith("sk-ant-") : !/^AIza[\w-]{20,}$/.test(key);
  if (bad) {
    $("settingsErr").hidden = false;
    $("settingsErr").textContent = formBrain === "claude"
      ? "API key Claude selalu diawali sk-ant-. Salin ulang dari console.anthropic.com."
      : "API key Gemini biasanya diawali AIza. Salin ulang dari aistudio.google.com/apikey.";
    return;
  }
  if (formBrain === "claude") { apiKey = key; store.set("core9-key", key); client = makeClient(key); }
  else { gKey = key; store.set("core9-gkey", key); }
  useBrain(formBrain);
});
$("settingsCancel").onclick = () => { $("settings").hidden = true; };
$("showKey").onchange = (e) => { $("apiKey").type = e.target.checked ? "text" : "password"; };
$("settingsBtn").onclick = () => openSettings();

/* ================= kontrol ================= */
form.addEventListener("submit", (e) => {
  e.preventDefault();
  if (stream) { stream.abort(); stopSpeaking(); return; }
  if (listening) { stopListening(false); return; }
  if (speaking() && !input.value.trim()) { stopSpeaking(); idleStatus(); return; }
  const t = input.value;
  input.value = ""; heard = "";
  input.style.height = "auto";
  send(t);
});
input.addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey && !e.isComposing) { e.preventDefault(); if (!stream) form.requestSubmit(); }
});
input.addEventListener("input", () => { input.style.height = "auto"; input.style.height = Math.min(input.scrollHeight, 160) + "px"; });
mic.addEventListener("click", () => {
  if (listening) { stopListening(false); return; }
  if (stream) stream.abort();
  startListening();
});
$("modeQuick").onclick = () => setMode("quick");
$("modeSmart").onclick = () => setMode("smart");
$("voiceBtn").onclick = () => setVoice(!voiceOn);
$("handsBtn").onclick = () => {
  setHands(!handsFree);
  if (handsFree && !listening && !stream && !speaking()) startListening();
  if (!handsFree && listening) stopListening(true);
};
langSel.addEventListener("change", () => { lang = langSel.value; store.set("core9-lang", lang); });
$("reset").onclick = () => {
  if (stream) stream.abort();
  stopSpeaking();
  if (listening) stopListening(true);
  turns = [];
  heard = ""; input.value = "";
  if (brain === "offline") newOfflineChat();
  updateCount();
  setStatus("SIAGA");
  greet();
  reforge();
};

/* ================= mulai ================= */
langSel.value = lang;
setMode(tier);
setVoice(voiceOn);
setHands(handsFree);
updateCount();
greet();
if (Capacitor.isNativePlatform()) initMic();
else { mic.hidden = true; $("handsBtn").hidden = true; $("hint").textContent = "ENTER = KIRIM"; }
applyBrainLabel();
if (brain === "offline") {
  // Langsung siapkan otak offline. Jika belum ada di HP, tampilkan pengaturan untuk mengunduhnya.
  if (Capacitor.isNativePlatform()) {
    setStatus("MENYIAPKAN", "work");
    loadOffline()
      .then((r) => { setStatus("SIAGA"); if (r !== "ready") openSettings(); })
      .catch((e) => { offline.state = "error"; offline.error = e.message; setStatus("SIAGA"); openSettings(); });
  } else openSettings();
} else if (!hasKey()) openSettings();
