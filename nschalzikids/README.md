# Nschalzikids 🧕✨

Aplikasi Android untuk anak menonton video YouTube **tanpa iklan**, dengan ikon gadis berkerudung.

## Fitur
- Beranda ramah anak: 12 kategori (Lagu Anak, Mengaji, Lagu Islami, Doa Harian, Kisah Nabi, Huruf, Berhitung, Warna, Hewan, Dongeng, Bahasa Inggris, Prakarya) + kotak pencarian.
- **Pemblokir iklan bawaan** ala Brave Shields: domain iklan/pelacak diblokir, banner iklan disembunyikan, dan iklan video dilewati otomatis.
- **Mode Terbatas YouTube** aktif otomatis (menyaring konten dewasa).
- Anak tidak bisa keluar ke situs lain: semua tautan selain YouTube diblokir.
- Tombol beranda melayang (logo kerudung) untuk kembali ke menu utama.
- Video bisa layar penuh (landscape).

## Cara mendapatkan APK
1. Buka tab **Actions** di repositori GitHub → workflow **Build Nschalzikids APK**.
2. Pilih run terbaru yang berhasil (centang hijau) → unduh artefak **Nschalzikids-apk**.
3. Ekstrak zip-nya, salin `Nschalzikids.apk` ke HP, lalu pasang (izinkan "Instal dari sumber tidak dikenal").

Atau bangun sendiri dengan Android Studio / `./gradlew assembleRelease` (butuh Android SDK).

## Catatan
- YouTube sering mengubah situsnya, jadi sesekali iklan bisa lolos sebelum pemblokir diperbarui.
- Aplikasi ini untuk pemakaian pribadi; tidak bisa diterbitkan di Play Store karena memblokir iklan YouTube.
