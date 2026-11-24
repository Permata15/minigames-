# Sinema Bebas — Aplikasi Android Nonton Film Gratis & Legal

Aplikasi Android ringan (Kotlin + Jetpack Compose + Media3 ExoPlayer) untuk menonton **film, serial TV, dan drama** secara gratis dari sumber legal.

## Fitur
- **Katalog gratis & legal** dari Internet Archive, default hanya film **tahun 1997 ke atas** (bisa diubah di Pengaturan): Terbaru, Film Populer, Drama & Indie, Film Pendek, Animasi, Sci‑Fi & Horor, Serial & Web Series.
- **Pencarian** judul dengan scroll tanpa batas.
- **Serial**: daftar episode, putar berurutan (tombol next/prev otomatis).
- **Subtitle Indonesia, Inggris, Jerman**
  - Subtitle bawaan (embedded / dari sumber) dipilih otomatis sesuai bahasa favorit.
  - Tombol **CC** di player untuk ganti subtitle kapan saja.
  - Tombol **+ Subtitle** untuk memuat file `.srt` / `.vtt` / `.ass` dari HP.
- **Ganti audio**: ikon ⚙ di player → *Audio track* (untuk video dengan beberapa bahasa dubbing, misalnya HLS/DASH/MKV multi‑audio). Bahasa audio favorit bisa diatur.
- **Sumber Saya**: tambahkan tautan MP4 / HLS `.m3u8` / DASH `.mpd` atau impor playlist **M3U**, lengkap dengan URL subtitle per bahasa.
- **Lanjutkan menonton**: posisi terakhir disimpan otomatis.
- **Ringan & lancar**: tanpa library jaringan besar, R8 + shrink resources, buffer adaptif, mode **Hemat kuota** (maks 480p).

> Aplikasi ini tidak mengambil konten Netflix atau layanan berbayar lain. Tambahkan di "Sumber Saya" hanya tautan yang Anda berhak tonton.

## 📥 Download APK (langsung)
**https://github.com/Permata15/minigames-/releases/latest/download/SinemaBebas.apk**

Buka link di HP → file terunduh → ketuk untuk instal (izinkan "Instal dari sumber ini").

## Cara membangun sendiri
**Otomatis (GitHub Actions):** setiap push ke folder `android-app/` membangun APK. Buka tab **Actions** → *Build Android APK* → run terbaru → unduh artifact **SinemaBebas-apk**, ekstrak, lalu instal `app-release.apk` di HP (izinkan "Instal dari sumber tidak dikenal").

**Manual (Android Studio):** buka folder `android-app/`, tunggu Gradle sync, lalu *Run* atau *Build → Build APK(s)*.
Atau lewat terminal: `cd android-app && ./gradlew assembleRelease`.

Minimal Android 8.0 (API 26).

## Struktur
```
app/src/main/java/com/permata/sinemabebas/
├── MainActivity.kt          # navigasi bawah: Beranda, Cari, Sumber Saya, Pengaturan
├── PlayerActivity.kt        # ExoPlayer: subtitle, audio track, file subtitle lokal, resume
├── data/ArchiveRepository.kt# katalog & detail dari archive.org
├── data/Prefs.kt            # pengaturan, sumber pengguna, riwayat, posisi tonton
├── data/M3u.kt              # parser playlist M3U
└── ui/                      # layar Compose
```
