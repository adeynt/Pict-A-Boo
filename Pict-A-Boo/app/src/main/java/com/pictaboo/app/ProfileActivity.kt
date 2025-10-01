package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
const val PREFS_NAME = "PictABooPrefs"
const val KEY_USERNAME = "username"
const val KEY_EMAIL = "email"
// ===================================================================

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Panggil data profil segera setelah Activity dibuat
        loadProfileData()

        // --- Inisialisasi View & Listener ---
        val btnEditProfile = findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val menuLogout = findViewById<TextView>(R.id.menuLogout)

        // Listener Tombol Edit Profile
        btnEditProfile.setOnClickListener {
            // Membuka halaman Edit Profile
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Listener Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Listener Logout (Contoh: Anda bisa tambahkan listener untuk menu lain di sini)
        menuLogout.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            // Logout: Arahkan ke WelcomeActivity dan hapus semua history Activity sebelumnya
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Apply Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Fungsi untuk memuat data dari Shared Preferences
    private fun loadProfileData() {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Baca data yang tersimpan. Jika belum ada, gunakan nilai default/dummy awal.
        val username = sharedPref.getString(KEY_USERNAME, "xxyyzz")
        val email = sharedPref.getString(KEY_EMAIL, "abc@gmail.com")

        // Set data ke TextView di layout
        findViewById<TextView>(R.id.tvUsername).text = "Username: $username"
        findViewById<TextView>(R.id.tvEmail).text = "Email: $email"
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }
}