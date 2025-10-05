package com.pictaboo.app

import android.content.Intent // Penting untuk Intent dan Flags
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth // Diperlukan untuk 'auth'

// DEKLARASI CONST VAL DI SINI (Membuatnya dapat diakses oleh RegisterActivity & LoginActivity)
const val PREFS_NAME = "PictABooPrefs"
const val KEY_USERNAME = "username"
const val KEY_EMAIL = "email"
// ===================================================================

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Inisialisasi Firebase Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        loadProfileData()

        val btnEditProfile = findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val menuLogout = findViewById<TextView>(R.id.menuLogout)

        // Listener Tombol Edit Profile
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Listener Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Listener Logout: Sign Out dari Firebase
        menuLogout.setOnClickListener {
            auth.signOut() // Sign Out dari Firebase
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

            // Arahkan ke WelcomeActivity dan hapus semua history Activity sebelumnya
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Menggunakan Intent.FLAG_ACTIVITY
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Fungsi untuk memuat data dari SharedPreferences & Firebase
    private fun loadProfileData() {
        // PERHATIAN: Periksa ulang inisialisasi untuk jaga-jaga (meskipun seharusnya sudah terinisialisasi di onCreate)
        if (!::auth.isInitialized) {
            auth = FirebaseAuth.getInstance()
        }

        val currentUser = auth.currentUser
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Menggunakan nilai default string yang benar
        val email = currentUser?.email ?: sharedPref.getString(KEY_EMAIL, "abc@gmail.com (Default)")
        val username = sharedPref.getString(KEY_USERNAME, "xxyyzz (Default)")

        // Set data ke TextView di layout
        findViewById<TextView>(R.id.tvUsername).text = "Username: $username"
        findViewById<TextView>(R.id.tvEmail).text = "Email: ${email ?: "Not Logged In"}"
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }
}