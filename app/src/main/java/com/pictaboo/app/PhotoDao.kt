package com.pictaboo.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Interface Data Access Object untuk mengakses data PhotoModel dari Room Database.
 */
@Dao
interface PhotoDao {

    @Insert
    suspend fun insertPhoto(photo: PhotoModel): Long

    @Query("SELECT * FROM photos WHERE localUserId = :userId ORDER BY timestamp DESC")
    fun getUserPhotos(userId: String): Flow<List<PhotoModel>> // Kueri menggunakan localUserId
}

