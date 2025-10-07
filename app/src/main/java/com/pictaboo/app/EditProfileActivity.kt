package com.pictaboo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.pictaboo.app.AppDatabase
import com.pictaboo.app.data.User
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide
import com.pictaboo.app.ProfileActivity.Companion.KEY_USER_ID
import com.pictaboo.app.ProfileActivity.Companion.PREFS_NAME

class EditProfileActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var userId: Int = -1
    private var currentProfileUri: String? = null // State untuk menyimpan URI baru/lama

    // Permission request untuk Galeri
    private val requestGalleryPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGalleryForProfilePic()
        } else {
            Toast.makeText(this, "Izin galeri ditolak. Tidak bisa mengganti foto profil.", Toast.LENGTH_SHORT).show()
        }
    }

    // Contract untuk memilih satu gambar dari Galeri (PERBAIKAN FINAL URI PERSISTEN)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data

            imageUri?.let { uri ->

                // Ambil flags yang diberikan oleh sistem dari Intent hasil
                // Masking dengan FLAG_GRANT_READ_URI_PERMISSION adalah kunci untuk mendapatkan izin baca.
                val takeFlags = result.data?.flags?.and(Intent.FLAG_GRANT_READ_URI_PERMISSION) ?: 0

                if (takeFlags != 0) {
                    try {
                        // Minta izin akses persisten. Ini adalah baris kode kunci.
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                        Log.d("EditProfile", "Berhasil mendapatkan izin persisten untuk URI")

                        // Lanjutkan ke alur sukses:
                        currentProfileUri = uri.toString()
                        loadProfileImage(currentProfileUri)

                    } catch (e: SecurityException) {
                        Log.e("EditProfile", "Gagal meminta izin persisten: ${e.message}")
                        Toast.makeText(this, "Gagal mendapatkan akses permanen ke foto. (Security Issue)", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Kasus ketika sistem tidak memberikan flag yang dibutuhkan
                    Log.e("EditProfile", "Gagal: Intent hasil tidak menyertakan flag izin baca.")
                    Toast.makeText(this, "Gagal mendapatkan akses permanen ke foto. (Missing Flag)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            // Amankan izin akses persisten (tetap penting meskipun menggunakan PickVisualMedia)
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                // Minta izin akses persisten.
                contentResolver.takePersistableUriPermission(uri, flag)
                Log.d("EditProfile", "Berhasil mendapatkan izin persisten dengan PickVisualMedia.")

                // Update state URI yang akan disimpan ke Room
                currentProfileUri = uri.toString()

                // Tampilkan gambar di UI
                loadProfileImage(currentProfileUri)

            } catch (e: SecurityException) {
                Log.e("EditProfile", "Gagal takePersistableUriPermission: ${e.message}")
                Toast.makeText(this, "Gagal mendapatkan akses permanen ke foto (Perangkat Anda tidak mendukung Persistence Flag untuk URI Galeri).", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("EditProfile", "Pemilihan media dibatalkan")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        db = AppDatabase.getDatabase(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        val ivProfilePic = findViewById<ImageView>(R.id.ivProfilePic)

        // Ambil user_id dari SharedPreferences
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        userId = sharedPref.getInt(KEY_USER_ID, -1)

        if (userId != -1) {
            // Load data user dari Room Database
            lifecycleScope.launch {
                val user = db.userDao().getUserById(userId)
                if (user != null) {
                    etUsername.setText(user.username)
                    etEmail.setText(user.email)
                    currentProfileUri = user.profilePictureUri // Muat URI yang sudah ada
                    // Muat gambar ke UI
                    runOnUiThread {
                        loadProfileImage(currentProfileUri)
                    }
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        // MENGAKTIFKAN FITUR GANTI FOTO PROFIL
        // Klik pada ImageView akan memicu pemilihan gambar dari galeri
        ivProfilePic.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Username dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else if (userId != -1) {
                lifecycleScope.launch {
                    val user = db.userDao().getUserById(userId)
                    if (user != null) {
                        // Perbarui object User dengan data baru, termasuk profilePictureUri
                        val updatedUser = user.copy(
                            username = newUsername,
                            email = newEmail,
                            profilePictureUri = currentProfileUri // Simpan URI baru/lama
                        )
                        db.userDao().insertUser(updatedUser) // Room akan update jika ID sudah ada

                        // Update SharedPreferences juga (opsional)
                        with(sharedPref.edit()) {
                            putString(RegisterActivity.KEY_USERNAME, newUsername)
                            putString(RegisterActivity.KEY_EMAIL, newEmail)
                            apply()
                        }

                        runOnUiThread {
                            Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
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

    // Fungsi pembantu untuk memuat gambar dengan Glide
    private fun loadProfileImage(uriString: String?) {
        val ivProfilePic = findViewById<ImageView>(R.id.ivProfilePic)
        val uri = if (uriString.isNullOrEmpty()) null else Uri.parse(uriString)

        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .into(ivProfilePic)
    }

    private fun checkGalleryPermission() {
        val permissionToRequest =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openGalleryForProfilePic()
        } else {
            requestGalleryPermission.launch(permissionToRequest)
        }
    }

    private fun openGalleryForProfilePic() {
        // Ganti ACTION_PICK dengan ACTION_GET_CONTENT
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            // Tambahkan flag untuk meminta izin URI read di hasil Intent
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) // Tambahkan ini juga, meskipun tidak semua sistem mendukungnya, ini adalah praktik terbaik.
        }
        pickImage.launch(intent)
    }
}