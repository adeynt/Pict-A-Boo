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

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Hubungkan Activity ini dengan layout XML
        setContentView(R.layout.activity_profile)

        // --- 1. Inisialisasi View ---
        val btnEditProfile = findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        val menuMyPhotos = findViewById<TextView>(R.id.menuMyPhotos)
        val menuHelp = findViewById<TextView>(R.id.menuHelp)
        val menuAboutApp = findViewById<TextView>(R.id.menuAboutApp)
        val menuLogout = findViewById<TextView>(R.id.menuLogout)

        // Mengisi data profil dummy (Nanti diganti dengan data dari API/database)
        findViewById<TextView>(R.id.tvUsername).text = "Username: xxyyzz"
        findViewById<TextView>(R.id.tvEmail).text = "Email: abc@gmail.com"


        // --- 2. Listener Tombol Kembali ---
        btnBack.setOnClickListener {
            // Menutup activity saat ini, kembali ke halaman sebelumnya
            finish()
        }

        // --- 3. Listener untuk Menu Interaktif ---
        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile Page Not Implemented Yet", Toast.LENGTH_SHORT).show()
        }

        menuMyPhotos.setOnClickListener {
            Toast.makeText(this, "My Photos Page Not Implemented Yet", Toast.LENGTH_SHORT).show()
        }

        menuHelp.setOnClickListener {
            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show()
        }

        menuAboutApp.setOnClickListener {
            Toast.makeText(this, "About App clicked", Toast.LENGTH_SHORT).show()
        }

        menuLogout.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            // Logout: Arahkan ke WelcomeActivity dan hapus semua history Activity sebelumnya
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Apply Window Insets untuk desain edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}