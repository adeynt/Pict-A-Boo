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
import com.pictaboo.app.KEY_EMAIL
import com.pictaboo.app.KEY_USERNAME
import com.pictaboo.app.PREFS_NAME
class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLoginPrompt: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginPrompt = findViewById(R.id.tvLoginPrompt)

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