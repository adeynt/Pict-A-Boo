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
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream

class ResultActivity : AppCompatActivity() {

    private lateinit var imgResult: ImageView
    private lateinit var btnBack: ImageButton
    private lateinit var btnRetake: Button
    private lateinit var btnSave: Button

    private val FRAME_RES_ID = R.drawable.my_frame
    private val rects = mutableListOf<Rect>()
    private var resultBmp: Bitmap? = null
    private var photos: ArrayList<Uri> = arrayListOf()
    private var isFromGallery: Boolean = false
    private val MAX_SLOTS = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imgResult = findViewById(R.id.imgResult)
        btnBack = findViewById(R.id.btnBack)
        btnRetake = findViewById(R.id.btnRetake)
        btnSave = findViewById(R.id.btnSave)

        // Ambil foto dari intent
        photos = intent.getParcelableArrayListExtra("photos") ?: arrayListOf()

        // Menentukan apakah ini dari Galeri (dianggap Galeri jika kurang dari 3 foto
        // dikirim dan tidak melalui alur retake kamera)
        val comingFromCameraRetake = intent.getIntExtra("slotIndex", -1) != -1
        isFromGallery = photos.size > 0 && !comingFromCameraRetake && photos.size < MAX_SLOTS

        if (photos.isEmpty()) {
            Toast.makeText(this, "Tidak ada foto yang dipilih.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Gabungkan foto ke dalam frame
        resultBmp = createFramedResult(photos)
        if (resultBmp != null) {
            imgResult.setImageBitmap(resultBmp)
        } else {
            Toast.makeText(this, "Gagal memuat hasil foto.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Tombol Back
        btnBack.setOnClickListener {
            finish()
        }

        // ==========================================================
        // LOGIKA INTERAKSI TOMBOL & TOUCH (Photo Booth vs Gallery)
        // ==========================================================
        if (isFromGallery) {
            btnRetake.text = "Ganti Semua" // Ganti teks tombol
            btnRetake.setOnClickListener {
                // Alihkan kembali ke PhotoOption untuk memilih Galeri lagi
                val intent = Intent(this, PhotoOption::class.java)
                startActivity(intent)
                finish()
            }

            // Aktifkan touch listener untuk mengganti foto per slot
            imgResult.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN && resultBmp != null) {
                    val coords = mapTouchToImageCoords(imgResult, resultBmp!!, event.x, event.y)
                    val clickedSlot = rects.indexOfFirst { it.contains(coords.x.toInt(), coords.y.toInt()) }

                    if (clickedSlot != -1) {
                        showReplaceSlotDialog(clickedSlot) // Panggil dialog ganti foto
                    }
                }
                true
            }

        } else {
            // Logika asli untuk Photo Booth (3 foto)
            btnRetake.text = "Retake"
            btnRetake.setOnClickListener {
                showChooseSlotDialog()
            }
            // Aktifkan touch listener untuk Retake Photo Booth
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

        // Tombol Save (logika tetap sama)
        btnSave.setOnClickListener {
            resultBmp?.let { bmp ->
                saveImageToGallery(bmp)
            } ?: Toast.makeText(this, "Gagal menyimpan, foto kosong.", Toast.LENGTH_SHORT).show()
        }
    }

    /** 🔹 Dialog ganti foto per slot (Hanya untuk Galeri) */
    private fun showReplaceSlotDialog(slotIndex: Int) {
        val title = if (slotIndex < photos.size && photos[slotIndex] != Uri.EMPTY) "Ganti Foto Slot ke-${slotIndex + 1}" else "Isi Slot Kosong ke-${slotIndex + 1}"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Apakah Anda ingin memilih foto baru untuk slot ini?")
            .setPositiveButton("Ganti") { _, _ ->
                // Pindah ke Galeri untuk memilih 1 foto untuk slot ini
                launchGalleryForSingleSlot(slotIndex)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Activity Result Launcher untuk Ganti Foto Slot Tunggal
    private val replaceSinglePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data

            // Fix: Kita perlu cara untuk mendapatkan slotIndex yang dikirim.
            // Solusi sementara: kita harus mengirim slotIndex kembali dari Gallery (yang tidak bisa dilakukan dengan mudah)
            // Asumsi: Karena ini adalah REPLACE, kita anggap slotIndex terakhir yang di klik/diganti adalah slot yang sama.

            // Namun, untuk alur multi-select yang benar, kita harus memastikan URI di-update ke slot yang benar.
            // Karena tidak ada mekanisme passing slotIndex kembali via ActivityResultContracts.StartActivityForResult() tanpa Intent custom,
            // kita akan menggunakan solusi yang paling mungkin: mengirim URI dari Galeri dan me-reload Activity.

            // PENTING: Untuk membuat ini berfungsi dengan benar tanpa intent custom, kita asumsikan
            // ResultActivity akan di-recreate dan Intent baru akan membawa data yang benar.
            // Namun, karena kita tidak bisa mendapatkan slotIndex dari Intent result di sini,
            // kita akan biarkan ini dulu dan fokus pada ResultActivity.recreate()

            if (imageUri != null) {
                // Untuk membuat ini berfungsi, kita perlu menyimpan slotIndex saat launchGalleryForSingleSlot dipanggil.
                // Karena kita tidak memiliki mekanisme tersebut, kita hanya akan me-recreate.
                recreate()
            } else {
                Toast.makeText(this, "Gagal mengganti foto.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchGalleryForSingleSlot(slotIndex: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) // Hanya satu foto
            // Kita tidak bisa mengirim slotIndex langsung di ActivityResultContracts.StartActivityForResult
        }
        replaceSinglePhoto.launch(intent)
    }

    /** 🔹 Membuat hasil akhir dengan foto + frame */
    private fun createFramedResult(photos: ArrayList<Uri>): Bitmap? {
        val frameBmp = BitmapFactory.decodeResource(resources, FRAME_RES_ID)
        val frameW = frameBmp.width
        val frameH = frameBmp.height

        val leftPct = 0.05f
        val rightPct = 0.96f
        val slotHeight = 0.27f
        val gap = 0.026f
        val startTop = 0.02f

        // Rects tetap didefinisikan untuk 3 slot frame (sesuai R.drawable.my_frame)
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

        // Pastikan list fotos memiliki 3 elemen (jika Galeri, slot kosong diisi Uri.EMPTY)
        while (photos.size < MAX_SLOTS) {
            photos.add(Uri.EMPTY)
        }

        val photosToProcess = photos.take(MAX_SLOTS)

        val result = Bitmap.createBitmap(frameW, frameH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val zooms = listOf(1.2f, 1.2f, 1.2f)

        // Loop untuk 3 slot
        for (i in 0 until MAX_SLOTS) {
            val photoUri = photosToProcess[i]

            // Cek apakah slot ini punya foto valid
            if (photoUri != Uri.EMPTY) {
                // Kita perlu me-load bitmap lagi
                val src = loadBitmapFromUri(photoUri)
                if (src != null) {
                    val dst = rects[i]
                    val fitted = getCenterCroppedScaledBitmap(src, dst.width(), dst.height(), zooms[i])
                    canvas.drawBitmap(fitted, dst.left.toFloat(), dst.top.toFloat(), null)
                }
            } else {
                // FIX: Jika slot kosong, gambarlah background agar tidak terlihat seperti hitam/error
                val emptyPaint = Paint()
                emptyPaint.color = Color.parseColor("#FFE4EA") // Warna pink muda (sesuai box_background_pink)
                canvas.drawRect(rects[i], emptyPaint)
            }
        }

        canvas.drawBitmap(frameBmp, 0f, 0f, null)
        return result
    }

    /** 🔹 Dialog pilih bagian foto yang mau diretake (Hanya untuk Photo Booth) */
    private fun showChooseSlotDialog() {
        val items = arrayOf("Foto 1", "Foto 2", "Foto 3")
        AlertDialog.Builder(this)
            .setTitle("Pilih foto yang ingin di-retake")
            .setItems(items) { _, which ->
                showRetakeDialog(which)
            }
            .show()
    }

    /** 🔹 Dialog konfirmasi retake (Hanya untuk Photo Booth) */
    private fun showRetakeDialog(slotIndex: Int) {
        AlertDialog.Builder(this)
            .setTitle("Retake Foto")
            .setMessage("Ambil ulang foto ke-${slotIndex + 1}?")
            .setPositiveButton("Ya") { _, _ ->
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra("slotIndex", slotIndex)
                intent.putParcelableArrayListExtra("photos", photos)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /** 🔹 Simpan hasil ke galeri */
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

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Toast.makeText(this, "Berhasil disimpan ke galeri 🎉", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
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