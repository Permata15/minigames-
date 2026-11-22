// Server web chatbot sederhana memakai Claude API.
// API key disimpan di server (file .env), TIDAK pernah dikirim ke browser.
import "dotenv/config";
import express from "express";
import Anthropic from "@anthropic-ai/sdk";

const app = express();
const PORT = process.env.PORT || 3000;

// Ganti model lewat .env. claude-haiku-5-5 jauh lebih murah, claude-opus-5-5 paling pintar.
const MODEL = process.env.CLAUDE_MODEL || "claude-opus-5-5";

// "Kepribadian" chatbot Anda. Silakan ubah sesuka hati di .env.
const SYSTEM_PROMPT =
  process.env.SYSTEM_PROMPT ||
  "Kamu adalah asisten yang ramah dan membantu. Jawab dalam Bahasa Indonesia yang santai dan jelas.";

const client = new Anthropic(); // membaca ANTHROPIC_API_KEY dari environment

app.use(express.json({ limit: "1mb" }));
app.use(express.static("public"));

app.post("/api/chat", async (req, res) => {
  const { messages } = req.body ?? {};

  // Validasi sederhana: hanya terima role user/assistant berisi teks.
  if (
    !Array.isArray(messages) ||
    messages.length === 0 ||
    !messages.every(
      (m) =>
        (m.role === "user" || m.role === "assistant") &&
        typeof m.content === "string" &&
        m.content.length > 0,
    )
  ) {
    return res.status(400).json({ error: "Format pesan tidak valid." });
  }

  res.setHeader("Content-Type", "text/plain; charset=utf-8");
  res.setHeader("Cache-Control", "no-cache");

  try {
    const stream = client.beta.messages.stream({
      model: MODEL,
      max_tokens: 64000,
      system: SYSTEM_PROMPT,
      messages,
      // Usaha berpikir rendah = cepat & hemat, cukup untuk obrolan biasa.
      output_config: { effort: "low" },
      // Jika model menolak karena filter keamanan, otomatis dilanjutkan model cadangan.
      betas: ["server-side-fallback-2026-07-01"],
      fallbacks: "default",
    });

    for await (const event of stream) {
      if (
        event.type === "content_block_delta" &&
        event.delta.type === "text_delta"
      ) {
        res.write(event.delta.text);
      }
    }

    const final = await stream.finalMessage();
    if (final.stop_reason === "refusal") {
      res.write("\n\n[Maaf, permintaan ini tidak dapat dijawab.]");
    } else if (final.stop_reason === "max_tokens") {
      res.write("\n\n[Jawaban terpotong karena terlalu panjang.]");
    }
    res.end();
  } catch (error) {
    console.error(error);
    let msg = "Terjadi kesalahan di server.";
    if (error instanceof Anthropic.AuthenticationError) {
      msg = "API key salah atau belum diisi. Cek file .env.";
    } else if (error instanceof Anthropic.RateLimitError) {
      msg = "Terlalu banyak permintaan. Coba lagi sebentar lagi.";
    } else if (error instanceof Anthropic.BadRequestError) {
      msg = `Permintaan ditolak: ${error.message}`;
    } else if (error instanceof Anthropic.APIConnectionError) {
      msg = "Tidak bisa terhubung ke server Claude.";
    }
    if (!res.headersSent) res.status(500);
    res.end(`\n\n[Error: ${msg}]`);
  }
});

app.listen(PORT, () => {
  console.log(`Chatbot berjalan di http://localhost:${PORT} (model: ${MODEL})`);
});
