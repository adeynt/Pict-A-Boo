package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.pictaboo.app.AppDatabase
import com.pictaboo.app.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    companion object {
        const val PREFS_NAME = "PictABooPrefs"
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = AppDatabase.getDatabase(this)

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
                CoroutineScope(Dispatchers.IO).launch {
                    val existingUser = db.userDao().getUserByEmail(email)
                    if (existingUser != null) {
                        runOnUiThread {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Email already registered",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val newUser = User(username = username, email = email, password = password)
                        val insertedId = db.userDao().insertUser(newUser) // Kembalian Long = id baru

                        // Simpan langsung ke SharedPreferences
                        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt(KEY_USER_ID, insertedId.toInt())
                            putString(KEY_USERNAME, username)
                            putString(KEY_EMAIL, email)
                            apply()
                        }

                        runOnUiThread {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration successful! You are now logged in.",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            finish()
                        }
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
