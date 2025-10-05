package com.pictaboo.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter // Deklarasi variabel Adapter

    // Inisialisasi Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects) // Asumsi nama layout ini

        recyclerView = findViewById(R.id.rv_photos) // Asumsi ID RecyclerView ini
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (auth.currentUser == null) {
            Toast.makeText(this, "Silakan login untuk melihat Projects Anda.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        fetchUserPhotos()
    }

    /** 🔹 Mengambil daftar foto dari Firestore untuk user yang sedang login */
    private fun fetchUserPhotos() {
        val currentUserId = auth.currentUser!!.uid

        db.collection("photos")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                // Mapping dokumen Firestore ke PhotoModel
                val photoList = result.documents.map { document ->
                    document.toObject(PhotoModel::class.java)!!.copy(id = document.id)
                }

                // LANGKAH 2: Hubungkan (Inisialisasi Adapter dan set ke RecyclerView)
                photoAdapter = PhotoAdapter(photoList) // FIX: Hanya 1 parameter
                recyclerView.adapter = photoAdapter

                if (photoList.isEmpty()) {
                    Toast.makeText(this, "Anda belum punya foto di Projects.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}