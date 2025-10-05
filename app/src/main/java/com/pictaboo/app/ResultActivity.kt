package com.pictaboo.app

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var imgResult: ImageView
    private lateinit var btnBack: ImageButton
    private lateinit var btnRetake: Button
    private lateinit var btnSave: Button

    private var frameResId: Int = R.drawable.my_frame
    private val rects = mutableListOf<Rect>()
    private var resultBmp: Bitmap? = null
    private var photos: ArrayList<Uri> = arrayListOf()
    private var isFromGallery: Boolean = false
    private val MAX_SLOTS = 3

    // Untuk replace slot
    private var currentReplacingSlot: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imgResult = findViewById(R.id.imgResult)
        btnBack = findViewById(R.id.btnBack)
        btnRetake = findViewById(R.id.btnRetake)
        btnSave = findViewById(R.id.btnSave)

        // Ambil FRAME_ID dari intent
        frameResId = intent.getIntExtra("FRAME_ID", R.drawable.my_frame)

        // Ambil foto dari intent
        photos = intent.getParcelableArrayListExtra("photos") ?: arrayListOf()

        // Menentukan apakah ini dari Galeri
        val comingFromCameraRetake = intent.getIntExtra("slotIndex", -1) != -1
        isFromGallery = photos.size > 0 && !comingFromCameraRetake && photos.size < MAX_SLOTS

        if (photos.isEmpty()) {
            Toast.makeText(this, "Tidak ada foto yang dipilih.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Gabungkan foto ke dalam frame
        showResult()

        // Tombol Back
        btnBack.setOnClickListener { finish() }

        // Tombol Retake / Ganti Semua
        if (isFromGallery) {
            btnRetake.text = "Ganti Semua"
            btnRetake.setOnClickListener {
                val intent = Intent(this, PhotoOption::class.java)
                intent.putExtra("FRAME_ID", frameResId) // teruskan frame
                startActivity(intent)
                finish()
            }

            // Touch listener untuk replace slot
            imgResult.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && resultBmp != null) {
                    val coords = mapTouchToImageCoords(imgResult, resultBmp!!, event.x, event.y)
                    val clickedSlot =
                        rects.indexOfFirst { it.contains(coords.x.toInt(), coords.y.toInt()) }
                    if (clickedSlot != -1) showReplaceSlotDialog(clickedSlot)
                }
                true
            }
        } else {
            btnRetake.text = "Retake"
            btnRetake.setOnClickListener { showChooseSlotDialog() }

            imgResult.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && resultBmp != null) {
                    val coords = mapTouchToImageCoords(imgResult, resultBmp!!, event.x, event.y)
                    val clickedSlot =
                        rects.indexOfFirst { it.contains(coords.x.toInt(), coords.y.toInt()) }
                    if (clickedSlot != -1) showRetakeDialog(clickedSlot)
                }
                true
            }
        }

        // Tombol Save
        btnSave.setOnClickListener {
            resultBmp?.let { saveImageToGallery(it) }
                ?: Toast.makeText(this, "Gagal menyimpan, foto kosong.", Toast.LENGTH_SHORT).show()
        }
    }

    /** 🔹 Tampilkan hasil frame + foto */
    private fun showResult() {
        resultBmp = createFramedResult(photos)
        imgResult.setImageBitmap(resultBmp)
    }

    /** 🔹 Dialog ganti foto per slot (Galeri) */
    private fun showReplaceSlotDialog(slotIndex: Int) {
        currentReplacingSlot = slotIndex
        val title = if (slotIndex < photos.size && photos[slotIndex] != Uri.EMPTY)
            "Ganti Foto Slot ke-${slotIndex + 1}"
        else
            "Isi Slot Kosong ke-${slotIndex + 1}"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Apakah Anda ingin memilih foto baru untuk slot ini?")
            .setPositiveButton("Ganti") { _, _ -> launchGalleryForSingleSlot() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private val replaceSinglePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null && currentReplacingSlot != -1) {
                photos[currentReplacingSlot] = imageUri
                showResult()
                currentReplacingSlot = -1
            } else {
                Toast.makeText(this, "Gagal mengganti foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchGalleryForSingleSlot() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        replaceSinglePhoto.launch(intent)
    }

    /** 🔹 Membuat hasil akhir dengan foto + frame */
    private fun createFramedResult(photos: ArrayList<Uri>): Bitmap {
        val frameBmp = BitmapFactory.decodeResource(resources, frameResId)
        val frameW = frameBmp.width
        val frameH = frameBmp.height

        val leftPct = 0.05f
        val rightPct = 0.96f
        val slotHeight = 0.27f
        val gap = 0.026f
        val startTop = 0.02f

        rects.clear()
        rects.addAll(
            listOf(
                Rect(
                    (frameW * leftPct).toInt(), (frameH * startTop).toInt(),
                    (frameW * rightPct).toInt(), (frameH * (startTop + slotHeight)).toInt()
                ),
                Rect(
                    (frameW * leftPct).toInt(),
                    (frameH * (startTop + slotHeight + gap)).toInt(),
                    (frameW * rightPct).toInt(),
                    (frameH * (startTop + slotHeight * 2 + gap)).toInt()
                ),
                Rect(
                    (frameW * leftPct).toInt(),
                    (frameH * (startTop + slotHeight * 2 + gap * 2)).toInt(),
                    (frameW * rightPct).toInt(),
                    (frameH * (startTop + slotHeight * 3 + gap * 2)).toInt()
                )
            )
        )

        while (photos.size < MAX_SLOTS) photos.add(Uri.EMPTY)
        val photosToProcess = photos.take(MAX_SLOTS)

        val result = Bitmap.createBitmap(frameW, frameH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val zooms = listOf(1.2f, 1.2f, 1.2f)

        for (i in 0 until MAX_SLOTS) {
            val photoUri = photosToProcess[i]
            val dst = rects[i]
            if (photoUri != Uri.EMPTY) {
                val src = loadBitmapFromUri(photoUri)
                if (src != null) {
                    val fitted =
                        getCenterCroppedScaledBitmap(src, dst.width(), dst.height(), zooms[i])
                    canvas.drawBitmap(fitted, dst.left.toFloat(), dst.top.toFloat(), null)
                }
            } else {
                val emptyPaint = Paint().apply { color = Color.parseColor("#FFE4EA") }
                canvas.drawRect(dst, emptyPaint)
            }
        }

        canvas.drawBitmap(frameBmp, 0f, 0f, null)
        return result
    }

    private fun showChooseSlotDialog() {
        val items = arrayOf("Foto 1", "Foto 2", "Foto 3")
        AlertDialog.Builder(this)
            .setTitle("Pilih foto yang ingin di-retake")
            .setItems(items) { _, which -> showRetakeDialog(which) }
            .show()
    }

    private fun showRetakeDialog(slotIndex: Int) {
        AlertDialog.Builder(this)
            .setTitle("Retake Foto")
            .setMessage("Ambil ulang foto ke-${slotIndex + 1}?")
            .setPositiveButton("Ya") { _, _ ->
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra("slotIndex", slotIndex)
                intent.putParcelableArrayListExtra("photos", photos)
                intent.putExtra("FRAME_ID", frameResId) // pastikan frame diteruskan
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "PictABoo_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PictABoo")
            }
        }

        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri)
                ?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            Toast.makeText(this, "Berhasil disimpan ke galeri 🎉", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getCenterCroppedScaledBitmap(
        src: Bitmap,
        targetW: Int,
        targetH: Int,
        scaleMultiplier: Float = 1.0f
    ): Bitmap {
        if (src.width == 0 || src.height == 0) return src
        val scale =
            maxOf(targetW.toFloat() / src.width, targetH.toFloat() / src.height) * scaleMultiplier
        val scaledW = (src.width * scale).toInt()
        val scaledH = (src.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)
        val x = ((scaledW - targetW) / 2).coerceAtLeast(0)
        val y = ((scaledH - targetH) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(scaled, x, y, targetW, targetH)
    }

    private fun mapTouchToImageCoords(
        imageView: ImageView,
        bitmap: Bitmap,
        touchX: Float,
        touchY: Float
    ): PointF {
        val matrixValues = FloatArray(9)
        imageView.imageMatrix.getValues(matrixValues)

        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        val actualX = (touchX - transX) / scaleX
        val actualY = (touchY - transY) / scaleY

        return PointF(actualX, actualY)
    }
}
