package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.pictaboo.app.data.User
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var currentUser: User? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        db = AppDatabase.getDatabase(this)

        // Ambil user_id dari SharedPreferences
        val prefs = getSharedPreferences(RegisterActivity.PREFS_NAME, MODE_PRIVATE)
        userId = prefs.getInt(RegisterActivity.KEY_USER_ID, -1)
        if (userId == -1) {
            // Jika session tidak ada → redirect ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadProfileData()

        // Tombol Edit Profile
        val btnEditProfile = findViewById<MaterialButton>(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Tombol Logout
        val btnLogout = findViewById<TextView>(R.id.menuLogout)
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadProfileData() {
        lifecycleScope.launch {
            currentUser = db.userDao().getUserById(userId)
            runOnUiThread {
                findViewById<TextView>(R.id.tvUsername).text = currentUser?.username ?: "Username"
                findViewById<TextView>(R.id.tvEmail).text = currentUser?.email ?: "Email"
            }
        }
    }

    /** 🔹 Tampilkan modal logout */
    private fun showLogoutDialog() {
        val logoutDialog = LogoutDialogFragment()
        logoutDialog.setLogoutDialogListener(object : LogoutDialogFragment.LogoutDialogListener {
            override fun onLogoutConfirmed() {
                performLogout()
            }
        })
        logoutDialog.show(supportFragmentManager, "logout_dialog")
    }

    /** 🔹 Hapus session dan arahkan ke WelcomeActivity */
    private fun performLogout() {
        val prefs = getSharedPreferences(RegisterActivity.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply() // hapus session
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}
