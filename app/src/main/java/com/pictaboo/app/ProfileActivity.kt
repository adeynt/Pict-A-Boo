package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.pictaboo.app.AppDatabase
import com.pictaboo.app.data.User
import kotlinx.coroutines.launch

// Const untuk SharedPreferences
const val PREFS_NAME = "PictABooPrefs"
const val KEY_USER_ID = "user_id"

class ProfileActivity : AppCompatActivity(), LogoutDialogFragment.LogoutDialogListener {

    private lateinit var db: AppDatabase
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        db = AppDatabase.getDatabase(this)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadProfileData()

        val navProfile = findViewById<TextView>(R.id.nav_profile)

        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val navHome = findViewById<TextView>(R.id.nav_home)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val navFrame = findViewById<TextView>(R.id.nav_frame)

        navFrame.setOnClickListener {
            startActivity(Intent(this, Frames::class.java))
        }

        val navProject = findViewById<TextView>(R.id.nav_project)

        navProject.setOnClickListener {
            // Mengarah ke halaman daftar proyek lokal
            startActivity(Intent(this, ProjectsActivity::class.java))
        }
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val userId = prefs.getInt(KEY_USER_ID, -1)

        if (userId == -1) {
            Toast.makeText(this, "No logged in user", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            currentUser = db.userDao().getUserById(userId) // Kita butuh method getUserById di UserDao
            runOnUiThread {
                findViewById<TextView>(R.id.tvUsername).text =
                    "Username: ${currentUser?.username ?: "Default"}"
                findViewById<TextView>(R.id.tvEmail).text =
                    "Email: ${currentUser?.email ?: "Default"}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    fun goToMyPhotos(view: View) {
        startActivity(Intent(this, ProjectsActivity::class.java))
    }

    fun goToFaq(view: View) {
        startActivity(Intent(this, FaqActivity::class.java))
    }

    fun goToAboutApp(view: View) {
        startActivity(Intent(this, AboutAppActivity::class.java))
    }

    fun editProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }

    fun logout(view: View) {
        val logoutDialog = LogoutDialogFragment()
        logoutDialog.setLogoutDialogListener(this)
        logoutDialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }

    override fun onLogoutConfirmed() {
        // Hapus session
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_ID).apply()

        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
