package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pictaboo.app.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi database Room
        db = AppDatabase.getDatabase(this)

        val etEmail = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUpPrompt = findViewById<TextView>(R.id.tvSignUpPrompt)

        // Tombol Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Query Room DB di Coroutine
                lifecycleScope.launch {
                    val user = db.userDao().getUserByEmail(email)
                    if (user != null && user.password == password) {
                        // Login sukses
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                        // Simpan session ke SharedPreferences yang sama dengan RegisterActivity
                        val prefs = getSharedPreferences(RegisterActivity.PREFS_NAME, MODE_PRIVATE)
                        with(prefs.edit()) {
                            putInt(RegisterActivity.KEY_USER_ID, user.id)
                            putString(RegisterActivity.KEY_USERNAME, user.username)
                            putString(RegisterActivity.KEY_EMAIL, user.email)
                            apply()
                        }

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Login gagal
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Email or password incorrect", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Logika Sign Up link
        val spannable = android.text.SpannableString("Don’t have an account? Sign Up")
        val start = spannable.indexOf("Sign Up")
        val end = start + "Sign Up".length

        val clickableSpan = object : android.text.style.ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = android.graphics.Color.parseColor("#E68BB4")
            }
        }

        spannable.setSpan(clickableSpan, start, end, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvSignUpPrompt.text = spannable
        tvSignUpPrompt.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }
}
