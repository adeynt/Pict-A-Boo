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

    private var frameId: Int = 0 // Tambahkan variabel untuk menampung frame

    // START: LAUNCHER BARU UNTUK IZIN KAMERA
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Izin diberikan, luncurkan kamera
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("FRAME_ID", frameId)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
        }
    }
    // END: LAUNCHER BARU UNTUK IZIN KAMERA

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

    // Contract untuk menerima hasil dari Galeri
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedUris = arrayListOf<Uri>()
            val data = result.data

            if (data?.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    selectedUris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                selectedUris.add(data.data!!)
            }

            if (selectedUris.isNotEmpty()) {
                val finalUris = selectedUris.take(3)

                val intent = Intent(this, ResultActivity::class.java)
                intent.putParcelableArrayListExtra("photos", ArrayList(finalUris))
                intent.putExtra("FRAME_ID", frameId) // kirim frame id ke ResultActivity
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "No photo selected.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_option)

        // Ambil FRAME_ID dari PreviewFrame
        frameId = intent.getIntExtra("FRAME_ID", 0)

        btnCamera = findViewById(R.id.btn_camera)
        btnImport = findViewById(R.id.btn_import)

        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

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
            // Izin sudah ada, langsung buka kamera
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("FRAME_ID", frameId) // kirim frame ke CameraActivity juga
            startActivity(intent)
        } else {
            // FIX: Minta izin menggunakan launcher yang sudah dideklarasikan
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermission() {
        val permissionToRequest =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestGalleryPermission.launch(permissionToRequest)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImage.launch(intent)
    }
}