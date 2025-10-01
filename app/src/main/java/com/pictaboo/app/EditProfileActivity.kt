package com.pictaboo.app

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton


class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        // Inisialisasi View
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        // Dapatkan instance Shared Preferences
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Memuat data yang sudah tersimpan atau menggunakan data dummy awal
        val currentUsername = sharedPref.getString(KEY_USERNAME, "xxyyzz")
        val currentEmail = sharedPref.getString(KEY_EMAIL, "abc@gmail.com")

        // Mengisi field input dengan data yang termuat
        etUsername.setText(currentUsername)
        etEmail.setText(currentEmail)

        // Listener Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Username and Email cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                with (sharedPref.edit()) {
                    putString(KEY_USERNAME, newUsername)
                    putString(KEY_EMAIL, newEmail)
                    apply() // Menyimpan secara asinkron
                }

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman Profile
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}