package com.example.myimagelogapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myimagelogapp.entity.UploadTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: UploadTaskEntity)

    @Query("SELECT * FROM upload_task ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UploadTaskEntity>>

    @Query("DELETE FROM upload_task")
    suspend fun clearAll()
}