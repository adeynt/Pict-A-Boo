package com.pictaboo.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val profilePictureUri: String? = null // BARU: Menambahkan kolom untuk URI foto profil
)