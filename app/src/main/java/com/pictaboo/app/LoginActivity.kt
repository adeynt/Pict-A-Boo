package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // Tambahkan import
import android.util.Log // Tambahkan import

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Deklarasi instance Firebase Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 💡 Cek jika user sudah login (sesi aktif), langsung pindah ke MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return // Keluar dari onCreate agar tidak menjalankan kode di bawahnya
        }

        // Referensi UI
        // Catatan: Layout Anda menggunakan etUsername untuk input email/username
        val etEmail = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUpPrompt = findViewById<TextView>(R.id.tvSignUpPrompt)

        // Login button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // LOGIKA LOGIN MENGGUNAKAN FIREBASE
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login sukses
                            Log.d("LoginActivity", "signInWithEmail:success")
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                            // Pindah ke MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Login gagal
                            Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        // Logika Sign Up link (kode yang sudah ada)
        val spannable = android.text.SpannableString("Don’t have an account? Sign Up")
        val start = spannable.indexOf("Sign Up")
        val end = start + "Sign Up".length

        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // hilangkan garis bawah
                ds.color = android.graphics.Color.parseColor("#E68BB4") // warna pink
            }
        }

        spannable.setSpan(clickableSpan, start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvSignUpPrompt.text = spannable
        tvSignUpPrompt.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }
}