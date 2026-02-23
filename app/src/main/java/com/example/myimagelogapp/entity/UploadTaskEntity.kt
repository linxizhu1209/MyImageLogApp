package com.example.myimagelogapp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_task")
data class UploadTaskEntity(
    @PrimaryKey val workId: String,
    val title: String,
    val content: String,
    val photoCount: Int,
    val status: String,
    val progress: Int,
    val createdAt: Long,
    val errorMessage: String?,
    val resultJson: String? = null
)