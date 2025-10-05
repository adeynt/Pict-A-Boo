package com.pictaboo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.ArrayList

class PhotoOption : AppCompatActivity() {

    private lateinit var btnCamera: TextView
    private lateinit var btnImport: TextView

    // Permission request untuk Galeri
    private val requestGalleryPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Contract untuk menerima hasil dari Galeri (Disesuaikan untuk Multi-Select)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedUris = arrayListOf<Uri>()
            val data = result.data

            if (data?.clipData != null) {
                // FIX KRITIS: Mode Multi-select (mengambil hingga 3 foto)
                for (i in 0 until data.clipData!!.itemCount) {
                    selectedUris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                // Mode Single-select (fallback, jika user hanya memilih 1 foto)
                selectedUris.add(data.data!!)
            }

            if (selectedUris.isNotEmpty()) {
                // Hanya kirim maksimal 3 foto ke ResultActivity
                val finalUris = selectedUris.take(3)

                val intent = Intent(this, ResultActivity::class.java)
                // Mengirim semua URI yang dipilih ke ResultActivity
                intent.putParcelableArrayListExtra("photos", ArrayList(finalUris))

                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Tidak ada foto yang dipilih.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_option)

        btnCamera = findViewById(R.id.btn_camera)
        btnImport = findViewById(R.id.btn_import)

        // Ketika klik Camera (Logika ini diasumsikan sudah benar)
        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        // Ketika klik Import (Galeri)
        btnImport.setOnClickListener {
            checkGalleryPermission() // Memulai proses pengecekan izin
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        } else {
            // Logika request permission kamera
        }
    }

    /** Memeriksa Izin yang Tepat Berdasarkan Versi Android **/
    private fun checkGalleryPermission() {
        val permissionToRequest =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestGalleryPermission.launch(permissionToRequest)
        }
    }

    /** Membuka Galeri dan menunggu hasil **/
    private fun openGallery() {
        // FIX KRITIS: Menggunakan ACTION_GET_CONTENT dan Intent.EXTRA_ALLOW_MULTIPLE
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Mengizinkan pemilihan banyak foto
        }
        pickImage.launch(intent)
    }
}