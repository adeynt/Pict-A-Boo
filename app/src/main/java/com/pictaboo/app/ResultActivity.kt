package com.pictaboo.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class ResultActivity : AppCompatActivity() {

    private lateinit var imgResult: ImageView

    // Frame strip
    private val FRAME_RES_ID = R.drawable.my_frame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imgResult = findViewById(R.id.imgResult)

        val photos = intent.getParcelableArrayListExtra<Uri>("photos")
        if (photos == null || photos.size < 3) {
            Toast.makeText(this, "Butuh 3 foto untuk menempel ke strip", Toast.LENGTH_SHORT).show()
            return
        }

        // Load frame strip
        val frameBmp = BitmapFactory.decodeResource(resources, FRAME_RES_ID)
        if (frameBmp == null) {
            Toast.makeText(this, "Gagal load frame", Toast.LENGTH_SHORT).show()
            return
        }

        val w = frameBmp.width
        val h = frameBmp.height

        // Persentase area slot
        val leftPct = 0.05f
        val rightPct = 0.96f

        val slotHeight = 0.27f   // tinggi tiap foto = 27%
        val gap = 0.026f         // jarak antar foto = 2.6%
        val startTop = 0.02f     // margin atas (4%)

        val top1TopPct = startTop
        val top1BottomPct = top1TopPct + slotHeight

        val top2TopPct = top1BottomPct + gap
        val top2BottomPct = top2TopPct + slotHeight

        val top3TopPct = top2BottomPct + gap
        val top3BottomPct = top3TopPct + slotHeight


        val rectTop = Rect(
            (w * leftPct).toInt(),
            (h * top1TopPct).toInt(),
            (w * rightPct).toInt(),
            (h * top1BottomPct).toInt()
        )

        val rectMiddle = Rect(
            (w * leftPct).toInt(),
            (h * top2TopPct).toInt(),
            (w * rightPct).toInt(),
            (h * top2BottomPct).toInt()
        )

        val rectBottom = Rect(
            (w * leftPct).toInt(),
            (h * top3TopPct).toInt(),
            (w * rightPct).toInt(),
            (h * top3BottomPct).toInt()
        )

        // Load foto
        val bitmaps = mutableListOf<Bitmap>()
        photos.take(3).forEach { uri ->
            loadBitmapFromUri(uri)?.let { bitmaps.add(it) }
        }

        if (bitmaps.size < 3) {
            Toast.makeText(this, "Gagal memuat beberapa foto", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat bitmap hasil
        val resultBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBmp)

        val dstRects = listOf(rectTop, rectMiddle, rectBottom)

        // Bisa atur zoom tiap slot kalau mau beda (misalnya 1.2f, 1.1f, dst)
        val zooms = listOf(1.2f, 1.2f, 1.2f)

        for (i in 0..2) {
            val src = bitmaps[i]
            val dst = dstRects[i]
            val fitted = getCenterCroppedScaledBitmap(src, dst.width(), dst.height(), zooms[i])
            canvas.drawBitmap(fitted, dst.left.toFloat(), dst.top.toFloat(), null)
        }

        // Gambar frame di atas
        canvas.drawBitmap(frameBmp, 0f, 0f, null)

        imgResult.setImageBitmap(resultBmp)
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val input: InputStream? = contentResolver.openInputStream(uri)
            input.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // scale + center-crop, dengan zoom tambahan
    private fun getCenterCroppedScaledBitmap(
        src: Bitmap,
        targetW: Int,
        targetH: Int,
        scaleMultiplier: Float = 1.0f
    ): Bitmap {
        val srcW = src.width
        val srcH = src.height
        if (srcW == 0 || srcH == 0) return src

        val scale = Math.max(
            targetW.toFloat() / srcW.toFloat(),
            targetH.toFloat() / srcH.toFloat()
        ) * scaleMultiplier

        val scaledW = (srcW * scale).toInt()
        val scaledH = (srcH * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)

        val x = (scaledW - targetW) / 2
        val y = (scaledH - targetH) / 2
        return Bitmap.createBitmap(scaled, x.coerceAtLeast(0), y.coerceAtLeast(0), targetW, targetH)
    }
}
