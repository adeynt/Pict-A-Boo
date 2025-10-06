package com.pictaboo.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insertPhoto(photo: PhotoModel): Long

    // Ambil foto milik user tertentu berdasarkan localUserId
    @Query("SELECT * FROM photos WHERE UserId = :userId ORDER BY timestamp DESC")
    fun getUserPhotos(userId: Int): Flow<List<PhotoModel>>
}
