import Anthropic from "@anthropic-ai/sdk";
import { Capacitor } from "@capacitor/core";
import { SpeechRecognition } from "@capacitor-community/speech-recognition";
import { TextToSpeech, QueueStrategy } from "@capacitor-community/text-to-speech";

/* ================= konfigurasi ================= */
// KILAT: model tercepat, tanpa berpikir dulu → jawaban langsung keluar.
// CERDAS: model terpintar, berpikir sebelum menjawab.
const MODELS = { quick: "claude-haiku-5-5", smart: "claude-opus-5-5" };

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
let stream = null;            // stream Claude yang sedang berjalan
let tier = store.get("core9-mode", "quick") === "smart" ? "smart" : "quick";
let lang = store.get("core9-lang", "auto");
if (lang !== "auto" && !LANGS[lang]) lang = "auto";
let voiceOn = store.get("core9-voice", "on") === "on";
let handsFree = store.get("core9-hands", "off") === "on";
let apiKey = store.get("core9-key", "");
let client = apiKey ? makeClient(apiKey) : null;

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

function openStream() {
  const common = {
    max_tokens: 16000,
    system: systemPrompt(),
    messages: turns.map((t) => ({ role: t.role, content: t.content })),
  };
  if (tier === "quick") {
    return client.messages.stream({
      ...common,
      model: MODELS.quick,
      thinking: { type: "disabled" },          // tanpa jeda berpikir
      output_config: { effort: "low" },
    });
  }
  return client.beta.messages.stream({
    ...common,
    model: MODELS.smart,
    output_config: { effort: "medium" },
    betas: ["server-side-fallback-2026-07-01"],
    fallbacks: "default",                        // jika ditolak filter keamanan, model cadangan melanjutkan
  });
}

function errorText(e) {
  if (e instanceof Anthropic.AuthenticationError) return { msg: "API key salah atau tidak aktif. Periksa di ⚙ Pengaturan.", settings: true };
  if (e instanceof Anthropic.PermissionDeniedError) return { msg: "API key ini tidak punya izin. Periksa akun di console.anthropic.com.", settings: true };
  if (e instanceof Anthropic.RateLimitError) return { msg: "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi." };
  if (e instanceof Anthropic.BadRequestError) {
    const low = /credit|balance|billing/i.test(e.message);
    return { msg: low ? "Saldo API habis. Isi saldo di console.anthropic.com → Billing." : "Permintaan ditolak: " + e.message };
  }
  if (e instanceof Anthropic.InternalServerError) return { msg: "Server Claude sedang sibuk. Coba kirim ulang." };
  if (e instanceof Anthropic.APIConnectionError) return { msg: "Tidak ada koneksi internet atau server tidak terjangkau." };
  return { msg: "Terjadi gangguan: " + (e && e.message ? e.message : "tidak diketahui") };
}

async function send(text) {
  text = (text || "").trim();
  if (!text || stream) return;
  if (!client) { openSettings(); return; }
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
  try {
    stream = openStream();
    for await (const ev of stream) {
      if (ev.type === "content_block_delta" && ev.delta.type === "text_delta") {
        full += ev.delta.text;
        body.innerHTML = format(full);
        log.scrollTop = log.scrollHeight;
        if (!speaking()) setStatus("MENJAWAB", "work");
        if (speech === mySpeech) feedSpeech(full, false);
      }
    }
    const msg = await stream.finalMessage();
    if (msg.stop_reason === "refusal") {
      if (!full) body.parentElement.remove();
      bubble("sys", "Permintaan ini tidak bisa diproses. Coba ubah pertanyaannya.");
    } else if (msg.stop_reason === "max_tokens") {
      bubble("sys", "Jawaban terpotong karena terlalu panjang. Minta lanjutannya jika perlu.");
    }
    ok = true;
  } catch (e) {
    const aborted = e instanceof Anthropic.APIUserAbortError || (stream && stream.aborted);
    if (!aborted) {
      stopSpeaking();
      const { msg, settings } = errorText(e);
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
function openSettings(err) {
  $("apiKey").value = apiKey;
  $("settingsErr").hidden = !err;
  $("settingsErr").textContent = err || "";
  $("settingsCancel").hidden = !apiKey;
  $("settings").hidden = false;
  setTimeout(() => $("apiKey").focus(), 50);
}
$("settingsForm").addEventListener("submit", (e) => {
  e.preventDefault();
  const key = $("apiKey").value.trim();
  if (!key.startsWith("sk-ant-")) {
    $("settingsErr").hidden = false;
    $("settingsErr").textContent = "API key Claude selalu diawali sk-ant-. Salin ulang dari console.anthropic.com.";
    return;
  }
  apiKey = key;
  store.set("core9-key", key);
  client = makeClient(key);
  $("settings").hidden = true;
  setStatus("SIAGA");
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
if (!apiKey) openSettings();
