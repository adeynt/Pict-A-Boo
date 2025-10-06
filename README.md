# Pict A Boo: Fun Photo Booth App 📸

<img src="https://github.com/adeynt/Pict-A-Boo/raw/main/app/src/main/res/drawable/logo_pictabook.png" alt="App Logo" width="300px" />

Pict A Boo adalah aplikasi *Photo Booth* bergaya fun dan unik yang dikembangkan untuk platform Android. Aplikasi ini memungkinkan pengguna menangkap beberapa foto dan menggabungkannya ke dalam satu *photo strip* yang menarik dengan berbagai pilihan *frame* dekoratif. Aplikasi ini mendukung autentikasi pengguna lokal dan penyimpanan proyek ke galeri perangkat.

---

## ✨ Fitur Utama

Aplikasi ini menyertakan fitur-fitur berikut:

* **Autentikasi Pengguna:** Pendaftaran dan *login* pengguna diimplementasikan menggunakan **Room Database** untuk manajemen sesi dan data pengguna lokal.
* **Manajemen Profil:** Pengguna dapat mengedit username dan email serta mengganti foto profil mereka dari galeri perangkat.
* **Pengambilan Foto:** Integrasi kamera penuh menggunakan **CameraX API**, mendukung pergantian kamera depan/belakang dan mode hitung mundur (timer).
* **Pembuatan Photo Strip:** Pengguna dapat menangkap hingga 3 foto yang kemudian digabungkan secara otomatis ke dalam *frame* yang dipilih.
* **Koleksi Frame:** Menyediakan berbagai pilihan *frame* foto yang lucu dan *stylish*.
* **Penyimpanan Lokal (Projects):** Foto hasil akhir disimpan ke Galeri perangkat, dan metadata proyek (URI) dilacak menggunakan Room Database.

<img src="https://github.com/adeynt/Pict-A-Boo/raw/main/app/src/main/res/drawable/welcome_img.png" alt="Welcome Image Illustration" width="300px" />

---

## 🛠️ Teknologi yang Digunakan

| Kategori | Teknologi | Versi/Catatan |
| :--- | :--- | :--- |
| **Bahasa** | Kotlin | Versi 1.9.23 |
| **Kamera** | CameraX | `1.3.0` (Core, Camera2, Lifecycle, View) |
| **Database** | Room Persistence Library | `2.6.1` untuk penyimpanan data lokal |
| **Pemuatan Gambar** | Glide | `4.16.0` untuk memuat gambar URI dan *preview* |
| **Dependencies**| AndroidX, Material3 | Digunakan untuk komponen UI modern dan kompatibilitas |

---

## ⚙️ Setup Proyek

Untuk menjalankan proyek ini secara lokal, Anda memerlukan lingkungan pengembangan Android Studio dengan Java Development Kit (JDK) yang kompatibel.

### Persyaratan Minimum

* **Min SDK:** 24
* **Target SDK:** 36
* **Gradle:** 8.13

### Langkah Instalasi

1.  **Clone Repositori:**
    ```bash
    git clone [https://github.com/adeynt/Pict-A-Boo.git](https://github.com/adeynt/Pict-A-Boo.git)
    ```

2.  **Buka di Android Studio:**
    Buka folder proyek di Android Studio dan tunggu hingga Gradle selesai menyinkronkan dependensi.

3.  **Jalankan Aplikasi:**
    Pilih perangkat fisik atau emulator (Android 7.0 / API 24 atau lebih tinggi) dan jalankan aplikasi. Aplikasi akan meminta izin kamera dan penyimpanan saat pertama kali dibuka.

### Ilustrasi Frame

Berikut adalah salah satu contoh *frame* yang tersedia di aplikasi:

<img src="https://github.com/adeynt/Pict-A-Boo/raw/main/app/src/main/res/drawable/frame_4.png" alt="Frame Preview" width="200px" />
