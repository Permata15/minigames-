# 🤖 Chatbot Saya: Web Chatbot dengan Claude API

Web chatbot pribadi tanpa langganan bulanan. Anda cukup bayar sesuai pemakaian lewat Claude API.

## Cara menjalankan

1. **Instal Node.js** (versi 18 ke atas) dari https://nodejs.org
2. **Buat API key** di https://console.anthropic.com → *API Keys* → *Create Key*.
   Isi saldo (*Billing*) secukupnya, misalnya $5.
3. Buka terminal di folder `chatbot`, lalu jalankan:
   ```bash
   npm install
   cp .env.example .env
   ```
4. Buka file `.env`, lalu ganti `isi-api-key-anda-di-sini` dengan API key Anda.
5. Jalankan:
   ```bash
   npm start
   ```
6. Buka **http://localhost:3000** di browser. Selesai! 🎉

## Mengubah chatbot

Semua pengaturan ada di file `.env`:

| Pengaturan | Fungsi |
|---|---|
| `SYSTEM_PROMPT` | Kepribadian/peran chatbot, misalnya *"Kamu adalah guru matematika yang sabar"* |
| `CLAUDE_MODEL` | `claude-opus-5-5` (paling pintar) atau `claude-haiku-5-5` (jauh lebih murah, cocok untuk hemat) |
| `PORT` | Port server (bawaan 3000) |

Tampilan bisa diubah di `public/index.html`.

## Struktur file

```
chatbot/
├── server.js          ← server: menerima pesan & meneruskan ke Claude
├── public/index.html  ← tampilan chat di browser
├── .env.example       ← contoh pengaturan
└── package.json
```

## ⚠️ Penting soal keamanan

- **Jangan pernah** taruh API key di `index.html` atau upload file `.env` ke GitHub.
  File `.env` sudah otomatis diabaikan lewat `.gitignore`.
- Kalau nanti dipasang online, siapa pun yang membuka web Anda bisa memakai saldo API Anda.
  Tambahkan login/password atau batasi pemakaian sebelum dibagikan ke publik.
