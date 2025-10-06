package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PreviewFrame : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preview_frame)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnNext = findViewById<Button>(R.id.btn_next)
        val previewImage = findViewById<ImageView>(R.id.previewImage)

        // Ambil frame dari intent
        val frameId = intent.getIntExtra("FRAME_ID", 0)
        if (frameId != 0) {
            previewImage.setImageResource(frameId)
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, Frames::class.java))
        }

        btnNext.setOnClickListener {
            val frameId = intent.getIntExtra("FRAME_ID", 0)
            val intent = Intent(this, PhotoOption::class.java)
            intent.putExtra("FRAME_ID", frameId)
            startActivity(intent)
        }

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
}
