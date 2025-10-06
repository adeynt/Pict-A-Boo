package com.pictaboo.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class PhotoEntity(
    // ID unik untuk setiap proyek
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Path/URI foto yang akan disimpan (Ini adalah data yang akan dimuat oleh Glide)
    val filePath: String,

    // Timestamp untuk pengurutan
    val timestamp: Long = System.currentTimeMillis()
)