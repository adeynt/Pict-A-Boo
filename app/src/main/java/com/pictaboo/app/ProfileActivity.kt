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
import com.google.firebase.auth.FirebaseAuth

// DEKLARASI CONST VAL DI SINI
const val PREFS_NAME = "PictABooPrefs"
const val KEY_USERNAME = "username"
const val KEY_EMAIL = "email"

class ProfileActivity : AppCompatActivity(), LogoutDialogFragment.LogoutDialogListener { // Implementasikan interface di sini!

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        loadProfileData()

        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadProfileData() {
        if (!::auth.isInitialized) {
            auth = FirebaseAuth.getInstance()
        }
        val currentUser = auth.currentUser
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val email = currentUser?.email ?: sharedPref.getString(KEY_EMAIL, "abc@gmail.com (Default)")
        val username = sharedPref.getString(KEY_USERNAME, "xxyyzz (Default)")

        findViewById<TextView>(R.id.tvUsername).text = "Username: $username"
        findViewById<TextView>(R.id.tvEmail).text = "Email: ${email ?: "Not Logged In"}"
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    fun goToMyPhotos(view: View) {
        val intent = Intent(this, ProjectsActivity::class.java)
        startActivity(intent)
    }

    fun goToFaq(view: View) {
        val intent = Intent(this, FaqActivity::class.java)
        startActivity(intent)
    }

    fun goToAboutApp(view: View) {
        val intent = Intent(this, AboutAppActivity::class.java)
        startActivity(intent)
    }

    fun editProfile(view: View) {
        startActivity(Intent(this, EditProfileActivity::class.java))
    }

    // Dipanggil dari menuLogout
    fun logout(view: View) {
        val logoutDialog = LogoutDialogFragment()
        logoutDialog.setLogoutDialogListener(this)
        logoutDialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }

    override fun onLogoutConfirmed() {
        auth.signOut()
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}