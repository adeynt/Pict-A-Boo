package com.pictaboo.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

import com.google.firebase.auth.FirebaseAuth
// IMPOR FIREBASE DATA DIHAPUS

class ProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter

    // Inisialisasi Firebase (Hanya Auth)
    private val auth = FirebaseAuth.getInstance()

    // Inisialisasi Room DAO
    private val photoDao by lazy {
        AppDatabase.getDatabase(this).photoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        recyclerView = findViewById(R.id.rv_photos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (auth.currentUser == null) {
            Toast.makeText(this, "Silakan login untuk melihat Projects Anda.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchUserPhotosFromRoom() // Mengambil data dari Room
    }

    /** 🔹 Mengambil daftar foto dari Room untuk user yang sedang login */
    private fun fetchUserPhotosFromRoom() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Menggunakan Flow dari Room untuk mendapatkan update data secara real-time
        lifecycleScope.launch {
            photoDao.getUserPhotos(currentUserId).collect { photoList ->
                // Inisialisasi atau update adapter
                if (!::photoAdapter.isInitialized) {
                    photoAdapter = PhotoAdapter(photoList)
                    recyclerView.adapter = photoAdapter
                } else {
                    photoAdapter.updateData(photoList)
                }

                if (photoList.isEmpty()) {
                    Toast.makeText(this@ProjectsActivity, "Anda belum punya foto di Projects.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
