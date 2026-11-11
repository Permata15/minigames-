# NeonAmp 🎧 — pemutar musik Android ala Winamp, tampilan neon futuristik

Fitur:
- **Cari & putar lagu dari YouTube** (streaming online) — tanpa API key, memakai NewPipeExtractor. Bisa juga tempel/bagikan link YouTube ke aplikasi.
- **Unduh untuk offline** — audio (m4a), sampul, dan lirik disimpan di penyimpanan aplikasi; tab **Offline**.
- **Musik di HP** — pindai MP3/FLAC/M4A yang sudah ada di perangkat (tab **Di HP**).
- **Lirik sinkron** (berjalan mengikuti lagu, ketuk baris untuk lompat) dari LRCLIB, ikut tersimpan saat diunduh.
- Panel **LCD ala Winamp**: waktu bercahaya, judul berjalan, visualizer spektrum.
- Shuffle, repeat (semua/satu), antrean, kontrol di notifikasi & lock screen, tetap main di background.

## Ambil APK
Setiap push ke folder `neonamp-android/` otomatis dibangun oleh GitHub Actions
(workflow **NeonAmp Android APK**). Buka tab **Actions** → run terbaru → unduh artifact
**NeonAmp-apk**, ekstrak, lalu pasang `app-release.apk` di HP (izinkan "instal dari sumber tidak dikenal").

## Build sendiri
Butuh Android Studio / JDK 17 + Android SDK 35:
```
cd neonamp-android
./gradlew assembleRelease
```

## Struktur
- `data/YouTubeRepo.kt` — pencarian & URL audio YouTube
- `data/DownloadManager.kt` — unduh offline
- `data/Lyrics.kt` — lirik LRCLIB + parser LRC
- `playback/PlaybackService.kt` — ExoPlayer (Media3) + MediaSession
- `ui/` — Jetpack Compose (tema neon, LCD, visualizer, lirik)

> Catatan: mengunduh dari YouTube bisa bertentangan dengan Ketentuan Layanan YouTube dan hak cipta.
> Gunakan untuk penggunaan pribadi dan konten yang kamu punya haknya.
> Jika suatu saat YouTube mengubah sistemnya dan lagu gagal diputar, perbarui versi `NewPipeExtractor` di `app/build.gradle.kts`.
