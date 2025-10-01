package com.pictaboo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PhotoOption : AppCompatActivity() {

    private lateinit var btnCamera: TextView
    private lateinit var btnImport: TextView

    // Permission request untuk kamera
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Kalau granted, buka halaman CameraActivity (real-time kamera)
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission request untuk galeri
    private val requestGalleryPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_option)

        btnCamera = findViewById(R.id.btn_camera)
        btnImport = findViewById(R.id.btn_import)

        // Ketika klik Camera
        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        // Ketika klik Import
        btnImport.setOnClickListener {
            checkGalleryPermission()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Kalau sudah ada izin langsung buka CameraActivity
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        } else {
            // Kalau belum ada izin, request dulu
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestGalleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivity(intent)
    }
}
