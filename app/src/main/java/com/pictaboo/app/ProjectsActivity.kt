package com.pictaboo.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pictaboo.app.AppDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter

    private val photoDao by lazy {
        AppDatabase.getDatabase(this).photoDao()
    }

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        recyclerView = findViewById(R.id.rv_photos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Ambil user_id dari SharedPreferences
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Silakan login untuk melihat Projects Anda.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchUserPhotosFromRoom()
    }

    /** 🔹 Mengambil daftar foto dari Room untuk user yang sedang login */
    private fun fetchUserPhotosFromRoom() {
        lifecycleScope.launch {
            // collect Flow dari Room
            photoDao.getUserPhotos(userId).collect { photoList ->
                if (!::photoAdapter.isInitialized) {
                    photoAdapter = PhotoAdapter(photoList)
                    recyclerView.adapter = photoAdapter
                } else {
                    photoAdapter.updateData(photoList)
                }

                if (photoList.isEmpty()) {
                    Toast.makeText(
                        this@ProjectsActivity,
                        "Anda belum punya foto di Projects.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
