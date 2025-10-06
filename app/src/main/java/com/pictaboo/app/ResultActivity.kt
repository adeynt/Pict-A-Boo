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
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Date

import com.google.firebase.auth.FirebaseAuth
// IMPOR FIREBASE DATA DIHAPUS

class ResultActivity : AppCompatActivity() {

    private lateinit var imgResult: ImageView
    private lateinit var btnBack: ImageButton
    private lateinit var btnRetake: Button
    private lateinit var btnSave: Button

    private val FRAME_RES_ID = R.drawable.my_frame
    private val rects = mutableListOf<Rect>()
    private var resultBmp: Bitmap? = null
    private var photos: ArrayList<Uri> = arrayListOf()

    // INISIALISASI ROOM & AUTH
    private val auth = FirebaseAuth.getInstance()
    private val photoDao by lazy {
        AppDatabase.getDatabase(this).photoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imgResult = findViewById(R.id.imgResult)
        btnBack = findViewById(R.id.btnBack)
        btnRetake = findViewById(R.id.btnRetake)
        btnSave = findViewById(R.id.btnSave)

        photos = intent.getParcelableArrayListExtra("photos") ?: arrayListOf()

        if (photos.size < 3) {
            Toast.makeText(this, "You need 3 photos to create a strip.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        resultBmp = createFramedResult(photos)
        if (resultBmp != null) {
            imgResult.setImageBitmap(resultBmp)
        } else {
            Toast.makeText(this, "Failed to load photo result.", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnRetake.setOnClickListener {
            showChooseSlotDialog()
        }

        // Tombol Save (LOGIKA ROOM)
        btnSave.setOnClickListener {
            resultBmp?.let { bmp ->
                saveImageToGalleryAndRoom(bmp)
            } ?: Toast.makeText(this, "Failed to save, photo is empty.", Toast.LENGTH_SHORT).show()
        }

        imgResult.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && resultBmp != null) {
                val coords = mapTouchToImageCoords(imgResult, resultBmp!!, event.x, event.y)
                val clickedSlot = rects.indexOfFirst { it.contains(coords.x.toInt(), coords.y.toInt()) }

                if (clickedSlot != -1) {
                    showRetakeDialog(clickedSlot)
                }
            }
            true
        }
    }

    /** 🔹 Simpan hasil ke Galeri lokal dan Room Database */
    private fun saveImageToGalleryAndRoom(bitmap: Bitmap) {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Langkah 1: Simpan gambar ke Galeri lokal dan dapatkan URI-nya
        val localUri = saveImageToGallery(bitmap)

        if (localUri != null) {
            // Langkah 2: Simpan metadata ke Room
            val newPhoto = PhotoModel(
                localUserId = user.uid,
                localUri = localUri.toString(),
                timestamp = Date()
            )

            // Menggunakan Coroutine untuk operasi database asinkron
            lifecycleScope.launch {
                try {
                    val newId = photoDao.insertPhoto(newPhoto)
                    Toast.makeText(this@ResultActivity, "Photo saved to Projects! 🎉", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@ResultActivity, "Failed to save metadata to Room: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Failed to save photo to gallery.", Toast.LENGTH_LONG).show()
        }
    }

    /** 🔹 Membuat hasil akhir dengan 3 foto + frame */
    private fun createFramedResult(photos: ArrayList<Uri>): Bitmap? {
        val frameBmp = BitmapFactory.decodeResource(resources, FRAME_RES_ID)
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
                Rect((frameW * leftPct).toInt(), (frameH * startTop).toInt(),
                    (frameW * rightPct).toInt(), (frameH * (startTop + slotHeight)).toInt()),
                Rect((frameW * leftPct).toInt(), (frameH * (startTop + slotHeight + gap)).toInt(),
                    (frameW * rightPct).toInt(), (frameH * (startTop + slotHeight * 2 + gap)).toInt()),
                Rect((frameW * leftPct).toInt(), (frameH * (startTop + slotHeight * 2 + gap * 2)).toInt(),
                    (frameW * rightPct).toInt(), (frameH * (startTop + slotHeight * 3 + gap * 2)).toInt())
            )
        )

        val bitmaps = photos.take(3).mapNotNull { loadBitmapFromUri(it) }
        if (bitmaps.size < 3) return null

        val result = Bitmap.createBitmap(frameW, frameH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val zooms = listOf(1.2f, 1.2f, 1.2f)

        for (i in rects.indices) {
            val src = bitmaps[i]
            val dst = rects[i]
            val fitted = getCenterCroppedScaledBitmap(src, dst.width(), dst.height(), zooms[i])
            canvas.drawBitmap(fitted, dst.left.toFloat(), dst.top.toFloat(), null)
        }

        canvas.drawBitmap(frameBmp, 0f, 0f, null)
        return result
    }

    /** 🔹 Dialog pilih bagian foto yang mau diretake */
    private fun showChooseSlotDialog() {
        val items = arrayOf("Photo 1", "Photo 2", "Photo 3")
        AlertDialog.Builder(this)
            .setTitle("Select a photo to retake")
            .setItems(items) { _, which ->
                showRetakeDialog(which)
            }
            .show()
    }

    /** 🔹 Dialog konfirmasi retake */
    private fun showRetakeDialog(slotIndex: Int) {
        AlertDialog.Builder(this)
            .setTitle("Retake Photo")
            .setMessage("Retake photo ${slotIndex + 1}?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra("slotIndex", slotIndex)
                intent.putParcelableArrayListExtra("photos", photos)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /** 🔹 Simpan hasil ke galeri dan mengembalikan Uri-nya */
    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val filename = "PictABoo_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PictABoo")
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            return uri // Kembalikan URI
        } else {
            return null
        }
    }

    /** 🔹 Load bitmap dari Uri */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 🔹 Crop & zoom foto agar pas dengan frame */
    private fun getCenterCroppedScaledBitmap(
        src: Bitmap,
        targetW: Int,
        targetH: Int,
        scaleMultiplier: Float = 1.0f
    ): Bitmap {
        if (src.width == 0 || src.height == 0) return src
        val scale = maxOf(
            targetW.toFloat() / src.width,
            targetH.toFloat() / src.height
        ) * scaleMultiplier

        val scaledW = (src.width * scale).toInt()
        val scaledH = (src.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)

        val x = ((scaledW - targetW) / 2).coerceAtLeast(0)
        val y = ((scaledH - targetH) / 2).coerceAtLeast(0)

        return Bitmap.createBitmap(scaled, x, y, targetW, targetH)
    }

    /** 🔹 Konversi titik sentuh ke koordinat di gambar sebenarnya */
    private fun mapTouchToImageCoords(imageView: ImageView, bitmap: Bitmap, touchX: Float, touchY: Float): PointF {
        val matrix = FloatArray(9)
        imageView.imageMatrix.getValues(matrix)

        val scaleX = matrix[Matrix.MSCALE_X]
        val scaleY = matrix[Matrix.MSCALE_Y]
        val transX = matrix[Matrix.MTRANS_X]
        val transY = matrix[Matrix.MTRANS_Y]

        val actualX = (touchX - transX) / scaleX
        val actualY = (touchY - transY) / scaleY

        return PointF(actualX, actualY)
    }
}
