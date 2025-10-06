package com.pictaboo.app

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.pictaboo.app.AppDatabase
import com.pictaboo.app.data.User
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        db = AppDatabase.getDatabase(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        // Ambil user_id dari SharedPreferences
        val sharedPref = getSharedPreferences(RegisterActivity.PREFS_NAME, MODE_PRIVATE)
        userId = sharedPref.getInt(RegisterActivity.KEY_USER_ID, -1)

        if (userId != -1) {
            // Load data user dari Room Database
            lifecycleScope.launch {
                val user = db.userDao().getUserById(userId)
                if (user != null) {
                    etUsername.setText(user.username)
                    etEmail.setText(user.email)
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Username and Email cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (userId != -1) {
                lifecycleScope.launch {
                    val user = db.userDao().getUserById(userId)
                    if (user != null) {
                        val updatedUser = user.copy(username = newUsername, email = newEmail)
                        db.userDao().insertUser(updatedUser) // Bisa juga pakai @Update di DAO
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
