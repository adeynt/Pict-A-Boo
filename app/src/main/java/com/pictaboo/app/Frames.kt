package com.pictaboo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

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

        val btnFrame = findViewById<LinearLayout>(R.id.btn_frame)
        val btnFrame2 = findViewById<LinearLayout>(R.id.btn_frame2)
        val btnFrame3 = findViewById<LinearLayout>(R.id.btn_frame3)
        val btnFrame4 = findViewById<LinearLayout>(R.id.btn_frame4)
        val btnFrame5 = findViewById<LinearLayout>(R.id.btn_frame5)
        val btnFrame6 = findViewById<LinearLayout>(R.id.btn_frame6)

        btnFrame.setOnClickListener {
            startActivity(Intent(this, PreviewFrame::class.java))
        }

        btnFrame2.setOnClickListener {
            startActivity(Intent(this, PreviewFrame::class.java))
        }
        btnFrame3.setOnClickListener {
            startActivity(Intent(this, PreviewFrame::class.java))
        }
        btnFrame4.setOnClickListener {
            startActivity(Intent(this, PreviewFrame::class.java))
        }
        btnFrame5.setOnClickListener {
            startActivity(Intent(this, PreviewFrame::class.java))
        }
        btnFrame6.setOnClickListener {
            startActivity(Intent(this, PreviewFrame::class.java))
        }
    }
}