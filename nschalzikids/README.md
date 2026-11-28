# Nschalzikids 🧕✨

Aplikasi Android untuk anak menonton video YouTube **tanpa iklan**, dengan ikon gadis berkerudung.

## Fitur
- **Hanya channel anak pilihan**: anak hanya bisa membuka channel di daftar. Setiap video diperiksa dulu pemiliknya sebelum diputar; video dari channel lain langsung diblokir.
- Tanpa pencarian bebas, Shorts, beranda YouTube, video rekomendasi, komentar, atau tautan keluar.
- **Pemblokir iklan berlapis**: domain iklan diblokir, data iklan dihapus dari respons YouTube sebelum pemutar membacanya, lalu iklan yang masih lolos dilewati otomatis.
- **Mode Terbatas YouTube** aktif sebagai lapisan tambahan.
- **Mode Orang Tua** (soal perkalian): cari channel, tekan "➕ Izinkan channel ini" untuk menambah, ✕ untuk menghapus. Terkunci otomatis saat aplikasi ditinggalkan.
- Channel bawaan: Nussa, Riko The Series, Omar & Hana, Diva The Series, Little Angel Indonesia, YuBimba, Numberblocks, Alphablocks, Super Simple Songs, Sesame Street, SciShow Kids, PBS KIDS.
- APK sangat ringan (tanpa library tambahan, diperkecil dengan R8).

## Cara mendapatkan APK
1. Buka tab **Actions** di repositori GitHub → workflow **Build Nschalzikids APK**.
2. Pilih run terbaru yang berhasil (centang hijau) → unduh artefak **Nschalzikids-apk**.
3. Ekstrak zip-nya, salin `Nschalzikids.apk` ke HP, lalu pasang (izinkan "Instal dari sumber tidak dikenal").

Atau bangun sendiri dengan Android Studio / `./gradlew assembleRelease` (butuh Android SDK).

## Catatan
- YouTube sering mengubah situsnya, jadi sesekali iklan bisa lolos sebelum pemblokir diperbarui.
- Aplikasi ini untuk pemakaian pribadi; tidak bisa diterbitkan di Play Store karena memblokir iklan YouTube.
