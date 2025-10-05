package com.pictaboo.app

import com.google.firebase.Timestamp

/**
 * Model data untuk menyimpan metadata foto yang diambil dari Firestore.
 */
data class PhotoModel(
    val id: String = "",
    val userId: String = "",
    val url: String = "",
    val timestamp: Timestamp = Timestamp.now()
)