package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Frames : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_frames)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil semua tombol frame dari layout
        val btnFrame1 = findViewById<LinearLayout>(R.id.btn_frame)
        val btnFrame2 = findViewById<LinearLayout>(R.id.btn_frame2)
        val btnFrame3 = findViewById<LinearLayout>(R.id.btn_frame3)
        val btnFrame4 = findViewById<LinearLayout>(R.id.btn_frame4)
        val btnFrame5 = findViewById<LinearLayout>(R.id.btn_frame5)
        val btnFrame6 = findViewById<LinearLayout>(R.id.btn_frame6)

        // Fungsi untuk membuka halaman preview dengan frame tertentu
        fun openPreviewWithFrame(frameId: Int) {
            val intent = Intent(this, PreviewFrame::class.java)
            intent.putExtra("FRAME_ID", frameId)
            startActivity(intent)
        }

        // Listener untuk setiap frame
        btnFrame1.setOnClickListener { openPreviewWithFrame(R.drawable.frame_1) }
        btnFrame2.setOnClickListener { openPreviewWithFrame(R.drawable.frame_2) }
        btnFrame3.setOnClickListener { openPreviewWithFrame(R.drawable.frame_3) }
        btnFrame4.setOnClickListener { openPreviewWithFrame(R.drawable.frame_4) }
        btnFrame5.setOnClickListener { openPreviewWithFrame(R.drawable.frame_5) }
        btnFrame6.setOnClickListener { openPreviewWithFrame(R.drawable.frame_6) }
    }
}
