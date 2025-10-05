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
    private lateinit var btnToggleTimer: ImageButton
    private lateinit var layoutPreviewPhotos: LinearLayout
    private lateinit var tvCountdown: TextView

    private var imageCapture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null

    private val MAX_PHOTOS = 3
    private var slotIndex: Int? = null
    private var existingPhotos = mutableListOf<Uri>()

    private var frameResId: Int = R.drawable.my_frame // frame yang dipilih
    private var isTimerEnabled = true // default timer aktif

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.viewFinder)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnBack = findViewById(R.id.btnBack)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnToggleTimer = findViewById(R.id.btnToggleTimer)
        layoutPreviewPhotos = findViewById(R.id.layoutPreviewPhotos)
        tvCountdown = findViewById(R.id.tvCountdown)

        // Ambil data dari intent
        slotIndex = intent.getIntExtra("slotIndex", -1).takeIf { it != -1 }
        existingPhotos = intent.getParcelableArrayListExtra("photos") ?: mutableListOf()
        frameResId = intent.getIntExtra("FRAME_ID", R.drawable.my_frame)

        // Jalankan kamera
        if (allPermissionsGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

        // Tombol capture
        btnTakePhoto.setOnClickListener {
            if (isTimerEnabled) startTimerAndTakePhoto()
            else takePhoto()
        }

        btnBack.setOnClickListener { finish() }

        btnSwitchCamera.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        btnToggleTimer.setOnClickListener {
            isTimerEnabled = !isTimerEnabled
            updateTimerButton()
        }

        updateTimerButton() // set icon awal
        refreshPreviewPhotos() // tampilkan preview
    }

    /** ================= CAMERA ================= */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetRotation(viewFinder.display.rotation)
                    .build()
                    .also { it.setSurfaceProvider(viewFinder.surfaceProvider) }

                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(viewFinder.display.rotation)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }

    /** ================= COUNTDOWN + FOTO ================= */
    private fun startTimerAndTakePhoto() {
        tvCountdown.visibility = android.view.View.VISIBLE
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCountdown.text = ((millisUntilFinished / 1000) + 1).toString()
            }

            override fun onFinish() {
                tvCountdown.visibility = android.view.View.GONE
                takePhoto()
            }
        }.start()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
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

                    // RETAKE
                    if (slotIndex != null && slotIndex!! in existingPhotos.indices) {
                        existingPhotos[slotIndex!!] = uri
                        goToResult()
                        return
                    }

                    // NORMAL
                    if (existingPhotos.size >= MAX_PHOTOS) {
                        Toast.makeText(baseContext, "Maksimal $MAX_PHOTOS foto", Toast.LENGTH_SHORT).show()
                        return
                    }

                    existingPhotos.add(uri)
                    refreshPreviewPhotos()

                    // Jika sudah 3 foto → lanjut ke result
                    if (existingPhotos.size == MAX_PHOTOS) {
                        goToResult()
                    }
                }
            })
    }

    /** ================= UTIL ================= */
    private fun refreshPreviewPhotos() {
        layoutPreviewPhotos.removeAllViews()
        for (photoUri in existingPhotos) {
            val imageView = ImageView(this)
            val params = LinearLayout.LayoutParams(200, 200)
            params.setMargins(8, 0, 8, 0)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(photoUri)
            layoutPreviewPhotos.addView(imageView)
        }
    }

    private fun goToResult() {
        val intent = Intent(this@CameraActivity, ResultActivity::class.java)
        intent.putParcelableArrayListExtra("photos", ArrayList(existingPhotos))
        intent.putExtra("FRAME_ID", frameResId)
        startActivity(intent)
        finish()
    }

    private fun updateTimerButton() {
        if (isTimerEnabled) btnToggleTimer.setImageResource(R.drawable.ic_timer_on)
        else btnToggleTimer.setImageResource(R.drawable.ic_timer_off)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera()
            else {
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
