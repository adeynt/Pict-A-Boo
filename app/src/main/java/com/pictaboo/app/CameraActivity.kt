package com.pictaboo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var viewFinder: PreviewView
    private lateinit var btnTakePhoto: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnSwitchCamera: ImageButton
    private lateinit var layoutPreviewPhotos: LinearLayout
    private lateinit var tvCountdown: TextView

    private var imageCapture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private val tempPhotoUris = mutableListOf<Uri>()
    private val MAX_PHOTOS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.viewFinder)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnBack = findViewById(R.id.btnBack)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        layoutPreviewPhotos = findViewById(R.id.layoutPreviewPhotos)
        tvCountdown = findViewById(R.id.tvCountdown)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        btnTakePhoto.setOnClickListener {
            startTimerAndTakePhoto()
        }

        btnBack.setOnClickListener { finish() }

        btnSwitchCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetRotation(viewFinder.display.rotation)
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(viewFinder.display.rotation)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startTimerAndTakePhoto() {
        tvCountdown.visibility = android.view.View.VISIBLE

        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) + 1
                tvCountdown.text = seconds.toString()
            }

            override fun onFinish() {
                tvCountdown.visibility = android.view.View.GONE
                takePhoto()
            }
        }.start()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        if (tempPhotoUris.size >= MAX_PHOTOS) {
            Toast.makeText(this, "Maksimal $MAX_PHOTOS foto", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile = File(
            externalCacheDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "Gagal ambil foto: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(photoFile)
                    tempPhotoUris.add(uri)

                    val imageView = ImageView(this@CameraActivity)
                    val params = LinearLayout.LayoutParams(200, 200)
                    params.setMargins(8, 0, 8, 0)
                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setImageURI(uri)
                    layoutPreviewPhotos.addView(imageView)

                    Toast.makeText(baseContext, "Foto ${tempPhotoUris.size} tersimpan", Toast.LENGTH_SHORT).show()

                    // kalau sudah 3 foto, buka page baru
                    if (tempPhotoUris.size == MAX_PHOTOS) {
                        val intent = Intent(this@CameraActivity, ResultActivity::class.java)
                        intent.putParcelableArrayListExtra("photos", ArrayList(tempPhotoUris))
                        startActivity(intent)
                    }
                }
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission tidak diberikan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
