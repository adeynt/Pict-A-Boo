package com.pictaboo.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class FaqActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // MEMASTIKAN LAYOUT DIMUAT
        setContentView(R.layout.activity_faq)

        // Null-safe call: Mencegah crash saat mencari tombol kembali
        val backButton: ImageView? = findViewById(R.id.btn_back)

        // Jika tombol kembali ditemukan, tambahkan listener
        backButton?.setOnClickListener {
            finish() // Kembali ke Profile Activity
        }
    }
}