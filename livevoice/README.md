# LiveVoice AI 🎙️

Teman ngobrol AI berbasis **suara** + penerjemah dua arah + teleprompter, untuk Android (APK) dan web.

## Fitur
- **Ngobrol (voice-to-voice)** — tekan bola, bicara bahasa apa saja; AI menjawab dengan teks **dan suara**, lalu otomatis mendengarkan lagi (hands-free) sehingga bisa tektokan lancar seperti dengan teman.
  - Mode **Universal AI**: audio dikirim langsung ke Gemini → bahasa apa pun (termasuk Jawa/Sunda/campuran) terdeteksi otomatis dan dijawab dalam bahasa yang sama.
  - Mode **Pengenal suara HP**: teks muncul real-time saat bicara.
  - Suara mulai keluar per kalimat selagi jawaban masih ditulis → respons terasa instan.
  - Ketuk bola saat AI bicara untuk **memotong** dan langsung bicara.
  - Opsional Google Search agar jawaban faktual & terkini (sumber ditampilkan).
- **Terjemah** — juru bahasa dua arah (tombol per bahasa), hasil dibacakan otomatis; input ketik dengan deteksi bahasa otomatis.
- **Prompter** — naskah mengikuti suaramu kata demi kata, bisa diterjemahkan dan diperdengarkan contoh pelafalannya.
- **Setelan** — API key, model, kepribadian (teman/guru/profesional), kecepatan & nada suara, jeda bicara.

## Cara dapat APK
Setiap push ke folder `livevoice/` menjalankan GitHub Actions **Build LiveVoice APK**:
- Unduh dari **Releases → `livevoice-v2.0.N`** → file `.apk`, atau dari tab **Actions** (artifact).
- Di HP: buka file APK → izinkan *instal dari sumber tidak dikenal*.

Build manual (butuh Node 22, JDK 21, Android SDK):
```bash
cd livevoice
npm install
npm run android:init   # sekali saja: buat proyek android + izin + ikon
npm run apk            # hasil: android/app/build/outputs/apk/debug/app-debug.apk
```

## Pertama kali dipakai
**Tanpa API key** — langsung buka aplikasi, ketuk bola, dan ngobrol.
- Otak AI default: AI gratis tanpa key (Pollinations, model gpt-oss-20b). Pengguna anonim dibatasi; bila server minta antre, aplikasi menunggu otomatis.
- Suara dikenali oleh pengenal suara HP, jawaban dibacakan oleh suara HP. Bahasa jawaban mengikuti bahasa yang kamu pakai.
- Opsional: di **Setelan → Otak AI** pilih *Google Gemini* dan isi API key sendiri untuk mode Universal AI (mendengar langsung bahasa apa pun) dan Google Search.

Versi web: `npm run serve` lalu buka http://localhost:8080
