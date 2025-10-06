package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.security.ProviderInstaller // <-- IMPORT WAJIB
// HAPUS: import com.google.firebase.FirebaseApp
import com.pictaboo.app.KEY_EMAIL
import com.pictaboo.app.KEY_USERNAME
import com.pictaboo.app.PREFS_NAME

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Deklarasi instance Firebase Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ** START: FIX CRASH (Provider Installer) **
        ProviderInstaller.installIfNeededAsync(this, object : ProviderInstaller.ProviderInstallListener {
            override fun onProviderInstalled() {
                // Provider sudah terinstal, lanjutkan semua logika aplikasi
                setupLogic()
            }

            override fun onProviderInstallFailed(errorCode: Int, intent: Intent?) {
                // Lanjutkan setup meskipun instalasi provider gagal
                if (intent != null) {
                    startActivity(intent)
                } else {
                    Log.e("RegisterActivity", "Provider install failed with code $errorCode. Continuing setup.")
                    setupLogic()
                }
            }
        })
        // ** END: FIX CRASH **
    }

    private fun setupLogic() {
        // HAPUS: FirebaseApp.initializeApp(this) // Tidak lagi diperlukan/menyebabkan error build

        auth = FirebaseAuth.getInstance() // Inisialisasi Firebase pindah ke sini

        // Referensi UI (Dideklarasikan sebagai val lokal)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvLoginPrompt = findViewById<TextView>(R.id.tvLoginPrompt)

        btnRegister.setOnClickListener {
            val username = etUsername.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString()?.trim() ?: ""

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // LOGIKA REGISTER MENGGUNAKAN FIREBASE
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Pendaftaran sukses, simpan username secara lokal
                            val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString(KEY_USERNAME, username)
                                putString(KEY_EMAIL, email)
                                apply()
                            }

                            Toast.makeText(this, "Registration successful! Please sign in.", Toast.LENGTH_LONG).show()

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        tvLoginPrompt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}