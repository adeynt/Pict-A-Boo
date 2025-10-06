package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fungsi pembantu untuk navigasi Frame
        fun openPreviewWithFrame(frameId: Int) {
            val intent = Intent(this, PreviewFrame::class.java)
            intent.putExtra("FRAME_ID", frameId)
            startActivity(intent)
        }

        // --- INISIALISASI UI ELEMENTS ---
        val btnStart = findViewById<Button>(R.id.btn_start)
        val btnFrame1 = findViewById<LinearLayout>(R.id.btn_frame)
        val btnFrame2 = findViewById<LinearLayout>(R.id.btn_frame2)
        val btnFrame3 = findViewById<LinearLayout>(R.id.btn_frame3)

        // NAVIGASI BAWAH
        val navHome = findViewById<TextView>(R.id.nav_home)
        val navFrame = findViewById<TextView>(R.id.nav_frame)
        val navProject = findViewById<TextView>(R.id.nav_project)
        val navProfile = findViewById<TextView>(R.id.nav_profile)

        // --- SET LISTENERS ---

        // Tombol Start (mengarah ke halaman Frames)
        btnStart.setOnClickListener {
            startActivity(Intent(this, Frames::class.java))
        }

        // Shortcut Frame di tengah halaman
        btnFrame1.setOnClickListener { openPreviewWithFrame(R.drawable.frame_1) }
        btnFrame2.setOnClickListener { openPreviewWithFrame(R.drawable.frame_2) }
        btnFrame3.setOnClickListener { openPreviewWithFrame(R.drawable.frame_3) }

        // Navigasi Bawah

        // Home (Halaman saat ini)
        navHome.setOnClickListener {
            // Karena sudah di MainActivity, tidak perlu pindah activity
        }

        // Frame
        navFrame.setOnClickListener {
            startActivity(Intent(this, Frames::class.java))
        }

        // Projects (ROOM FEATURE)
        navProject.setOnClickListener {
            // Mengarah ke halaman daftar proyek lokal
            startActivity(Intent(this, ProjectsActivity::class.java))
        }

        // Profile
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
