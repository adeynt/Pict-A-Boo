package com.pictaboo.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Model data untuk menyimpan metadata foto, diimplementasikan sebagai Room Entity.
 */
@Entity(tableName = "photos")
data class PhotoModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Int = 0,   // ganti localUserId → userId
    val localUri: String = "",
    val timestamp: Date = Date()
)
