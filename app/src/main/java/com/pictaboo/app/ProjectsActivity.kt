package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pictaboo.app.PhotoModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter

    private val photoDao by lazy { AppDatabase.getDatabase(this).photoDao() }

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        recyclerView = findViewById(R.id.rv_photos)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // ✅ Ambil session user dari SharedPreferences
        val prefs = getSharedPreferences(RegisterActivity.PREFS_NAME, MODE_PRIVATE)
        userId = prefs.getInt(RegisterActivity.KEY_USER_ID, -1)

        if (userId == -1) {
            // User belum login → redirect ke LoginActivity
            Toast.makeText(this, "Please log in to view your Projects.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Ambil foto user dari Room
        fetchUserPhotosFromRoom()

        // Navigasi bawah
        val navProfile = findViewById<TextView>(R.id.nav_profile)
        val navHome = findViewById<TextView>(R.id.nav_home)
        val navFrame = findViewById<TextView>(R.id.nav_frame)
        val navProject = findViewById<TextView>(R.id.nav_project)

        navProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        navHome.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        navFrame.setOnClickListener { startActivity(Intent(this, Frames::class.java)) }
        navProject.setOnClickListener { /* sudah di ProjectsActivity, tidak perlu pindah */ }
    }

    /** 🔹 Mengambil daftar foto dari Room untuk user yang sedang login */
    private fun fetchUserPhotosFromRoom() {
        lifecycleScope.launch {
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
                        "You don't have any photos in Projects yet.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
