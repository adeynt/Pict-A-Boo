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
import com.google.android.material.button.MaterialButton
private fun MainActivity.openPreviewWithFrame(frame1: Int) {}

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
        fun openPreviewWithFrame(frameId: Int) {
            val intent = Intent(this, PreviewFrame::class.java)
            intent.putExtra("FRAME_ID", frameId)
            startActivity(intent)
        }

        val btnStart = findViewById<Button>(R.id.btn_start)

        btnStart.setOnClickListener {
            startActivity(Intent(this, Frames::class.java))
        }

        val btnFrame1 = findViewById<LinearLayout>(R.id.btn_frame)
        val btnFrame2 = findViewById<LinearLayout>(R.id.btn_frame2)
        val btnFrame3 = findViewById<LinearLayout>(R.id.btn_frame3)

        btnFrame1.setOnClickListener { openPreviewWithFrame(R.drawable.frame_1) }
        btnFrame2.setOnClickListener { openPreviewWithFrame(R.drawable.frame_2) }
        btnFrame3.setOnClickListener { openPreviewWithFrame(R.drawable.frame_3) }

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
    }
}
